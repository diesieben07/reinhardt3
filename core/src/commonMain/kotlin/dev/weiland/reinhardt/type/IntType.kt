package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.BindingTarget
import dev.weiland.reinhardt.db.DbRow

public object IntType : WritableColumnType<Int> {
    override fun getNullable(row: DbRow, column: String): Int? {
        val value = row.getInt(column)
        return if (row.wasNull(column)) null else value
    }

    override fun bind(target: BindingTarget, column: String, value: Int?) {
        target.bind(column, value)
    }
}