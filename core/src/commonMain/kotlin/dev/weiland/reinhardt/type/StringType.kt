package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.BindingTarget
import dev.weiland.reinhardt.db.DbRow

public object StringType : WritableColumnType<String> {

    override fun getNullable(row: DbRow, column: String): String? {
        return row.getString(column)
    }

    override fun bind(target: BindingTarget, column: String, value: String?) {
        target.bind(column, value)
    }

}