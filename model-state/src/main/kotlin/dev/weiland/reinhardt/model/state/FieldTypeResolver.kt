package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.TypeName

public interface FieldTypeResolver {

    public fun isFieldType(type: TypeName): Boolean
    public fun resolveFieldTypeInfo(fieldType: TypeName): FieldTypeInfo

}