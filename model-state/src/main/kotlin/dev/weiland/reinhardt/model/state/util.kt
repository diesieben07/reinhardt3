package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.KmClassifier

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
        val cls = declarationContainerFor(current) as? ImmutableKmClass ?: return false
        current = cls.superClassName() ?: return false
    } while (true)
}