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
        val modelCompanionClassName = ClassName(modelPackage, "ModelCompanion")
        val modelCompanionWithPkClassName = ClassName(modelPackage, "ModelCompanionWithPK")
        val dbRowClassName = ClassName(dbPackage, "DbRow")

        const val modelReaderReadEntityNullable = "readEntityNullable"
        const val dbRowDatabaseProperty = "database"
        const val primaryKeyFieldName = "primaryKeyField"

    }

    public fun generate(fieldGens: List<FieldCodegen>) {
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
        val modelCompanionClass = TypeSpec.objectBuilder(entityReaderClassName)
            .addSuperinterface(
                modelCompanionClassName.parameterizedBy(model.className, entityInterfaceClassName)
            )

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

        val genContext = FieldGenContext(
            entityInterfaceName = entityInterfaceClassName, entityInterface = entityInterface,
            entityClassName = entityClassClassName, entityClass = entityClass, entityClassConstructor = entityClassConstructor,
            entityCompanionName = entityReaderClassName, entityCompanion = modelCompanionClass,
            entityReaderReadNullableFun = entityReaderReadFun,
            entityClassCallParams = mutableListOf()
        )

        var foundPk: FieldCodegen? = null
        for (fieldGen in fieldGens) {
            fieldGen.generate(genContext)
            val primaryKeyType = fieldGen.primaryKeyType
            if (primaryKeyType != null) {
                check(foundPk == null) { "Duplicate primary key for model $model"}
                foundPk = fieldGen
            }
        }

        if (foundPk != null) {
            val primaryKeyType = checkNotNull(foundPk.primaryKeyType)
            modelCompanionClass.addSuperinterface(
                modelCompanionWithPkClassName.parameterizedBy(
                    model.className, entityInterfaceClassName, primaryKeyType
                )
            )

            modelCompanionClass.addProperty(
                PropertySpec.builder(
                    primaryKeyFieldName,
                    basicFieldClassName.parameterizedBy(primaryKeyType),
                    KModifier.OVERRIDE
                ).getter(
                    FunSpec.getterBuilder().addCode("return %T.%N", model.className, foundPk.info.name).build()
                ).build()
            )
        }

        entityReaderReadFun.addCode(
            "return %T(row.database", entityClassClassName
        )
        for (param in genContext.entityClassCallParams) {
            entityReaderReadFun.addCode(", ")
            entityReaderReadFun.addCode(param)
        }
        entityReaderReadFun.addCode(")")

        modelCompanionClass.addFunction(entityReaderReadFun.build())

        entityClass.primaryConstructor(entityClassConstructor.build())

//        file.addFunction(primaryKeyFun.build())
        file.addType(entityInterface.build())
        file.addType(entityClass.build())
        file.addType(modelCompanionClass.build())

        target.accept(file.build())
    }


}