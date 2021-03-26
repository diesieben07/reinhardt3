package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.metadata.ImmutableKmType
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview

@KotlinPoetMetadataPreview
public data class FieldState(
    public val name: String,
    public val fieldType: ImmutableKmType,
)

