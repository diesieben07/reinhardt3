package dev.weiland.reinhardt.dbgen.codegen

//import com.squareup.kotlinpoet.*
//import dev.weiland.reinhardt.ResultRow
//import dev.weiland.reinhardt.dbgen.codegen.ctx.CodegenContextImpl
//import dev.weiland.reinhardt.dbgen.codegen.ctx.EntityGenContext
//import dev.weiland.reinhardt.dbgen.type.SimpleColumnType
//import schemacrawler.schema.Column
//import schemacrawler.schema.Table
//
//class EntityGenerator(private val ctx: CodegenContextImpl) {
//
//    private companion object {
//        val RESULT_ROW_PARAM_NAME = "row"
//    }
//
//    fun generate(table: Table, packageName: String): TypeSpec {
//        val entityClassName = ClassName(packageName, ctx.nameTransformer.getTableName(table))
//        val companionObjectName = entityClassName.nestedClass("Companion")
//        val context = EntityGenContext(
//            table = table,
//            entityClassName = entityClassName,
//            companionObjectName = companionObjectName
//        )
//        return generateImpl(context)
////        return TypeSpec.interfaceBuilder(entityClassName).let { builder ->
////            for (column in table.columns) {
////                builder.addProperty(generateColumnProperty(column))
////            }
////            builder.build()
////        }
//    }
//
//    fun generateImpl(ctx: EntityGenContext): TypeSpec {
//        val classBuilder = TypeSpec.classBuilder(ctx.entityClassName)
//        val primaryConstructorBuilder = FunSpec.constructorBuilder()
//        val secondaryConstructorCallArgs = mutableListOf<CodeBlock>()
//        for (column in ctx.table.columns) {
//            val name = ctx.nameTransformer.getColumnName(column)
//            val type = ctx.typeMapper.getColumnType(ctx.catalog, column, column.columnDataType)
//            primaryConstructorBuilder.addParameter(generateClassPrimaryConstructorParameter(column, name, type))
//
//            classBuilder.addProperty(generateClassProperty(column, name, type))
//
//            secondaryConstructorCallArgs += generateClassRowConstructorCallArg(column, name, type)
//        }
//
//        classBuilder.primaryConstructor(primaryConstructorBuilder.build())
//        classBuilder.addFunction(
//            FunSpec.constructorBuilder()
//                .addParameter(RESULT_ROW_PARAM_NAME, ResultRow::class)
//                .callThisConstructor(secondaryConstructorCallArgs)
//                .build()
//        )
//
//        return classBuilder.build()
//    }
//
//    private fun generateClassPrimaryConstructorParameter(column: Column, name: String, type: SimpleColumnType): ParameterSpec {
//        return ParameterSpec.builder(name, type.runtimeType.asTypeName()).build()
//    }
//
//    private fun generateClassProperty(column: Column, name: String, type: SimpleColumnType): PropertySpec {
//        return PropertySpec.builder(name, type.runtimeType.asTypeName())
//            .initializer("%N", name)
//            .build()
//    }
//
//    private fun generateClassRowConstructorCallArg(column: Column, name: String, type: SimpleColumnType): CodeBlock {
//        return CodeBlock.builder()
//            .add(type.getInitializer())
//            .add(".get(%N, %S)", RESULT_ROW_PARAM_NAME, name)
//            .build()
//    }
//
//    private fun generateColumnProperty(column: Column): PropertySpec {
//        val columnType = ctx.typeMapper.getColumnType(ctx.catalog, column, column.columnDataType)
//        return PropertySpec.builder(ctx.nameTransformer.getColumnName(column), columnType.kotlinType.asTypeName())
//            .build()
//    }
//
//}