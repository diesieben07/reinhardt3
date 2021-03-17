package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.metadata.ImmutableKmClass
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import dev.weiland.reinhardt.constants.KnownNames

public data class ModelState(
    public val name: ClassName,
    public val field: List<FieldState>
) {

    public companion object {

        @KotlinPoetMetadataPreview
        public fun of(className: ClassName, classInspector: ClassInspector): ModelState? {
            if (!classInspector.isSubclass(className, KnownNames.MODEL_CLASS_NAME)) {
                return null
            }

            val kmClass = classInspector.declarationContainerFor(className) as? ImmutableKmClass ?: return null
            val typeSpec = kmClass.toTypeSpec(classInspector, className)

            val fieldTypeResolver = ClassInspectorFieldTypeResolver(classInspector)

            val fields = typeSpec.propertySpecs.mapNotNull { property ->
                val propertyType = property.type
                if (fieldTypeResolver.isFieldType(propertyType)) {
                    FieldState(property.name, propertyType)
                } else null
            }

            if (fields.isEmpty()) {
                return null
            }

            return ModelState(className, fields)
        }

    }

}