package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

public interface FieldTypeResolver {

    public fun isFieldType(type: TypeName): Boolean
    @KotlinPoetMetadataPreview
    public fun isFieldType(type: ImmutableKmType): Boolean
    public fun resolveFieldTypeInfo(fieldType: TypeName): FieldTypeInfo

}