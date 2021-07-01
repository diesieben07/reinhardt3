package dev.weiland.reinhardt.db

import dev.weiland.reinhardt.expr.Expression
import dev.weiland.reinhardt.expr.and
import dev.weiland.reinhardt.model.Model
import dev.weiland.reinhardt.model.ModelCompanion
import kotlin.math.exp

public sealed interface DatabaseQuery<M : Model, E : Any> {

    public val model: ModelCompanion<M, E>
    public val filter: Expression<Boolean>?
    public fun filter(expression: Expression<Boolean>): DatabaseQuery<M, E>

}

private data class DatabaseQueryImpl<M : Model, E : Any>(override val model: ModelCompanion<M, E>, override val filter: Expression<Boolean>?) : DatabaseQuery<M, E> {
    override fun filter(expression: Expression<Boolean>): DatabaseQuery<M, E> {
        return DatabaseQueryImpl(
            model,
            if (filter == null) expression else filter and expression
        )
    }
}

public fun <M : Model, E : Any> ModelCompanion<M, E>.all(): DatabaseQuery<M, E> {
    return DatabaseQueryImpl(this, null)
}
