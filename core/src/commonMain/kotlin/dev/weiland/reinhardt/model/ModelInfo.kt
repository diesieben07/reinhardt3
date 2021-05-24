package dev.weiland.reinhardt.model

public interface ModelInfo {

    public val qualifiedName: String
    public val fields: List<Field>

    public companion object {

        public fun of(modelInstance: Model): ModelInfo {
            TODO()
        }

    }

}