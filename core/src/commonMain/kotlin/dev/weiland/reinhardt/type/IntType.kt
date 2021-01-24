package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.ResultRow

public object IntType : ColumnType<Int> {
    override fun getNullable(row: ResultRow, column: String): Int? {
        TODO("Not yet implemented")
    }
}