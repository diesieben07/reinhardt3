package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.*

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

    }

    public fun generate() {
        val file = FileSpec.builder(
            model.className.packageName,
            model.className.simpleNames.joinToString(postfix = "__reinhardt_generated", separator = "_")
        )

        val entityInterfaceClassName = model.className.peerClass(model.className.simpleName + "Entity")
        val entityInterface = TypeSpec.interfaceBuilder(
            entityInterfaceClassName
        )

        val entityClass = TypeSpec.classBuilder(
            model.className.peerClass(model.className.simpleName + "EntityD")
        )
        entityClass.addModifiers(KModifier.PRIVATE)
        entityClass.addSuperinterface(entityInterfaceClassName)

        val entityClassConstructor = FunSpec.constructorBuilder()

        entityClassConstructor.addParameter("_db", databaseClassName)

        entityClass.addProperty(
            PropertySpec.builder(
                "_db", databaseClassName
            ).initializer("_db").build()
        )

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
        }

        entityClass.primaryConstructor(entityClassConstructor.build())

        val primaryKeyField = model.fields.singleOrNull { it.isPrimaryKey }

        val primaryKeyFun = FunSpec.builder("primaryKey")
            .receiver(model.className)
        if (primaryKeyField != null) {
            primaryKeyFun.returns(
                checkNotNull(primaryKeyField.asBasicFieldType)
            )
            primaryKeyFun.addCode(
                "return %T.%N", model.className, primaryKeyField.name
            )
        } else {
            primaryKeyFun.returns(NOTHING.copy(nullable = true))
            primaryKeyFun.addCode("return null")
        }

        file.addFunction(primaryKeyFun.build())
        file.addType(entityInterface.build())
        file.addType(entityClass.build())

        target.accept(file.build())
    }


}