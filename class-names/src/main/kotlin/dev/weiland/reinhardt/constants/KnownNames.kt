package dev.weiland.reinhardt.constants

import com.squareup.kotlinpoet.ClassName

public object KnownNames {

    public const val MODEL_PACKAGE: String = "dev.weiland.reinhardt.model"
    public val FIELD_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "Field")
    public val MODEL_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "Model")

}