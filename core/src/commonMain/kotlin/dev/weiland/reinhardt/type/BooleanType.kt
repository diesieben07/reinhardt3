package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.BindingTarget
import dev.weiland.reinhardt.db.DbRow

public object BooleanType : WritableColumnType<Boolean> {
    override fun getNullable(row: DbRow, column: String): Boolean? {
        val value = row.getBoolean(column)
        return if (row.wasNull(column)) null else value
    }

    override fun bind(target: BindingTarget, column: String, value: Boolean?) {
        target.bind(column, value)
    }

}