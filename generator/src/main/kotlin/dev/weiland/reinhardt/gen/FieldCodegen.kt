package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.TypeSpec

public interface FieldCodegen {

    public fun generateEntityInterface(typeBuilder: TypeSpec.Builder)

}