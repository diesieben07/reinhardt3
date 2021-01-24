package dev.weiland.reinhardt.dbgen.type

import com.squareup.kotlinpoet.CodeBlock
import dev.weiland.reinhardt.type.ColumnType
import kotlin.reflect.KType
import kotlin.reflect.typeOf

data class CodegenType(val columnType: ColumnType<*>, val kotlinType: KType) {

    fun getInitializer(): CodeBlock {
        return CodeBlock.of("%T", columnType::class)
    }

}

fun ColumnType<*>.forCodegen(kotlinType: KType) = CodegenType(this, kotlinType)
inline fun <reified T> ColumnType<T>.forCodegen() = forCodegen(typeOf<T>())