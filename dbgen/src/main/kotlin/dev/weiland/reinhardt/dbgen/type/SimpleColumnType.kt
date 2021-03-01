package dev.weiland.reinhardt.dbgen.type

import com.squareup.kotlinpoet.*
import dev.weiland.reinhardt.type.ArrayType
import dev.weiland.reinhardt.type.ColumnType
import dev.weiland.reinhardt.type.StringType
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType
import kotlin.reflect.typeOf

interface CodegenColumnType {

    val runtimeType: KType
    fun getInitializer(companionObject: TypeName, columnName: String): CodeBlock
    fun contributeToCompanionObject(columnName: String, companionObject: TypeSpec.Builder, companionObjectName: TypeName) = Unit

}

data class SimpleColumnType(val columnType: ColumnType<*>, override val runtimeType: KType) : CodegenColumnType {

    init {
        require(columnType::class.objectInstance != null)
    }

    override fun getInitializer(companionObject: TypeName, columnName: String): CodeBlock {
        return CodeBlock.of("%T", columnType::class)
    }

}

data class ArrayColumnType(val elementType: CodegenColumnType) : CodegenColumnType {

    override val runtimeType: KType
        get() = Array::class.createType(listOf(KTypeProjection(KVariance.INVARIANT, elementType.runtimeType)))

    override fun getInitializer(companionObject: TypeName, columnName: String): CodeBlock {
        return CodeBlock.of("%T.%N", companionObject, "${columnName}\$\$type")
    }

    override fun contributeToCompanionObject(columnName: String, companionObject: TypeSpec.Builder, companionObjectName: TypeName) {
        val fakeElementName = "\$\$${columnName}\$\$ElementType"
        elementType.contributeToCompanionObject(fakeElementName, companionObject, companionObjectName)
        val arrayType = ArrayType::class.createType(listOf(KTypeProjection.invariant(elementType.runtimeType)))
        val initializer = CodeBlock.builder()
            .add("%T(", ArrayType::class)
            .add(elementType.getInitializer(companionObjectName, fakeElementName))
            .add(")")
            .build()

        companionObject.addProperty(
            PropertySpec.builder("${columnName}\$\$type", arrayType.asTypeName())
                .initializer(initializer)
                .build()
        )
    }
}

fun codegenType(kotlinType: KType): CodegenColumnType {
    when {
        kotlinType.classifier == Array::class -> {
            return ArrayColumnType(codegenType(kotlinType.arguments.single().type ?: error("illegal array type")))
        }
        kotlinType.classifier == String::class -> {
            return SimpleColumnType(StringType, kotlinType)
        }
        else -> TODO()
    }
}
inline fun <reified T> codegenType() = codegenType(typeOf<T>())