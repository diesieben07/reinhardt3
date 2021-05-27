package dev.weiland.reinhardt.model

public interface ModelInfo {

    public val qualifiedName: String
    public val fields: List<Field>
    public val primaryKey: BasicField<*>?

    public companion object {

        public fun of(modelInstance: Model): ModelInfo {
            TODO()
        }

    }

}