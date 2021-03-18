package dev.weiland.reinhardt.constants

import com.squareup.kotlinpoet.ClassName

public object KnownNames {

    public const val MODEL_PACKAGE: String = "dev.weiland.reinhardt.model"
    public val FIELD_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "Field")
    public val MODEL_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "Model")

    public val MODEL_REF_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "ModelRef")
    public const val MODEL_REF_MODEL_FUN: String = "model"

    public val FIELD_REF_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "FieldRef")

    private const val REF_CLASS_POSTFIX = "Ref"
    private const val ENTITY_INTERFACE_PREFIX = "E"
    private const val ENTITY_LAZY_PREFIX = "L"

    private fun modelClassDerivedName(modelClassName: ClassName, prefix: String = "", postfix: String = ""): ClassName {
        return modelClassName.peerClass(modelClassName.simpleNames.joinToString(separator = "_", prefix = prefix, postfix = postfix))
    }

    public fun makeRefClassName(modelClassName: ClassName): ClassName {
        return modelClassDerivedName(modelClassName, postfix = REF_CLASS_POSTFIX)
    }

}