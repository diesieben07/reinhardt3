package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.constants.KnownNames
import java.lang.reflect.Modifier

private val companionClassValue = object : ClassValue<ModelCompanion<*, *>>() {
    override fun computeValue(type: Class<*>): ModelCompanion<*, *> {
        val qualifiedName = type.kotlin.qualifiedName
        val modelClassName = checkNotNull(qualifiedName) {
            "Model class has no qualified name"
        }
        val generatedClassName = KnownNames.getGeneratedFileClassName(modelClassName, includePackage = true)
        val generatedClass = Class.forName(generatedClassName, true, type.classLoader)
        val modelCompanionFun = generatedClass.getDeclaredMethod(KnownNames.MODEL_COMPANION_FUN, type)
        check(Modifier.isPublic(modelCompanionFun.modifiers) && Modifier.isStatic(modelCompanionFun.modifiers)) { "ModelCompanion getter not public static" }
        return modelCompanionFun.invoke(null, type.kotlin.objectInstance!!) as ModelCompanion<*, *>
    }
}

internal actual fun <M : Model> getModelCompanionImpl(model: M): ModelCompanion<M, *> {
    @Suppress("UNCHECKED_CAST")
    return companionClassValue[model::class.java] as ModelCompanion<M, *>
}