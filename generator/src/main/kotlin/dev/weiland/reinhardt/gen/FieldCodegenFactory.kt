package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

public interface FieldCodegenFactory {

    public fun getCodeGenerator(model: CodegenModel, field: CodegenField, lookup: FieldInfoLookup): FieldCodegen?

}

public interface FieldInfoLookup {

    public fun lookupPropertyType(propertyClassName: ClassName, propertyName: String): TypeName?
    public fun lookupFunctionReturnType(functionClassName: ClassName, functionName: String): TypeName?
    public fun isSubtypeOf(rawClass: ClassName): Boolean

}