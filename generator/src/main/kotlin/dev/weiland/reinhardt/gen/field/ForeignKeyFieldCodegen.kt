package dev.weiland.reinhardt.gen.field

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.weiland.reinhardt.constants.KnownNames
import dev.weiland.reinhardt.gen.*

internal class ForeignKeyFieldCodegen(
    val model: CodegenModel,
    val field: CodegenField,
    val relatedFieldModel: ClassName,
    val relatedPrimaryKeyInfo: PrimaryKeyInfo,
    val nullable: Boolean,
    val eager: Boolean
) : FieldCodegen {

    private companion object {
        val coroutineDeferredClassName = ClassName("kotlinx.coroutines", "Deferred")
        val coroutineCompletableDeferredClassName = ClassName("kotlinx.coroutines", "CompletableDeferred")
        val coroutineScopeMemberName = MemberName("kotlinx.coroutines", "coroutineScope")
        val asyncMemberName = MemberName("kotlinx.coroutines", "async")
    }

    override val info: CodegenField
        get() = this.field

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
                "val %N = %T.%N().%N.%N(database, row, columnPrefix + %S)\n",
                field.name, relatedFieldModel, KnownNames.MODEL_COMPANION_FUN, KnownNames.MODEL_COMPANION_ENTITY_READER_VAL,
                if (nullable) "readEntityNullable" else "readEntity", field.name + "_",
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

            ctx.entityReaderReadNullableFun.addCode(
                "val %N = %T.%N().%N.%N(database, row, columnPrefix + %S)\n",
                field.name, relatedFieldModel, KnownNames.MODEL_COMPANION_FUN, KnownNames.MODEL_COMPANION_ENTITY_READER_VAL,
                if (nullable) "readPrimaryKeyNullable" else "readPrimaryKey", field.name + "_"
            )
            ctx.entityClassCallParams += CodeBlock.of("%N", field.name)

            ctx.entityInterface.addFunction(
                FunSpec.builder(field.name)
                    .addModifiers(KModifier.SUSPEND, KModifier.ABSTRACT)
                    .returns(relEntityMaybeNull)
                    .build()
            )

            ctx.entityClass.addProperty(
                PropertySpec.builder(field.name, coroutineDeferredClassName.parameterizedBy(relEntityMaybeNull))
                    .addModifiers(KModifier.PRIVATE)
                    .delegate(
                        """
                            %M·{
                                if (%N == null)·{
                                    %M(null)
                                }·else·{
                                    _db.%M·{
                                        _db.getEntity(%T, %N)
                                    }
                                }
                            }
                        """.trimIndent(),
                        MemberName("kotlin", "lazy"),
                        idFieldName,
                        MemberName("kotlinx.coroutines", "CompletableDeferred"),
                        asyncMemberName,
                        relatedEntityReader, idFieldName
                    )
                    .build()
            )

            val fieldLazyGetterFun = FunSpec.builder(field.name)
                .addModifiers(KModifier.SUSPEND, KModifier.OVERRIDE)
                .returns(relEntityMaybeNull)

            if (nullable) {
                fieldLazyGetterFun.addCode(
                    "if (%N == null) return null\n", idFieldName
                )
            }
            fieldLazyGetterFun.addCode(
                "return %N.await()", field.name
            )

//            fieldLazyGetterFun.addCode(
//                "val d = %N ?: _db.%M·{", field.name, asyncMemberName
//            )
//            fieldLazyGetterFun.addCode(
//                "_db.getEntity(%T, %N)", relatedEntityReader, idFieldName
//            )
//            fieldLazyGetterFun.addCode(" }.%M·{ %N = it }", MemberName("kotlin", "also"), field.name)
//            fieldLazyGetterFun.addCode("\nreturn d.await()")

            ctx.entityClass.addFunction(
                fieldLazyGetterFun
                    .build()
            )
        }
    }
}