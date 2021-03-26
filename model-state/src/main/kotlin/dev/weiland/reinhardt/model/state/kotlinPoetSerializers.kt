@file:OptIn(ExperimentalSerializationApi::class)
package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE

//@Serializer(forClass = CodeBlock::class)
//internal object CodeBlockSerializer : KSerializer<CodeBlock> {
//
//}
//
//@Serializer(forClass = KModifier::class)
//internal object KModifierSerializer
//
//internal object ParameterSpecSerializer : KSerializer<ParameterSpec> {
//    private val annotationSpecListSerializer = ListSerializer(AnnotationSpecSerializer)
//    private val kModifierSetSerializer = SetSerializer(KModifierSerializer)
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ParameterSpec") {
//        element("name", String.serializer().descriptor)
//        element("type", TypeNameSerializer.descriptor)
//        element("annotations", annotationSpecListSerializer.descriptor)
//        element("modifiers", kModifierSetSerializer.descriptor)
//    }
//
//    override fun serialize(encoder: Encoder, value: ParameterSpec) {
//        encoder.encodeStructure(descriptor) {
//            encodeStringElement(descriptor, 0, value.name)
//            encodeSerializableElement(descriptor, 1, TypeNameSerializer, value.type)
//            encodeSerializableElement(descriptor, 2, annotationSpecListSerializer, value.annotations)
//            encodeSerializableElement(descriptor, 3, kModifierSetSerializer, value.modifiers)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): ParameterSpec {
//        TODO("Not yet implemented")
//    }
//}
//
//@Serializer(forClass = TypeName::class)
//internal object TypeNameSerializer
//
//internal object LambdaTypeNameSerializer : KSerializer<LambdaTypeName> {
//
//    private val parameterSpecListSerializer = ListSerializer(ParameterSpecSerializer)
//    private val annotationSpecListSerializer = ListSerializer(AnnotationSpecSerializer)
//    private val nullableTypeNameSerializer = TypeNameSerializer.nullable
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("LambdaTypeName") {
//        element("receiver", nullableTypeNameSerializer.descriptor)
//        element("parameters", parameterSpecListSerializer.descriptor)
//        element("returnType", TypeNameSerializer.descriptor)
//        element("nullable", Boolean.serializer().descriptor)
//        element("suspending", Boolean.serializer().descriptor)
//        element("annotations", annotationSpecListSerializer.descriptor)
//    }
//
//    override fun serialize(encoder: Encoder, value: LambdaTypeName) {
//        encoder.encodeStructure(descriptor) {
//            encodeSerializableElement(descriptor, 0, nullableTypeNameSerializer, value.receiver)
//            encodeSerializableElement(descriptor, 1, parameterSpecListSerializer, value.parameters)
//            encodeSerializableElement(descriptor, 2, TypeNameSerializer, value.returnType)
//            encodeBooleanElement(descriptor, 3, value.isNullable)
//            encodeBooleanElement(descriptor, 4, value.isSuspending)
//            encodeSerializableElement(descriptor, 5, annotationSpecListSerializer, value.annotations)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): LambdaTypeName {
//        decoder.decodeStructure(descriptor) {
//            while (true) {
//                var receiver: TypeName? = null
//                var parameters: List<ParameterSpec>? = null
//                when (val index = decodeElementIndex(descriptor)) {
//                    0 -> receiver = decodeSerializableElement(descriptor, 0, nullableTypeNameSerializer)
//                    1 -> parameters = decodeSerializableElement(descriptor, 1, parameterSpecListSerializer)
//
//                }
//            }
//        }
//    }
//}
//
//internal object AnnotationSpecSerializer : KSerializer<AnnotationSpec> {
//    override val descriptor: SerialDescriptor
//        get() = TODO("Not yet implemented")
//
//    override fun serialize(encoder: Encoder, value: AnnotationSpec) {
//        TODO("Not yet implemented")
//    }
//
//    override fun deserialize(decoder: Decoder): AnnotationSpec {
//        TODO("Not yet implemented")
//    }
//}

internal object ClassNameSerializer : KSerializer<ClassName> {

    private val stringListSerializer = ListSerializer(String.serializer())

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ClassName") {
        element("packageName", String.serializer().descriptor)
        element("simpleNames", stringListSerializer.descriptor)
    }

    override fun serialize(encoder: Encoder, value: ClassName) {
        require(!value.isNullable) {
            "Nullable ClassName not supported"
        }
        require(value.annotations.isEmpty()) {
            "ClassName with annotations not supported"
        }
        require(value.tags.isEmpty()) {
            "ClassName with tags not supported"
        }
        encoder.encodeStructure(descriptor) {
            encodeStringElement(descriptor, 0, value.packageName)
            encodeSerializableElement(descriptor, 1, stringListSerializer, value.simpleNames)
        }
    }

    override fun deserialize(decoder: Decoder): ClassName {
        return decoder.decodeStructure(descriptor) {
            var packageName: String? = null
            var simpleNames: List<String>? = null
            while (true) {
                when (decodeElementIndex(descriptor)) {
                    0 -> packageName = decodeStringElement(descriptor, 0)
                    1 -> simpleNames = decodeSerializableElement(descriptor, 1, stringListSerializer)
                    DECODE_DONE -> break
                }
            }
            ClassName(
                checkNotNull(packageName),
                checkNotNull(simpleNames)
            )
        }
    }
}