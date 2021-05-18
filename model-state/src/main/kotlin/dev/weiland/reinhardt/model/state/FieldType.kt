package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.ClassName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

//@Serializable
public data class FieldType(
    public val fieldClass: ClassName
)