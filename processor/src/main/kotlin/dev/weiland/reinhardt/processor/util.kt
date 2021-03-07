package dev.weiland.reinhardt.processor

import kotlinx.metadata.ClassName
import kotlinx.metadata.jvm.JvmMethodSignature
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import org.atteo.evo.inflector.English
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.AnnotationValue
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.*
import javax.lang.model.util.Elements

internal typealias AsmType = org.objectweb.asm.Type

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

internal val ClassName.packageName: String
    get() {
        return substringBeforeLast('/', missingDelimiterValue = "").replace('/', '.')
    }

internal val ClassName.className: String
    get() {
        return substringAfterLast('/')
    }

fun String.pluralizeEnglish(): String = when (this) {
    "person" -> "people"
    else -> English.plural(this)
}