package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.DbRow

public class ArrayType<T>(public val base: ColumnType<T>) : ColumnType<List<T>> {

    override fun getNullable(row: DbRow, column: String): List<T>? {
        TODO("Not yet implemented")
    }
}