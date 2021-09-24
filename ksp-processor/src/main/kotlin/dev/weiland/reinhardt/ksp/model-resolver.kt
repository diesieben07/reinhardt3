package dev.weiland.reinhardt.ksp

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import dev.weiland.reinhardt.constants.KnownNames
import dev.weiland.reinhardt.model.BasicFieldInfo
import dev.weiland.reinhardt.model.FieldInfo
import dev.weiland.reinhardt.model.ModelInfo

internal class ModelInfoFromKspResolver(
    private val resolver: Resolver
) {

    private val fieldClass = requireNotNull(resolver.getClassDeclarationByName(KnownNames.FIELD_CLASS_NAME.canonicalName))
    private val fieldStarType = fieldClass.asStarProjectedType()

    private val basicFieldClass = requireNotNull(resolver.getClassDeclarationByName(KnownNames.BASIC_FIELD_CLASS_NAME.canonicalName))
    private val basicFieldStarType = basicFieldClass.asStarProjectedType()
    private val basicFieldFromDbFun = requireNotNull(
        basicFieldClass.getDeclaredFunctions().single { it.simpleName.asString() == KnownNames.BASIC_FIELD_FROM_DB_FUN }
    )

    internal fun makeModelInfoFromKsp(ksp: KSClassDeclaration): ModelInfo {
        return ModelInfo(
            requireNotNull(ksp.qualifiedName).toKotlinPoetClassName(),
            ksp.getDeclaredProperties().mapNotNull { makeFieldInfo(it) }.toList()
        )
    }

    private fun makeFieldInfo(kspProperty: KSPropertyDeclaration): FieldInfo? {
        val resolvedFieldType = kspProperty.type.resolve()
        if (!fieldStarType.isAssignableFrom(resolvedFieldType)) return null

        if (basicFieldStarType.isAssignableFrom(resolvedFieldType)) {
            val fieldReturnType = requireNotNull(basicFieldFromDbFun.asMemberOf(resolvedFieldType).returnType)
            return BasicFieldInfo(
                kspProperty.simpleName.asString(),
                resolvedFieldType.toKotlinPoetType(),
                fieldReturnType.toKotlinPoetType()
            )
        } else {
            // TODO!!!
            return null
        }


    }
}