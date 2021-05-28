package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

public data class CodegenModel(
    val className: ClassName,
    val fields: List<CodegenField>
)

public data class CodegenField(
    val name: String,
    val entityProperties: List<CodegenEntityProperty>,
)


public data class CodegenEntityProperty(
    val name: String,
    val type: TypeName
)