package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmVariance

internal fun ImmutableKmType.toTypeName(): TypeName {
    val classifier = requireNotNull(classifier as? KmClassifier.Class) {
        "Only KmClassifier.Class is supported"
    }
    require(outerType == null) {
        "Inner classes are not supported"
    }
    require(flexibleTypeUpperBound == null) {
        "Flexible types are not supported"
    }
    require(!isRaw) {
        "Raw types are not supported"
    }
    val className = ClassInspectorUtil.createClassName(classifier.name)
    return if (arguments.isEmpty()) {
        className
    } else {
        className.parameterizedBy(
            arguments.map { projection -> projection.toTypeName() }
        )
    }
}

internal fun ImmutableKmTypeProjection.toTypeName(): TypeName {
    val typeName = type?.toTypeName() ?: return STAR
    return when (variance) {
        null -> STAR
        KmVariance.INVARIANT -> typeName
        KmVariance.IN -> WildcardTypeName.consumerOf(typeName)
        KmVariance.OUT -> WildcardTypeName.producerOf(typeName)
    }
}

@KotlinPoetMetadataPreview
internal fun ImmutableKmClass.superClassName(): ClassName? {
    val superClass = supertypes.firstOrNull()?.classifier as? KmClassifier.Class ?: return null
    return if (superClass.name == "kotlin/Any") {
        null
    } else {
        ClassInspectorUtil.createClassName(superClass.name)
    }
}

@KotlinPoetMetadataPreview
internal fun ClassInspector.isSubclass(
    subType: TypeName, superclass: ClassName
): Boolean {
    val className = when (subType) {
        is ClassName -> subType
        is ParameterizedTypeName -> subType.rawType
        is TypeVariableName -> TODO("NYI")
        is WildcardTypeName -> TODO("NYI")
        is LambdaTypeName, is Dynamic -> return false
    }
    var current = className
    do {
        if (current == superclass) {
            return true
        }
        val cls = getClassOrNull(current) ?: return false
        current = cls.superClassName() ?: return false
    } while (true)
}

@KotlinPoetMetadataPreview
public fun ClassInspector.getClassOrNull(className: ClassName): ImmutableKmClass? {
    return getContainerOrNull(className) as? ImmutableKmClass
}

@KotlinPoetMetadataPreview
internal fun ClassInspector.getContainerOrNull(className: ClassName): ImmutableKmDeclarationContainer? {
    return try {
        declarationContainerFor(className)
    } catch (e: IllegalStateException) {
        return null
    } catch (e: NotImplementedError) {
        return null
    }
}