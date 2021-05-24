package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.DbRow

public interface ColumnType<T> {

    public fun get(row: DbRow, column: String): T {
        return checkNotNull(getNullable(row, column)) {
            "Column $column of type $this was null unexpectedly"
        }
    }

    public fun getNullable(row: DbRow, column: String): T?

}