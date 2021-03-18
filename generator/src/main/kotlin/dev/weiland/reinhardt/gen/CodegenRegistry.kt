package dev.weiland.reinhardt.gen

import dev.weiland.reinhardt.model.state.FieldState
import dev.weiland.reinhardt.model.state.ModelState

public class CodegenRegistry {

    private val fieldGens = ArrayList<(ModelState, FieldState) -> FieldCodegen?>()

    init {
        fieldGens.add { _, field ->
            SimpleFieldCodegen(field)
        }
    }

    public fun getFieldGen(model: ModelState, field: FieldState): FieldCodegen {
        for (gen in fieldGens) {
            gen(model, field)?.let { return it }
        }
        throw IllegalArgumentException("Could not find FieldCodegen for field $field in model $model")
    }

}