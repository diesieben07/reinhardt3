package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.ResultRow

public class ArrayType<T>(public val base: ColumnType<T>) : ColumnType<List<T>> {

    override fun getNullable(row: ResultRow, column: String): List<T>? {
        TODO("Not yet implemented")
    }
}