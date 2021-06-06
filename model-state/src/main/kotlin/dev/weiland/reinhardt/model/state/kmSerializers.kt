//@file:OptIn(ExperimentalSerializationApi::class)
//
//package dev.weiland.reinhardt.model.state
//
//import com.squareup.kotlinpoet.metadata.ImmutableKmFlexibleTypeUpperBound
//import com.squareup.kotlinpoet.metadata.ImmutableKmType
//import com.squareup.kotlinpoet.metadata.ImmutableKmTypeProjection
//import kotlinx.metadata.KmAnnotation
//import kotlinx.metadata.KmAnnotationArgument
//import kotlinx.metadata.KmClassifier
//import kotlinx.metadata.KmVariance
//import kotlinx.serialization.*
//import kotlinx.serialization.builtins.ListSerializer
//import kotlinx.serialization.builtins.MapSerializer
//import kotlinx.serialization.builtins.nullable
//import kotlinx.serialization.builtins.serializer
//import kotlinx.serialization.descriptors.*
//import kotlinx.serialization.encoding.Decoder
//import kotlinx.serialization.encoding.Encoder
//import kotlinx.serialization.encoding.decodeStructure
//import kotlinx.serialization.encoding.encodeStructure
//import kotlinx.serialization.modules.SerializersModule
//import kotlinx.serialization.modules.polymorphic
//
//internal val kmVarianceSerializer = serializer<KmVariance>()
//
//internal object ImmutableKmTypeProjectionSerializer : KSerializer<ImmutableKmTypeProjection> {
//
//    private val nullableTypeSerializer by lazy {
//        ImmutableKmTypeSerializer.nullable
//    }
//    private val nullableVarianceSerializer = kmVarianceSerializer.nullable
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ImmutableKmTypeProjection") {
//        element("variance", nullableVarianceSerializer.descriptor)
//        element("type", deferDescriptor { ImmutableKmTypeSerializer.descriptor.nullable })
//    }
//
//    override fun serialize(encoder: Encoder, value: ImmutableKmTypeProjection) {
//        encoder.encodeStructure(descriptor) {
//            encodeSerializableElement(descriptor, 0, nullableVarianceSerializer, value.variance)
//            encodeSerializableElement(descriptor, 1, nullableTypeSerializer, value.type)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): ImmutableKmTypeProjection {
//        TODO("Not yet implemented")
//    }
//}
//
//internal object ImmutableKmTypeSerializer : KSerializer<ImmutableKmType> {
//
//    private val immutableKmTypeProjectionListSerializer = ListSerializer(ImmutableKmTypeProjectionSerializer)
//    private val annotationListSerializer = ListSerializer(KmAnnotationSerializer)
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ImmutableKmType") {
//        element("flags", Int.serializer().descriptor)
//        element("classifier", PolymorphicSerializer(KmClassifier::class).descriptor)
//        element("arguments", deferDescriptor { ListSerializer(ImmutableKmTypeProjectionSerializer).descriptor })
//        element("abbreviatedType", deferDescriptor { (this@ImmutableKmTypeSerializer as KSerializer<ImmutableKmType>).descriptor.nullable })
//        element("outerType", deferDescriptor { (this@ImmutableKmTypeSerializer as KSerializer<ImmutableKmType>).descriptor.nullable })
//        element("flexibleTypeUpperBound", ImmutableKmFlexibleTypeUpperBoundSerializer.descriptor.nullable)
//        element("isRaw", Boolean.serializer().descriptor)
//        element("annotations", annotationListSerializer.descriptor)
//
//    }
//
//    override fun deserialize(decoder: Decoder): ImmutableKmType {
//        TODO("Not yet implemented")
//    }
//
//    override fun serialize(encoder: Encoder, value: ImmutableKmType) {
//        encoder.encodeStructure(descriptor) {
//            encodeSerializableElement(descriptor, 0, Int.serializer(), value.flags)
//            encodeSerializableElement(descriptor, 1, PolymorphicSerializer(KmClassifier::class), value.classifier)
//            encodeSerializableElement(descriptor, 2, ListSerializer(ImmutableKmTypeProjectionSerializer), value.arguments)
//            encodeSerializableElement(descriptor, 3, ImmutableKmTypeSerializer.nullable, value.abbreviatedType)
//            encodeSerializableElement(descriptor, 4, ImmutableKmTypeSerializer.nullable, value.outerType)
//            encodeSerializableElement(descriptor, 5, ImmutableKmFlexibleTypeUpperBoundSerializer.nullable, value.flexibleTypeUpperBound)
//            encodeSerializableElement(descriptor, 6, Boolean.serializer(), value.isRaw)
//            encodeSerializableElement(descriptor, 7, annotationListSerializer, value.annotations)
//        }
//    }
//}
//
//internal object ImmutableKmFlexibleTypeUpperBoundSerializer : KSerializer<ImmutableKmFlexibleTypeUpperBound> {
//
//    private val nullableStringSerializer = String.serializer().nullable
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ImmutableKmFlexibleTypeUpperBound") {
//        element("type", deferDescriptor { ImmutableKmTypeSerializer.descriptor })
//        element("typeFlexibilityId", nullableStringSerializer.descriptor)
//    }
//
//    override fun serialize(encoder: Encoder, value: ImmutableKmFlexibleTypeUpperBound) {
//        encoder.encodeStructure(descriptor) {
//            encodeSerializableElement(descriptor, 0, ImmutableKmTypeSerializer, value.type)
//            encodeSerializableElement(descriptor, 1, nullableStringSerializer, value.typeFlexibilityId)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): ImmutableKmFlexibleTypeUpperBound {
//        TODO("Not yet implemented")
//    }
//}
//
//internal object KmAnnotationSerializer : KSerializer<KmAnnotation> {
//
//    private val mapSerializer: KSerializer<Map<String, KmAnnotationArgument<*>>> = MapSerializer(String.serializer(), PolymorphicSerializer(KmAnnotationArgument::class))
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("KmAnnotation") {
//        element("className", PrimitiveSerialDescriptor("className", PrimitiveKind.STRING))
//        element("arguments", mapSerializer.descriptor)
//
//    }
//
//    override fun serialize(encoder: Encoder, value: KmAnnotation) {
//        encoder.encodeStructure(descriptor) {
//            encodeStringElement(descriptor, 0, value.className)
//            encodeSerializableElement(descriptor, 1, mapSerializer, value.arguments)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): KmAnnotation {
//        TODO("Not yet implemented")
//    }
//}
//
//internal object ByteValueSerializer : KSerializer<KmAnnotationArgument.ByteValue> {
//
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("KmAnnotationArgument.ByteValue") {
//        element("value", PrimitiveSerialDescriptor("value", PrimitiveKind.BYTE))
//    }
//
//    override fun serialize(encoder: Encoder, value: KmAnnotationArgument.ByteValue) {
//        encoder.encodeStructure(descriptor) {
//            encodeByteElement(descriptor, 0, value.value)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): KmAnnotationArgument.ByteValue {
//        return decoder.decodeStructure(descriptor) {
//            check(decodeElementIndex(descriptor) == 0)
//            KmAnnotationArgument.ByteValue(decodeByteElement(descriptor, 0))
//        }
//    }
//}
//
//internal object KmClassifierClassSerializer : KSerializer<KmClassifier.Class> {
//    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("KmClassifier.Class") {
//        element("name", PrimitiveSerialDescriptor("name", PrimitiveKind.STRING))
//    }
//
//    override fun serialize(encoder: Encoder, value: KmClassifier.Class) {
//        encoder.encodeStructure(descriptor) {
//            encodeStringElement(descriptor, 0, value.name)
//        }
//    }
//
//    override fun deserialize(decoder: Decoder): KmClassifier.Class {
//        decoder.decodeStructure(descriptor) {
//            val index = decodeElementIndex(descriptor)
//            check(index == 0)
//            return KmClassifier.Class(decodeStringElement(descriptor, 0))
//        }
//    }
//}
//
//public val kmSerializersModule: SerializersModule = SerializersModule {
//    polymorphic(KmAnnotationArgument::class) {
//        subclass(KmAnnotationArgument.ByteValue::class, ByteValueSerializer)
//    }
//    polymorphic(KmClassifier::class) {
//        subclass(KmClassifier.Class::class, KmClassifierClassSerializer)
//    }
//}