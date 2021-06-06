package dev.weiland.reinhardt.gen.field

import com.squareup.kotlinpoet.*
import dev.weiland.reinhardt.gen.*

public class BasicFieldCodegen(
    private val model: CodegenModel,
    private val field: CodegenField,
    private val basicFieldContentType: TypeName,
    ) : FieldCodegen {

    override fun generate(ctx: FieldGenContext) {
        val basicFieldType = basicFieldContentType
        ctx.entityInterface.addProperty(
            PropertySpec.builder(field.name, basicFieldType).build()
        )
        ctx.entityClass.addProperty(
            PropertySpec.builder(field.name, basicFieldType)
                .addModifiers(KModifier.OVERRIDE)
                .initializer(field.name)
                .build()
        )
        ctx.entityClassConstructor.addParameter(
            ParameterSpec(field.name, basicFieldType)
        )
        if (field.isPrimaryKey) {
            ctx.entityReaderReadNullableFun.addCode(
                "val %N = %T.%N.fromDbNullable(row, columnPrefix + %S) ?: return null\n",
                field.name, model.className, field.name, field.name
            )
        } else {
            ctx.entityReaderReadNullableFun.addCode(
                "val %N = %T.%N.fromDb(row, columnPrefix + %S)\n",
                field.name, model.className, field.name, field.name
            )
        }
        ctx.entityClassCallParams += CodeBlock.of("%N", field.name)
    }

}