package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.TypeSpec

public interface FieldCodegen {

    public fun generateRef(classBuilder: TypeSpec.Builder)

}