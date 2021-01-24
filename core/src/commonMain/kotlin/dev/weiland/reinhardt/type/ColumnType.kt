package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.ResultRow
import kotlin.reflect.KType

public interface ColumnType<T> {

    public fun get(row: ResultRow, column: String): T {
        return checkNotNull(getNullable(row, column)) {
            "Column $column of type $this was null unexpectedly"
        }
    }

    public fun getNullable(row: ResultRow, column: String): T?

}