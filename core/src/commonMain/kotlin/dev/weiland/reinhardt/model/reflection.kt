package dev.weiland.reinhardt.model

public fun <M : Model> M.getModelCompanion(): ModelCompanion<M, *> {
    return getModelCompanionImpl(this)
}

internal expect fun <M : Model> getModelCompanionImpl(model: M): ModelCompanion<M, *>
