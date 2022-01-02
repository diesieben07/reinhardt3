package dev.weiland.reinhardt.db

import dev.weiland.reinhardt.expr.ModelExpressionContainer
import dev.weiland.reinhardt.model.Model
import dev.weiland.reinhardt.model.ModelCompanionWithPK
import dev.weiland.reinhardt.model.ModelReader
import kotlinx.coroutines.CoroutineScope

public interface Database : CoroutineScope {

    public suspend fun getResults(): DbResults

    public suspend fun <M : Model, M_REF : ModelExpressionContainer, R : Any, PK : Any> getEntity(reader: ModelCompanionWithPK<M, R, M_REF, PK>, id: PK): R? {
        TODO()
//        return getSingle(reader.all().filter(reader.primaryKeyField))
    }

    public suspend fun <M : Model, M_REF : ModelExpressionContainer, E : Any> getFirst(query: DatabaseQuery<M, M_REF, E>): E?
    public suspend fun <M : Model, M_REF : ModelExpressionContainer, E : Any> getSingle(query: DatabaseQuery<M, M_REF, E>): E?

}