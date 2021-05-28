package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.db.DbRow
import dev.weiland.reinhardt.type.ColumnType

// fields
public sealed interface Field {

}

public abstract class RelationField<M : Model>(public val referencedModel: M) : Field {
}

public sealed interface BasicField<T> : Field {

    public val original: BasicField<out T>

    public fun fromDb(row: DbRow, column: String): T
    public fun fromDbNullable(row: DbRow, column: String): T?

}

public abstract class BasicFieldBase<T> : BasicField<T>, CanBeNullableField, CanBePrimaryKeyField {

    final override val original: BasicField<T> get() = this

    public override fun fromDb(row: DbRow, column: String): T {
        val nullableVal = fromDbNullable(row, column)
        return nullableVal ?: handleNull(row, column)
    }

    protected open fun handleNull(row: DbRow, column: String): T {
        throw IllegalStateException("Invalid null value received for field $this in column $column")
    }
}

public sealed interface BasicFieldWrapper<T, F : BasicField<out T>> : BasicField<T> {

    override val original: F

}

public sealed interface CanBePrimaryKeyField

public class PrimaryKeyWrapper<T : Any, F : BasicField<T>> internal constructor(override val original: F) : BasicFieldWrapper<T, F>, BasicField<T> by original {

}

public fun <T : Any, F> F.primaryKey(): PrimaryKeyWrapper<T, F> where F : CanBePrimaryKeyField, F : BasicField<T> {
    return PrimaryKeyWrapper(this)
}

public abstract class TypeBasedField<T> : BasicFieldBase<T>() {

    public abstract val type: ColumnType<T>

    override fun fromDbNullable(row: DbRow, column: String): T? {
        return type.getNullable(row, column)
    }

}

public class TestWrapper<T, F : BasicField<T>> internal constructor(override val original: F) : BasicFieldWrapper<T, F>, BasicField<T> by original

public fun <T, F : BasicField<T>> F.testWrapper(): TestWrapper<T, F> {
    return TestWrapper(this)
}

public abstract class NullableFieldBase<T> : BasicFieldBase<T?>() {

    override fun fromDb(row: DbRow, column: String): T? = fromDbNullable(row, column)
    override fun handleNull(row: DbRow, column: String): T? = null

}

public sealed interface CanBeNullableField

public class NullableWrapperField<T, F : BasicField<T>> internal constructor(override val original: F) : BasicFieldWrapper<T?, F> {

    override fun fromDb(row: DbRow, column: String): T? = fromDbNullable(row, column)
    override fun fromDbNullable(row: DbRow, column: String): T? = original.fromDbNullable(row, column)

}

public fun <T : Any, F> F.nullable(): NullableWrapperField<T, F> where F : CanBeNullableField, F : BasicFieldBase<T> {
    return NullableWrapperField(this)
}