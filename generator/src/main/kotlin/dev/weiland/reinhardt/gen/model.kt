package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

public data class CodegenModel(
    val className: ClassName,
)

public data class CodegenField(
    val name: String,
    val isPrimaryKey: Boolean,
    val declaredType: TypeName,
)


public data class CodegenEntityProperty(
    val name: String,
    val type: TypeName
)