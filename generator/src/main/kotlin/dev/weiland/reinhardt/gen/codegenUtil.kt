package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.classFor
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmType

@KotlinPoetMetadataPreview
private fun ImmutableKmType.replaceTypeParams(map: Map<Int, ImmutableKmType?>): ImmutableKmType? {
    val classifier = classifier
    if (classifier is KmClassifier.TypeParameter) {
        // type parameters don't have args
        return map[classifier.id]?.toMutable()?.let { result ->
            result.flags = result.flags or this.flags
            result.toImmutable()
        }
    } else {
        val result = KmType(flags)
        result.classifier = classifier
        arguments.mapTo(result.arguments) { arg ->
            arg.toMutable().also { result ->
                result.type = result.type?.toImmutable()?.replaceTypeParams(map)?.toMutable()
            }
        }
        return result.toImmutable()
    }
}

@KotlinPoetMetadataPreview
private fun ImmutableKmClass.superClassType(arguments: List<ImmutableKmTypeProjection>): ImmutableKmType? {
    require(isClass || isObject) { "Must be a class" }
    val superClassType = supertypes.first()
    val superClassClassifier = superClassType.classifier
    check(superClassClassifier is KmClassifier.Class) { "SuperClass is not a class" }
    return if (superClassClassifier.name == "kotlin/Any") {
        null
    } else {
        val paramMap = HashMap<Int, ImmutableKmType?>()
        check(arguments.size == typeParameters.size) { "TypeParameter size mismatch" }
        for ((index, typeParameter) in typeParameters.withIndex()) {
            val arg = arguments[index]
            paramMap[typeParameter.id] = arg.type
        }
        return superClassType.replaceTypeParams(paramMap)
    }
}

@KotlinPoetMetadataPreview
public fun ImmutableKmType.walkSuperClassTypes(classInspector: ClassInspector): Sequence<ImmutableKmType> {
    return sequence {
        var current: ImmutableKmType? = this@walkSuperClassTypes
        while (current != null) {
            yield(current)
            val currentClassifier = current.classifier
            // TODO: Support TypeParameters if necessary?
            if (currentClassifier !is KmClassifier.Class) {
                return@sequence
            }
            val currentClass = classInspector.classFor(ClassInspectorUtil.createClassName(currentClassifier.name))
            current = currentClass.superClassType(current.arguments)
        }
    }
}