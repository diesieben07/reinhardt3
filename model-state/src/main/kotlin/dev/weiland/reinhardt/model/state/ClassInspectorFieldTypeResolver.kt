package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import dev.weiland.reinhardt.constants.KnownNames
import kotlinx.metadata.KmClass
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.isLocal

@KotlinPoetMetadataPreview
public class ClassInspectorFieldTypeResolver(private val classInspector: ClassInspector) : FieldTypeResolver {

    private fun ImmutableKmClass.superClassName(): ClassName? {
        val superClass = supertypes.firstOrNull()?.classifier as? KmClassifier.Class ?: return null
        return if (superClass.name == "kotlin/Any") {
            null
        } else {
            ClassInspectorUtil.createClassName(superClass.name)
        }
    }

    override fun isFieldType(type: TypeName): Boolean {
        val className = when (type) {
            is ClassName -> type
            is ParameterizedTypeName -> type.rawType
            is TypeVariableName -> TODO("NYI")
            is WildcardTypeName -> TODO("NYI")
            is LambdaTypeName, is Dynamic -> return false
        }
        var current = className
        do {
            if (current == KnownNames.FIELD_CLASS_NAME) {
                return true
            }
            val cls = classInspector.declarationContainerFor(current) as? ImmutableKmClass ?: return false
            current = cls.superClassName() ?: return false
        } while (true)
    }

    override fun resolveFieldTypeInfo(fieldType: TypeName): FieldTypeInfo {
        TODO("Not yet implemented")
    }
}