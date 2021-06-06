package dev.weiland.reinhardt.gen.field

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.ClassName
import dev.weiland.reinhardt.gen.*

@AutoService(FieldCodegenFactory::class)
internal class BuiltinFieldGenFactory : FieldCodegenFactory {

    private val basicFieldClassName = ClassName("dev.weiland.reinhardt.model", "BasicField")
    private val foreignKeyClassName = ClassName("dev.weiland.reinhardt.model", "ForeignKey")
    private val nullableForeignKeyClassName = ClassName("dev.weiland.reinhardt.model", "NullableForeignKey")
    private val relatedFieldClassName = ClassName("dev.weiland.reinhardt.model", "RelationField")
    private val eagerClassName = ClassName("dev.weiland.reinhardt.model", "Eager")

    override fun getCodeGenerator(model: CodegenModel, field: CodegenField, lookup: FieldInfoLookup): FieldCodegen? {
        // TODO: log warnings in case of missing args, etc.
        val basicFieldContentType = lookup.lookupFunctionReturnType(basicFieldClassName, "fromDb")
        if (basicFieldContentType != null) {
            return BasicFieldCodegen(model, field, basicFieldContentType)
        }
        val relatedFieldModel = lookup.lookupPropertyType(relatedFieldClassName, "referencedModel")
        if (relatedFieldModel is ClassName) {
            when {
                lookup.isSubtypeOf(foreignKeyClassName) -> return ForeignKeyFieldCodegen(model, field, relatedFieldModel, false, lookup.hasAnnotation(eagerClassName))
                lookup.isSubtypeOf(nullableForeignKeyClassName) -> return ForeignKeyFieldCodegen(model, field, relatedFieldModel, true, lookup.hasAnnotation(eagerClassName))
            }
        }

        return null
    }
}