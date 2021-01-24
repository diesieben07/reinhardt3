package dev.weiland.reinhardt.dbgen.codegen

import com.squareup.kotlinpoet.*
import dev.weiland.reinhardt.ResultRow
import dev.weiland.reinhardt.dbgen.type.CodegenType
import dev.weiland.reinhardt.type.ColumnType
import schemacrawler.schema.Column
import schemacrawler.schema.Table

class EntityGenerator(private val ctx: CodegenContext) {

    private companion object {
        val RESULT_ROW_PARAM_NAME = "row"
    }

    fun generate(table: Table): TypeSpec {
        return TypeSpec.interfaceBuilder(ctx.nameTransformer.getTableName(table)).let { builder ->
            for (column in table.columns) {
                builder.addProperty(generateColumnProperty(column))
            }
            builder.build()
        }
    }

    fun generateImpl(table: Table): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(ctx.nameTransformer.getTableName(table))
        val primaryConstructorBuilder = FunSpec.constructorBuilder()
        val secondaryConstructorCallArgs = mutableListOf<CodeBlock>()
        for (column in table.columns) {
            val name = ctx.nameTransformer.getColumnName(column)
            val type = ctx.typeMapper.getColumnType(ctx.catalog, column, column.columnDataType)
            primaryConstructorBuilder.addParameter(generateClassPrimaryConstructorParameter(column, name, type))

            classBuilder.addProperty(generateClassProperty(column, name, type))

            secondaryConstructorCallArgs += generateClassRowConstructorCallArg(column, name, type)
        }

        classBuilder.primaryConstructor(primaryConstructorBuilder.build())
        classBuilder.addFunction(
            FunSpec.constructorBuilder()
                .addParameter(RESULT_ROW_PARAM_NAME, ResultRow::class)
                .callThisConstructor(secondaryConstructorCallArgs)
                .build()
        )

        return classBuilder.build()
    }

    private fun generateClassPrimaryConstructorParameter(column: Column, name: String, type: CodegenType): ParameterSpec {
        return ParameterSpec.builder(name, type.kotlinType.asTypeName()).build()
    }

    private fun generateClassProperty(column: Column, name: String, type: CodegenType): PropertySpec {
        return PropertySpec.builder(name, type.kotlinType.asTypeName())
            .initializer("%N", name)
            .build()
    }

    private fun generateClassRowConstructorCallArg(column: Column, name: String, type: CodegenType): CodeBlock {
        return CodeBlock.builder()
            .add(type.getInitializer())
            .add(".%M(%N, %S)", MemberName(ColumnType::class.asClassName(), "get"), RESULT_ROW_PARAM_NAME, name)
            .build()
    }

    private fun generateColumnProperty(column: Column): PropertySpec {
        val columnType = ctx.typeMapper.getColumnType(ctx.catalog, column, column.columnDataType)
        return PropertySpec.builder(ctx.nameTransformer.getColumnName(column), columnType.kotlinType.asTypeName())
            .build()
    }

}