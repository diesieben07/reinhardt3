package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.weiland.reinhardt.gen.field.BasicFieldCodegen

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

    private fun CodegenField.makeCodegen(): FieldCodegen {
        return when {
            basicFieldContentType != null -> BasicFieldCodegen(model, this)
            else -> TODO()
        }
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

        val genContext = FieldGenContext(
            entityInterfaceName = entityInterfaceClassName, entityInterface = entityInterface,
            entityClassName = entityClassClassName, entityClass = entityClass, entityClassConstructor = entityClassConstructor,
            entityReaderName = entityReaderClassName, entityReader = entityReaderClass,
            entityReaderReadNullableFun = entityReaderReadFun,
            entityClassCallParams = mutableListOf()
        )

        val fieldGens = model.fields.sortedBy { !it.isPrimaryKey }.map { it.makeCodegen() }
        for (fieldGen in fieldGens) {
            fieldGen.generate(genContext)
        }

        entityReaderReadFun.addCode(
            "return %T(row.database", entityClassClassName
        )
        for (param in genContext.entityClassCallParams) {
            entityReaderReadFun.addCode(", ")
            entityReaderReadFun.addCode(param)
        }
        entityReaderReadFun.addCode(")")

        entityReaderClass.addFunction(entityReaderReadFun.build())

        entityClass.primaryConstructor(entityClassConstructor.build())

//        file.addFunction(primaryKeyFun.build())
        file.addType(entityInterface.build())
        file.addType(entityClass.build())
        file.addType(entityReaderClass.build())

        target.accept(file.build())
    }


}