package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.ResultRow

public object StringType : ColumnType<String> {

    override fun getNullable(row: ResultRow, column: String): String? {
        return row.getString(column)
    }
}