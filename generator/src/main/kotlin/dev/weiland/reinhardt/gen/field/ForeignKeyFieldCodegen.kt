package dev.weiland.reinhardt.gen.field

import com.squareup.kotlinpoet.*
import dev.weiland.reinhardt.gen.*

internal class ForeignKeyFieldCodegen(
    val model: CodegenModel,
    val field: CodegenField,
    val relatedFieldModel: ClassName,
    val nullable: Boolean
) : FieldCodegen {
    override fun generate(ctx: FieldGenContext) {
        val relatedEntityReader = CodegenConstants.getEntityReaderClassName(relatedFieldModel)
        val relatedEntity = CodegenConstants.getEntityInterfaceClassName(relatedFieldModel)
        ctx.entityInterface.addProperty(
            PropertySpec.builder(field.name, relatedEntity).build()
        )
        ctx.entityClass.addProperty(
            PropertySpec.builder(field.name, relatedEntity)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(field.name)
                .build()
        )
        ctx.entityClassConstructor.addParameter(
            ParameterSpec(field.name, relatedEntity)
        )
        ctx.entityReaderReadNullableFun.addCode(
            "val %N = %T.%N(row, columnPrefix + %S)\n",
            field.name, relatedEntityReader, if (nullable) "readEntityNullable" else "readEntity", field.name + "_",
        )
        ctx.entityClassCallParams += CodeBlock.of("%N", field.name)
    }
}