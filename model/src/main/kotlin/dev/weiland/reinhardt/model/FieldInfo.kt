package dev.weiland.reinhardt.model

import com.squareup.kotlinpoet.TypeName

public sealed interface FieldInfo {
    public val propertyName: String
    public val propertyType: TypeName
}

public data class BasicFieldInfo(
    override val propertyName: String,
    override val propertyType: TypeName,
    val resolvedFieldType: TypeName
) : FieldInfo


