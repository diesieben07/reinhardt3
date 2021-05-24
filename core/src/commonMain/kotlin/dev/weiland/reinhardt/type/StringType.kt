package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.DbRow

public object StringType : ColumnType<String> {

    override fun getNullable(row: DbRow, column: String): String? {
        return row.getString(column)
    }
}