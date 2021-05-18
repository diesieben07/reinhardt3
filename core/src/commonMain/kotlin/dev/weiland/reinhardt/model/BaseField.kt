package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.db.DbRow

// fields
public sealed interface BaseField

public abstract class RelationField<M : Model>(public val referencedModel: M) : BaseField
public abstract class SimpleField<T> : BaseField {

    public open fun nullable(): SimpleField<T?> {
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

public abstract class NullableField<T> : SimpleField<T?>() {

    override fun nullable(): NullableField<T> = this
    override fun fromDb(row: DbRow, column: String): T? = fromDbNullable(row, column)
    override fun handleNull(row: DbRow, column: String): T? = null

}

public class NullableWrapperField<T>(public val delegate: SimpleField<T>) : NullableField<T>() {

    override fun fromDbNullable(row: DbRow, column: String): T? {
        return delegate.fromDbNullable(row, column)
    }

}
