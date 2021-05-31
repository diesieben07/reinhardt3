package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

public class ModelCodegen(
    private val model: CodegenModel,
    private val target: CodegenTarget,
) {

    private companion object {

        const val basePackage = "dev.weiland.reinhardt"
        const val dbPackage = "$basePackage.db"
        const val modelPackage = "$basePackage.model"
        val databaseClassName = ClassName(dbPackage, "Database")
        val fieldClassName = ClassName(modelPackage, "Field")
        val basicFieldClassName = ClassName(modelPackage, "BasicField")
        val modelReaderClassName = ClassName(modelPackage, "ModelReader")
        val dbRowClassName = ClassName(dbPackage, "DbRow")

        const val modelReaderReadEntityNullable = "readEntityNullable"
        const val dbRowDatabaseProperty = "database"

    }

    public fun generate() {
        val file = FileSpec.builder(
            model.className.packageName,
            model.className.simpleNames.joinToString(postfix = "__reinhardt_generated", separator = "_")
        )

        val entityInterfaceClassName = model.className.peerClass(model.className.simpleName + "Entity")
        val entityInterface = TypeSpec.interfaceBuilder(entityInterfaceClassName)

        val entityClassClassName = model.className.peerClass(model.className.simpleName + "EntityD")
        val entityClass = TypeSpec.classBuilder(entityClassClassName)
        entityClass.addModifiers(KModifier.PRIVATE)
        entityClass.addSuperinterface(entityInterfaceClassName)

        val entityReaderClassName = model.className.peerClass(model.className.simpleName + "EntityR")
        val modelReaderSuperclass = modelReaderClassName.parameterizedBy(
            model.className, entityInterfaceClassName
        )
        val entityReaderClass = TypeSpec.objectBuilder(entityReaderClassName)
            .addSuperinterface(modelReaderSuperclass)

        val entityReaderReadFun = FunSpec.builder(modelReaderReadEntityNullable)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("row", dbRowClassName)
            .addParameter("columnPrefix", STRING)
            .returns(entityInterfaceClassName.copy(nullable = true))

        val entityClassConstructor = FunSpec.constructorBuilder()

        entityClassConstructor.addParameter("_db", databaseClassName)

        entityClass.addProperty(
            PropertySpec.builder(
                "_db", databaseClassName
            ).initializer("_db").build()
        )

        val primaryKeyField = model.fields.singleOrNull { it.isPrimaryKey }

        val primaryKeyFun = FunSpec.builder("primaryKey")
            .receiver(model.className)

        if (primaryKeyField != null) {
            entityReaderReadFun.addCode(
                "val %N = %T.%N.fromDbNullable(row, columnPrefix + %S) ?: return null\n",
                primaryKeyField.name, model.className, primaryKeyField.name, primaryKeyField.name
            )

            primaryKeyFun.returns(
                checkNotNull(primaryKeyField.basicFieldContentType)
            )
            primaryKeyFun.addCode(
                "return %T.%N", model.className, primaryKeyField.name
            )
        } else {
            primaryKeyFun.returns(NOTHING.copy(nullable = true))
            primaryKeyFun.addCode("return null")
        }

        for (field in model.fields) {

            for (entityProperty in field.entityProperties) {
                val interfaceProperty = PropertySpec.builder(
                    entityProperty.name, entityProperty.type
                )
                entityInterface.addProperty(interfaceProperty.build())

                val classProperty = PropertySpec.builder(
                    entityProperty.name, entityProperty.type
                ).initializer(entityProperty.name).addModifiers(KModifier.OVERRIDE)
                entityClassConstructor.addParameter(entityProperty.name, entityProperty.type)
                entityClass.addProperty(classProperty.build())
            }
            if (!field.isPrimaryKey) {
                if (field.basicFieldContentType != null) {
                    entityReaderReadFun.addCode(
                        "val %N = %T.%N.fromDb(row, columnPrefix + %S)\n",
                        field.name, model.className, field.name, field.name
                    )
                } else {
                    entityReaderReadFun.addCode(
                        "val %N = %M()\n",
                        field.name, MemberName("kotlin", "TODO")
                    )
                }
            }
        }

        entityReaderReadFun.addCode(
            "return %T(row.database", entityClassClassName
        )
        for (field in model.fields) {
            entityReaderReadFun.addCode(
                ", %N", field.name
            )
        }
        entityReaderReadFun.addCode(")")

        entityReaderClass.addFunction(entityReaderReadFun.build())

        entityClass.primaryConstructor(entityClassConstructor.build())

        file.addFunction(primaryKeyFun.build())
        file.addType(entityInterface.build())
        file.addType(entityClass.build())
        file.addType(entityReaderClass.build())

        target.accept(file.build())
    }


}