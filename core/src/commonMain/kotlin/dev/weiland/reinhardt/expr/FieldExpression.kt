package dev.weiland.reinhardt.expr

import dev.weiland.reinhardt.db.DbRow
import dev.weiland.reinhardt.model.BasicField
import dev.weiland.reinhardt.model.Model
import dev.weiland.reinhardt.model.ModelCompanion

public data class FieldExpression<M : Model, T>(
    val model: ModelCompanion<M, *>,
    val modelAlias: String,
    val field: BasicField<T>
) : Expression<T> {

    override fun fromDb(row: DbRow, column: String): T = field.fromDb(row, column)

    override fun fromDbNullable(row: DbRow, column: String): T? = field.fromDbNullable(row, column)
}