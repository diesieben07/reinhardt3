package dev.weiland.reinhardt.type

import dev.weiland.reinhardt.db.BindingTarget

public interface WritableColumnType<T> : ColumnType<T> {

    public fun bind(target: BindingTarget, column: String, value: T?)

}