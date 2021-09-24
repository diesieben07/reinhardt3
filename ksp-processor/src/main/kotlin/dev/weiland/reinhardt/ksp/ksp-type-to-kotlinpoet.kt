package dev.weiland.reinhardt.ksp

import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal fun KSType.toKotlinPoetType(): TypeName {
    if (isError) return NOTHING

    return when {
        isFunctionType -> TODO()
        isSuspendFunctionType -> TODO()
        else -> {
            // TODO: handle outer and inner types
            val baseType = when (val declaration = declaration) {
                is KSClassDeclaration -> declaration.getKotlinPoetClassName()
                else -> TODO()
            }
            if (this.arguments.isNotEmpty()) {
                baseType.parameterizedBy(arguments.map { it.toKotlinPoetType() })
            } else {
                baseType
            }
        }
    }
}

private fun KSTypeArgument.toKotlinPoetType(): TypeName {
    val type = type?.resolve()?.toKotlinPoetType() ?: return STAR
    return when (variance) {
        Variance.STAR -> STAR
        Variance.INVARIANT -> type
        Variance.COVARIANT -> WildcardTypeName.producerOf(type)
        Variance.CONTRAVARIANT -> WildcardTypeName.consumerOf(type)
    }
}

internal fun KSClassDeclaration.getKotlinPoetClassName(): ClassName {
    return checkNotNull(qualifiedName).toKotlinPoetClassName()
}

internal fun KSName.toKotlinPoetClassName(): ClassName {
    return ClassName.bestGuess(this.asString())
}