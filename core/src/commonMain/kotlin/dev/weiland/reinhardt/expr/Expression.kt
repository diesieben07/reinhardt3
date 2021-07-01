package dev.weiland.reinhardt.expr

import dev.weiland.reinhardt.db.DbRow
import dev.weiland.reinhardt.type.BooleanType
import dev.weiland.reinhardt.type.ColumnType

public interface Expression<T> {

    public fun fromDb(row: DbRow, column: String): T
    public fun fromDbNullable(row: DbRow, column: String): T?

}

public interface TypeBasedExpression<T> : Expression<T> {
    public val dataType: ColumnType<T>

    override fun fromDb(row: DbRow, column: String): T = dataType.get(row, column)
    override fun fromDbNullable(row: DbRow, column: String): T? = dataType.getNullable(row, column)
}

public interface BooleanExpression : TypeBasedExpression<Boolean> {

    override val dataType: ColumnType<Boolean>
        get() = BooleanType

}