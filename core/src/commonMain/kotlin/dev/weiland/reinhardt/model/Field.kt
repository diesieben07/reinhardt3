package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.db.DbRow
import dev.weiland.reinhardt.type.ColumnType

// fields
public sealed interface Field

public abstract class RelationField<M : Model>(public val referencedModel: M) : Field

public abstract class BasicField<T> : Field {

    public open fun nullable(): BasicField<T?> {
        return NullableWrapperField(this)
    }

    public open fun fromDb(row: DbRow, column: String): T {
        val nullableVal = fromDbNullable(row, column)
        return nullableVal ?: handleNull(row, column)
    }

    protected open fun handleNull(row: DbRow, column: String): T {
        throw IllegalStateException("Invalid null value received for field $this in column $column")
    }

    public abstract fun fromDbNullable(row: DbRow, column: String): T?

}

public abstract class TypeBasedField<T> : BasicField<T>() {

    public abstract val type: ColumnType<T>

    override fun fromDbNullable(row: DbRow, column: String): T? {
        return type.getNullable(row, column)
    }

}

public abstract class NullableField<T> : BasicField<T?>() {

    override fun nullable(): NullableField<T> = this
    override fun fromDb(row: DbRow, column: String): T? = fromDbNullable(row, column)
    override fun handleNull(row: DbRow, column: String): T? = null

}

public class NullableWrapperField<T>(public val delegate: BasicField<T>) : NullableField<T>() {

    override fun fromDbNullable(row: DbRow, column: String): T? {
        return delegate.fromDbNullable(row, column)
    }

}
