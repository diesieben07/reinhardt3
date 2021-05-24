package dev.weiland.reinhardt.model

internal class RuntimeModelInfo(private val modelInstance: Model) : ModelInfo {

    init {
        val modelClass = modelInstance::class
        requireNotNull(modelClass.qualifiedName) {
            "Model class must not be local or anonymous"
        }
    }

    override val qualifiedName: String
        get() = checkNotNull(modelInstance::class.qualifiedName)

    override val fields: List<Field>
        get() = TODO("Not yet implemented")
}