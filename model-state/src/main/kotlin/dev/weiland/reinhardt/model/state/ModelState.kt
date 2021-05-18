package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.isAbstract
import com.squareup.kotlinpoet.metadata.isDeclaration
import com.squareup.kotlinpoet.metadata.isSynthesized
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import dev.weiland.reinhardt.constants.KnownNames
import dev.weiland.reinhardt.model.ModelModifier

public data class ModelState(
    public val className: ClassName,
    public val modifiers: Set<ModelModifier>,
    public val fields: List<FieldState>
) {

    public companion object {

        @KotlinPoetMetadataPreview
        public fun of(className: ClassName, classInspector: ClassInspector): ModelState? {
            if (!classInspector.isSubclass(className, KnownNames.MODEL_CLASS_NAME)) {
                return null
            }

            val kmClass = classInspector.getClassOrNull(className) ?: return null

            val fieldTypeResolver = ClassInspectorFieldTypeResolver(classInspector)

            val fields = kmClass.properties.asSequence()
                .filter { !it.isSynthesized && it.isDeclaration }
                .filter { fieldTypeResolver.isFieldType(it.returnType) }
                .map { property ->
                    FieldState(property.name, property.returnType)
                }
                .toList()

            if (fields.isEmpty()) {
                return null
            }

            val modifiers = buildSet {
                if (kmClass.isAbstract) {
                    add(ModelModifier.ABSTRACT)
                }
            }

            return ModelState(className, modifiers, fields)
        }

    }

}