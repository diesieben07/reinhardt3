@file:OptIn(ExperimentalSerializationApi::class)
package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE


//internal object TypeNameSerializer : KSerializer<TypeName> {
//
//}
//
//internal object ParameterizedTypeNameSerializer : KSerializer<ParameterizedTypeName> {
//
//    private val typeNameListSerializer = ListSerializer(TypeNameSerializer)
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ParameterizedTypeName") {
//        element("rawType", ClassNameSerializer.descriptor)
//        element("typeArguments", typeNameListSerializer.descriptor)
//        element("nullable", Boolean.serializer().descriptor)
//    }
//
//    override fun serialize(encoder: Encoder, value: ParameterizedTypeName) {
//        require(value)
//        encoder.encodeStructure(descriptor) {
//            encodeSerializableElement(descriptor, 0, ClassNameSerializer, value.rawType)
//        }
//    }
//
//}
//
//internal object ClassNameSerializer : KSerializer<ClassName> {
//
//    private val stringListSerializer = ListSerializer(String.serializer())
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ClassName") {
//        element("packageName", String.serializer().descriptor)
//        element("simpleNames", stringListSerializer.descriptor)
//        element("nullable", Boolean.serializer().descriptor)
//    }
//
//    override fun serialize(encoder: Encoder, value: ClassName) {
//        require(value.annotations.isEmpty()) {
//            "ClassName with annotations not supported"
//        }
//        require(value.tags.isEmpty()) {
//            "ClassName with tags not supported"
//        }
//        encoder.encodeStructure(descriptor) {
//            encodeStringElement(descriptor, 0, value.packageName)
//            encodeSerializableElement(descriptor, 1, stringListSerializer, value.simpleNames)
//            encodeBooleanElement(descriptor, 2, value.isNullable)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): ClassName {
//        return decoder.decodeStructure(descriptor) {
//            var packageName: String? = null
//            var simpleNames: List<String>? = null
//            while (true) {
//                when (decodeElementIndex(descriptor)) {
//                    0 -> packageName = decodeStringElement(descriptor, 0)
//                    1 -> simpleNames = decodeSerializableElement(descriptor, 1, stringListSerializer)
//                    DECODE_DONE -> break
//                }
//            }
//            ClassName(
//                checkNotNull(packageName),
//                checkNotNull(simpleNames)
//            )
//        }
//    }
//}