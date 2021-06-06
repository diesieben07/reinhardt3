package dev.weiland.reinhardt.gen.field

import dev.weiland.reinhardt.gen.*
import java.util.*

public object FieldCodegenFactories : FieldCodegenFactory {

    // TODO: ordering?
    private val factories by lazy {
        ServiceLoader.load(FieldCodegenFactory::class.java, FieldCodegenFactory::class.java.classLoader).toList()
    }

    override fun getCodeGenerator(model: CodegenModel, field: CodegenField, lookup: FieldInfoLookup): FieldCodegen? {
        for (factory in factories) {
            val r = factory.getCodeGenerator(model, field, lookup)
            if (r != null) {
                return r
            }
        }
        return null
    }
}