package dev.weiland.reinhardt.db

import dev.weiland.reinhardt.model.Model
import dev.weiland.reinhardt.model.ModelCompanionWithPK
import dev.weiland.reinhardt.model.ModelReader
import kotlinx.coroutines.CoroutineScope

public interface Database : CoroutineScope {

    public suspend fun getResults(): DbResults

    public suspend fun <M : Model, R : Any, PK : Any> getEntity(reader: ModelCompanionWithPK<M, R, PK>, id: PK): R? {
        TODO()
//        return getSingle(reader.all().filter(reader.primaryKeyField))
    }

    public suspend fun <M : Model, E : Any> getFirst(query: DatabaseQuery<M, E>): E?
    public suspend fun <M : Model, E : Any> getSingle(query: DatabaseQuery<M, E>): E?

}