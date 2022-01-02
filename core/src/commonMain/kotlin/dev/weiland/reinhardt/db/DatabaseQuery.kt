package dev.weiland.reinhardt.db

import dev.weiland.reinhardt.expr.Expression
import dev.weiland.reinhardt.expr.ModelExpressionContainer
import dev.weiland.reinhardt.expr.and
import dev.weiland.reinhardt.model.Expr
import dev.weiland.reinhardt.model.Model
import dev.weiland.reinhardt.model.ModelCompanion

public sealed interface DatabaseQuery<M : Model, M_REF : ModelExpressionContainer, E : Any> {

    public val model: ModelCompanion<M, E, M_REF>
    public val currentFilter: Expression<Boolean>?
    public fun addFilter(expression: Expression<Boolean>): DatabaseQuery<M, M_REF, E>

}

private data class DatabaseQueryImpl<M : Model, M_REF : ModelExpressionContainer, E : Any>(override val model: ModelCompanion<M, E, M_REF>, override val currentFilter: Expression<Boolean>?) : DatabaseQuery<M, M_REF, E> {
    override fun addFilter(expression: Expression<Boolean>): DatabaseQuery<M, M_REF, E> {
        return DatabaseQueryImpl(
            model,
            if (currentFilter == null) expression else currentFilter and expression
        )
    }
}

public fun <M : Model, E : Any, M_REF : ModelExpressionContainer> ModelCompanion<M, E, M_REF>.all(): DatabaseQuery<M, M_REF, E> {
    return DatabaseQueryImpl(this, null)
}

public infix fun <M : Model, M_REF : ModelExpressionContainer, E : Any> DatabaseQuery<M, M_REF, E>.filter(body: M_REF.() -> Expression<Boolean>) {
    val ref = model.ref()
    addFilter(ref.body())
}