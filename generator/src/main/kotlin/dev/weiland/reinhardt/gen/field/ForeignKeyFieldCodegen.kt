package dev.weiland.reinhardt.gen.field

import com.squareup.kotlinpoet.*
import dev.weiland.reinhardt.gen.*

internal class ForeignKeyFieldCodegen(
    val model: CodegenModel,
    val field: CodegenField,
    val relatedFieldModel: ClassName,
    val relatedPrimaryKeyInfo: PrimaryKeyInfo,
    val nullable: Boolean,
    val eager: Boolean
) : FieldCodegen {
    override fun generate(ctx: FieldGenContext) {
        val relatedEntityReader = CodegenConstants.getEntityReaderClassName(relatedFieldModel)
        val relatedEntity = CodegenConstants.getEntityInterfaceClassName(relatedFieldModel)
        val relEntityMaybeNull = relatedEntity.copy(nullable = nullable)
        val relatedFieldContentTypeMaybeNull = relatedPrimaryKeyInfo.contentType.copy(nullable = nullable)
        val idFieldName = CodegenConstants.getIdFieldName(model.className, relatedFieldModel, field.name)

        ctx.entityInterface.addProperty(
            PropertySpec.builder(idFieldName, relatedFieldContentTypeMaybeNull).build()
        )

        if (eager) {
            ctx.entityClass.addProperty(
                PropertySpec.builder(idFieldName, relatedFieldContentTypeMaybeNull)
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(
                        FunSpec.getterBuilder()
                            .addCode("return this.%N${if (nullable) "?." else "."}%N", field.name, relatedPrimaryKeyInfo.name)
                            .build()
                    )
                    .build()
            )

            ctx.entityInterface.addProperty(
                PropertySpec.builder(field.name, relEntityMaybeNull).build()
            )

            ctx.entityClass.addProperty(
                PropertySpec.builder(field.name, relEntityMaybeNull)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(field.name)
                    .build()
            )
            ctx.entityClassConstructor.addParameter(
                ParameterSpec(field.name, relEntityMaybeNull)
            )
            ctx.entityReaderReadNullableFun.addCode(
                "val %N = %T.%N(row, columnPrefix + %S)\n",
                field.name, relatedEntityReader, if (nullable) "readEntityNullable" else "readEntity", field.name + "_",
            )
            ctx.entityClassCallParams += CodeBlock.of("%N", field.name)
        } else {
            ctx.entityClass.addProperty(
                PropertySpec.builder(idFieldName, relatedFieldContentTypeMaybeNull)
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer(idFieldName)
                    .build()
            )
            ctx.entityClassConstructor.addParameter(
                idFieldName, relatedFieldContentTypeMaybeNull
            )
            ctx.entityClassCallParams.add(CodeBlock.of("%M()", MemberName("kotlin", "TODO")))

            ctx.entityInterface.addFunction(
                FunSpec.builder(field.name)
                    .addModifiers(KModifier.SUSPEND, KModifier.ABSTRACT)
                    .returns(relEntityMaybeNull)
                    .build()
            )

            ctx.entityClass.addFunction(
                FunSpec.builder(field.name)
                    .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                    .returns(relEntityMaybeNull)
                    .addCode("%M()", MemberName("kotlin", "TODO"))
                    .build()
            )
        }
    }
}