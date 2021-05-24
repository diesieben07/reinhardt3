package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.DbRow

public object IntType : ColumnType<Int> {
    override fun getNullable(row: DbRow, column: String): Int? {
        val value = row.getInt(column)
        return if (row.wasNull(column)) null else value
    }
}