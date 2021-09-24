package dev.weiland.reinhardt.model

import com.squareup.kotlinpoet.ClassName

public data class ModelInfo(
    val className: ClassName,
    val fields: List<FieldInfo>
)