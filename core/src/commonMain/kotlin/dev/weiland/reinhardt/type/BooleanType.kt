package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.DbRow

public object BooleanType : ColumnType<Boolean> {
    override fun getNullable(row: DbRow, column: String): Boolean? {
        val value = row.getBoolean(column)
        return if (row.wasNull(column)) null else value
    }
}