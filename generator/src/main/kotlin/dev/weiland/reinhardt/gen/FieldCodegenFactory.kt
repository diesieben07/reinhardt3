package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

public interface FieldCodegenFactory {

    public fun getCodeGenerator(model: CodegenModel, field: CodegenField, lookup: FieldInfoLookup): FieldCodegen?

}

public interface FieldInfoLookup {

    public fun lookupSupertype(rawClass: ClassName): TypeName?
    public fun isSubtypeOf(rawClass: ClassName): Boolean

}