package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.weiland.reinhardt.constants.KnownNames

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
        val modelReaderWithPkClassName = ClassName(modelPackage, "ModelReaderWithPK")

        val modelCompanionClassName = ClassName(modelPackage, "ModelCompanion")
        val modelCompanionWithPkClassName = ClassName(modelPackage, "ModelCompanionWithPK")
        val dbRowClassName = ClassName(dbPackage, "DbRow")

        const val modelReaderReadEntityNullable = "readEntityNullable"
        const val modelReaderReadPKNullable = "readPrimaryKeyNullable"
        const val dbRowDatabaseProperty = "database"
        const val primaryKeyFieldName = "primaryKeyField"

        val kotlinJvmNameClassName = ClassName("kotlin.jvm", "JvmName")

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

        val modelExpressionContainerClassName = model.className.peerClass(model.className.simpleName + "Ref")
        val modelExpressionContainerClass = TypeSpec.classBuilder(modelExpressionContainerClassName)
            .addModifiers(KModifier.PRIVATE)
            .addSuperinterface(KnownNames.MODEL_EXPRESSION_CONTAINER_CLASS_NAME)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter(
                        KnownNames.MODEL_EXPRESSION_CONTAINER_ALIAS_PARAMETER,
                        STRING,
                    )
                    .build()
            )
            .addProperty(
                PropertySpec.builder("_alias", STRING, KModifier.PRIVATE)
                    .initializer("%N", KnownNames.MODEL_EXPRESSION_CONTAINER_ALIAS_PARAMETER)
                    .build()
            )
            .addFunction(
                FunSpec.builder(KnownNames.MODEL_EXPRESSION_CONTAINER_ALIAS)
                    .returns(STRING)
                    .addModifiers(KModifier.OVERRIDE, KModifier.FINAL)
                    .addCode("return this.%N", "_alias")
                    .build()
            )

        val entityReaderGenericType = modelReaderClassName.parameterizedBy(model.className, entityInterfaceClassName)
        val modelCompanionGenericType = modelCompanionClassName.parameterizedBy(model.className, entityInterfaceClassName)
        val modelCompanionClass = TypeSpec.objectBuilder(entityReaderClassName)
            .addModifiers(KModifier.PRIVATE)
            .addSuperinterface(modelCompanionGenericType)
            .addSuperinterface(entityReaderGenericType)

        val entityReaderReadFun = FunSpec.builder(modelReaderReadEntityNullable)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("database", databaseClassName)
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

        var genContext = FieldGenContext(
            entityInterfaceName = entityInterfaceClassName, entityInterface = entityInterface,
            entityClassName = entityClassClassName, entityClass = entityClass, entityClassConstructor = entityClassConstructor,
            entityCompanionName = entityReaderClassName, entityCompanion = modelCompanionClass,
            entityReaderReadNullableFun = entityReaderReadFun,
            entityClassCallParams = mutableListOf(),
            entityReaderReadPKNullableFun = null,
            modelExpressionContainerClassName = modelExpressionContainerClassName,
            modelExpressionContainerClass = modelExpressionContainerClass
        )

        var foundPk: FieldCodegen? = null
        for (fieldGen in fieldGens) {
            val primaryKeyType = fieldGen.primaryKeyType
            if (primaryKeyType != null) {
                check(foundPk == null) { "Duplicate primary key for model $model" }
                foundPk = fieldGen

                val entityReaderReadPKNullableFun = FunSpec.builder(modelReaderReadPKNullable)
                    .addModifiers(KModifier.OVERRIDE)
                    .returns(primaryKeyType.copy(nullable = true))
                    .addParameter("database", databaseClassName)
                    .addParameter("row", dbRowClassName)
                    .addParameter("columnPrefix", STRING)

                genContext = genContext.copy(entityReaderReadPKNullableFun = entityReaderReadPKNullableFun)
            }
            fieldGen.generate(genContext)
        }

        val (selectedEntityReaderSupertype, selectedModelCompanionSupertype) = if (foundPk != null) {
            val primaryKeyType = checkNotNull(foundPk.primaryKeyType)
            val entityReaderGenericTypeWithPK = modelReaderWithPkClassName.parameterizedBy(
                model.className, entityInterfaceClassName, primaryKeyType
            )
            val modelCompanionGenericTypeWithPK = modelCompanionWithPkClassName.parameterizedBy(
                model.className, entityInterfaceClassName, primaryKeyType
            )
            modelCompanionClass
                .addSuperinterface(modelCompanionGenericTypeWithPK)
                .addSuperinterface(entityReaderGenericTypeWithPK)

            modelCompanionClass.addProperty(
                PropertySpec.builder(
                    primaryKeyFieldName,
                    basicFieldClassName.parameterizedBy(primaryKeyType),
                    KModifier.OVERRIDE
                ).getter(
                    FunSpec.getterBuilder().addCode("return %T.%N", model.className, foundPk.info.name).build()
                ).build()
            )

            modelCompanionClass.addFunction(
                checkNotNull(genContext.entityReaderReadPKNullableFun).build()
            )
            Pair(entityReaderGenericTypeWithPK, modelCompanionGenericTypeWithPK)
        } else {
            Pair(entityReaderGenericType, modelCompanionGenericType)
        }

        entityReaderReadFun.addCode(
            "return %T(database", entityClassClassName
        )
        for (param in genContext.entityClassCallParams) {
            entityReaderReadFun.addCode(", ")
            entityReaderReadFun.addCode(param)
        }
        entityReaderReadFun.addCode(")")

        modelCompanionClass.addFunction(entityReaderReadFun.build())

        modelCompanionClass.addProperty(
            PropertySpec.builder(
                KnownNames.MODEL_COMPANION_ENTITY_READER_VAL,
                selectedEntityReaderSupertype,
                KModifier.OVERRIDE
            )
                .getter(
                    FunSpec.getterBuilder()
                        .addCode("return this")
                        .build()
                )
                .build()
        )

        modelCompanionClass.addProperty(
            PropertySpec.builder(
                KnownNames.MODEL_COMPANION_MODEL_VAL,
                model.className,
                KModifier.OVERRIDE
            )
                .getter(
                    FunSpec.getterBuilder()
                        .addCode("return %T", model.className)
                        .build()
                )
                .build()
        )

        entityClass.primaryConstructor(entityClassConstructor.build())

        val entityReaderFun = FunSpec.builder(CodegenConstants.ENTITY_READER_FUN)
            .addCode("return %T", entityReaderClassName)
            .receiver(model.className)
            .returns(selectedEntityReaderSupertype)
            .build()

        val modelCompanionFun = FunSpec.builder(CodegenConstants.MODEL_COMPANION_FUN)
            .addCode("return %T", entityReaderClassName)
            .receiver(model.className)
            .returns(selectedModelCompanionSupertype)
            .build()

//        file.addFunction(primaryKeyFun.build())
        file.addType(modelExpressionContainerClass.build())
        file.addType(entityInterface.build())
        file.addType(entityClass.build())
        file.addType(modelCompanionClass.build())
        file.addFunction(entityReaderFun)
        file.addFunction(modelCompanionFun)

        file.addAnnotation(
            AnnotationSpec.builder(kotlinJvmNameClassName)
                .addMember("%S", KnownNames.getGeneratedFileClassName(model.className.reflectionName()))
                .build()
        )

        target.accept(file.build())
    }


}