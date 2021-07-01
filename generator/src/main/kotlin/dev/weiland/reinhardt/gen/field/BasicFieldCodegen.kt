package dev.weiland.reinhardt.gen.field

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.weiland.reinhardt.constants.KnownNames
import dev.weiland.reinhardt.gen.*

public class BasicFieldCodegen(
    private val model: CodegenModel,
    private val field: CodegenField,
    private val basicFieldContentType: TypeName,
) : FieldCodegen {

    override val info: CodegenField
        get() = this.field
    override val primaryKeyType: TypeName?
        get() = if (this.field.isPrimaryKey) basicFieldContentType else null

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

            val entityReaderReadPKNullableFun = checkNotNull(ctx.entityReaderReadPKNullableFun)
            entityReaderReadPKNullableFun.addCode(
                "return %T.%N.fromDbNullable(row, columnPrefix + %S)",
                model.className, field.name, field.name
            )
        } else {
            ctx.entityReaderReadNullableFun.addCode(
                "val %N = %T.%N.fromDb(row, columnPrefix + %S)\n",
                field.name, model.className, field.name, field.name
            )
        }
        ctx.entityClassCallParams += CodeBlock.of("%N", field.name)

        ctx.modelExpressionContainerClass?.let { modelExpressionContainerClass ->
            val fieldExpressionType = KnownNames.FIELD_EXPRESSION_CLASS_NAME.parameterizedBy(
                model.className,
                basicFieldContentType
            )
            modelExpressionContainerClass.addProperty(
                PropertySpec.builder(field.name, fieldExpressionType)
                    .initializer("%T(%T, this.%N, %T.%N)", fieldExpressionType, ctx.entityCompanionName, "_alias", model.className, field.name)
                    .build()
            )
        }
    }

}