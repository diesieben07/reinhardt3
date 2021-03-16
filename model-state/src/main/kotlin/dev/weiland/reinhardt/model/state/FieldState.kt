package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.TypeName

public data class FieldState(
    public val name: String,
    public val fieldType: TypeName,
)