package dev.weiland.reinhardt.processor

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.metadata.ImmutableKmProperty
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import io.github.encryptorcode.pluralize.Pluralize
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.*

internal typealias AsmType = org.objectweb.asm.Type
internal typealias KmClassName = kotlinx.metadata.ClassName

internal fun AnnotationValue.unpack(): Any? {
    val value = value
    return if (value is List<*>) {
        value.map { (it as AnnotationValue).unpack() }
    } else {
        value
    }
}

internal fun AnnotationMirror.getValue(key: String): Any? {
    return elementValues.entries.find { it.key.simpleName.contentEquals(key) }?.value?.unpack()
}

internal fun TypeElement.readMetadata(): KotlinClassMetadata? {
    val metadata = annotationMirrors.find {
        (it.annotationType.asElement() as? TypeElement)?.qualifiedName?.contentEquals("kotlin.Metadata") ?: false
    } ?: return null

    @Suppress("UNCHECKED_CAST")
    val header = KotlinClassHeader(
        kind = metadata.getValue("k") as Int,
        metadataVersion = (metadata.getValue("mv") as List<Int>?)?.toIntArray(),
        bytecodeVersion = (metadata.getValue("bv") as List<Int>?)?.toIntArray(),
        data1 = (metadata.getValue("d1") as List<String>?)?.toTypedArray(),
        data2 = (metadata.getValue("d2") as List<String>?)?.toTypedArray(),
        extraString = metadata.getValue("xs") as String?,
        packageName = metadata.getValue("pn") as String?,
        extraInt = metadata.getValue("xi") as Int?
    )
    return KotlinClassMetadata.read(header)
}

internal fun AsmType.fixedElementType(): AsmType {
    require(sort == AsmType.ARRAY)
    return AsmType.getType(descriptor.substring(1))
}

internal fun ClassName.toKmClassName(): KmClassName {
    return buildString {
        append(packageName.replace('.', '/'))
        if (packageName.isNotEmpty()) append('/')
        simpleNames.joinTo(this, separator = "$")
    }
}

fun String.pluralizeEnglish(): String {
    return Pluralize.pluralize(this)
}

internal fun ImmutableKmProperty.findElement(cls: TypeElement): Element {
    return cls.enclosedElements.filterIsInstance<ExecutableElement>()
        .firstOrNull() { it.simpleName.toString() == this.getterSignature?.name } ?: cls
}