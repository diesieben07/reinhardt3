package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.TypeName

public interface FieldCodegen {

    public val primaryKeyType: TypeName?
        get() = null

    public val info: CodegenField

    public fun generate(ctx: FieldGenContext)

}