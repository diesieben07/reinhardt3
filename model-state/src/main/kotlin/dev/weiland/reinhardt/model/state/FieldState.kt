@file:UseSerializers(ImmutableKmTypeSerializer::class)
package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.metadata.ImmutableKmType
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
public data class FieldState(
    public val name: String,
    public val fieldType: ImmutableKmType,
)

