package dev.weiland.reinhardt.db

import dev.weiland.reinhardt.model.Model
import dev.weiland.reinhardt.model.ModelReader
import kotlinx.coroutines.CoroutineScope

public interface Database : CoroutineScope {

    public suspend fun getResults(): DbResults

    public suspend fun <M : Model, R : Any> getEntity(reader: ModelReader<M, R>, id: Any): R?

}