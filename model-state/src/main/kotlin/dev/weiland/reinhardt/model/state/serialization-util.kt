package dev.weiland.reinhardt.model.state

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind

internal fun deferDescriptor(deferred: () -> SerialDescriptor): SerialDescriptor {
    return object : SerialDescriptor {
        private val delegate by lazy(deferred)

        @ExperimentalSerializationApi
        override val annotations: List<Annotation>
            get() = delegate.annotations

        @ExperimentalSerializationApi
        override val elementsCount: Int
            get() = delegate.elementsCount

        @ExperimentalSerializationApi
        override val isInline: Boolean
            get() = delegate.isInline

        @ExperimentalSerializationApi
        override val isNullable: Boolean
            get() = delegate.isNullable

        @ExperimentalSerializationApi
        override val kind: SerialKind
            get() = delegate.kind

        @ExperimentalSerializationApi
        override val serialName: String
            get() = delegate.serialName

        @ExperimentalSerializationApi
        override fun getElementAnnotations(index: Int): List<Annotation> {
            return delegate.getElementAnnotations(index)
        }

        @ExperimentalSerializationApi
        override fun getElementDescriptor(index: Int): SerialDescriptor {
            return delegate.getElementDescriptor(index)
        }

        @ExperimentalSerializationApi
        override fun getElementIndex(name: String): Int {
            return delegate.getElementIndex(name)
        }

        @ExperimentalSerializationApi
        override fun getElementName(index: Int): String {
            return delegate.getElementName(index)
        }

        @ExperimentalSerializationApi
        override fun isElementOptional(index: Int): Boolean {
            return delegate.isElementOptional(index)
        }
    }
}