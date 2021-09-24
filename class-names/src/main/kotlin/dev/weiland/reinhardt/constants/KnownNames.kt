package dev.weiland.reinhardt.constants

import com.squareup.kotlinpoet.ClassName

public object KnownNames {

    public const val MODEL_ANNOTATION: String = "dev.weiland.reinhardt.ModelAnnotation"

    private const val MODEL_PACKAGE: String = "dev.weiland.reinhardt.model"
    private const val EXPR_PACKAGE: String = "dev.weiland.reinhardt.expr"
    public val FIELD_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "Field")
    public val BASIC_FIELD_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "BasicField")
    public val BASIC_FIELD_FROM_DB_FUN: String = "fromDb"
    public val MODEL_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "Model")

    public val MODEL_REF_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "ModelRef")
    public const val MODEL_REF_MODEL_FUN: String = "model"

    public val FIELD_REF_CLASS_NAME: ClassName = ClassName(MODEL_PACKAGE, "FieldRef")

    private const val REF_CLASS_POSTFIX = "Ref"
    private const val ENTITY_INTERFACE_PREFIX = "E"
    private const val ENTITY_LAZY_PREFIX = "L"

    public const val MODEL_COMPANION_FUN: String = "objects"
    public const val ENTITY_READER_FUN: String = "entityReader"

    public const val MODEL_COMPANION_ENTITY_READER_VAL: String = "entityReader"
    public const val MODEL_COMPANION_MODEL_VAL: String = "model"

    public val MODEL_EXPRESSION_CONTAINER_CLASS_NAME: ClassName = ClassName(EXPR_PACKAGE, "ModelExpressionContainer")
    public const val MODEL_EXPRESSION_CONTAINER_ALIAS_PARAMETER: String = "alias"
    public const val MODEL_EXPRESSION_CONTAINER_ALIAS: String = "alias"

    public val FIELD_EXPRESSION_CLASS_NAME: ClassName = ClassName(EXPR_PACKAGE, "FieldExpression")

    private fun modelClassDerivedName(modelClassName: ClassName, prefix: String = "", postfix: String = ""): ClassName {
        return modelClassName.peerClass(modelClassName.simpleNames.joinToString(separator = "_", prefix = prefix, postfix = postfix))
    }

    public fun makeRefClassName(modelClassName: ClassName): ClassName {
        return modelClassDerivedName(modelClassName, postfix = REF_CLASS_POSTFIX)
    }

    public fun getGeneratedFileClassName(modelClassQualifiedName: String, includePackage: Boolean = false): String {
        val modelClassSimpleName = modelClassQualifiedName.substringAfterLast('.')
        val generatedSimpleName = modelClassSimpleName + "__reinhardt_generated"
        return when {
            includePackage && modelClassQualifiedName.contains('.') -> modelClassQualifiedName.substringBeforeLast('.') + '.' + generatedSimpleName
            else -> generatedSimpleName
        }
    }

}