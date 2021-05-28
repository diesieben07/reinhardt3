package dev.weiland.reinhardt.ksp

import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.sun.org.apache.bcel.internal.generic.FMUL
import dev.weiland.reinhardt.constants.FieldMarker

internal class KspProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val modelClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.Model")
        ) ?: error("Failed to find model class")
        val modelType = modelClass.asStarProjectedType()


        environment.logger.warn("Processor is running")

        for (file in resolver.getAllFiles()) {
            environment.logger.warn("File: ${file.filePath}")
            for (cls in file.declarations.filterIsInstance<KSClassDeclaration>()) {
                if (modelType.isAssignableFrom(cls.asStarProjectedType())) {
                    ModelGenerator(environment, resolver, cls).run()
                }
            }
        }

        return emptyList()
    }

    private class ModelGenerator(
        val environment: SymbolProcessorEnvironment,
        val resolver: Resolver,
        val cls: KSClassDeclaration
    ) {

        val fieldClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.Field")
        ) ?: error("failed to find field class")
        val fieldType = fieldClass.asStarProjectedType()

        val primaryKeyWrapperClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.PrimaryKeyWrapper")
        ) ?: error("failed to find field class")

        val basicFieldWrapperClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.BasicFieldWrapper")
        ) ?: error("Failed to find BasicFieldWrapper class")
        val basicFieldWrapperType = basicFieldWrapperClass.asStarProjectedType()

        val basicFieldWrapperOriginalProperty = basicFieldWrapperClass.getDeclaredProperties()
            .single { it.simpleName.asString() == "original" }

        fun run() {


            environment.logger.warn("Found model class: ${cls.qualifiedName?.asString()}")
            val fieldProperties = cls.declarations.filterIsInstance<KSPropertyDeclaration>()
                .filter { fieldType.isAssignableFrom(it.type.resolve()) }
            for (fieldProperty in fieldProperties) {
                environment.logger.warn("Found field property $fieldProperty")
                val resolvedFieldType = fieldProperty.type.resolve()
                val unwrappedType = unwrapFieldType(resolvedFieldType)
                environment.logger.warn("Found field: $unwrappedType")
            }
        }

        private fun unwrapFieldType(type: KSType): UnwrappedFieldType {
            val markers = mutableSetOf<FieldMarker>()
            var currentType = type
            while (basicFieldWrapperType.isAssignableFrom(currentType)) {
                markers.add(getFieldMarkerForWrapperType(currentType))
                currentType = basicFieldWrapperOriginalProperty.asMemberOf(currentType)
            }
            return UnwrappedFieldType(type, currentType, markers)
        }

        private fun getFieldMarkerForWrapperType(wrapperType: KSType): FieldMarker {
            return when (val simpleName = (wrapperType.declaration as KSClassDeclaration).simpleName.asString()) {
                "PrimaryKeyWrapper" -> FieldMarker.PRIMARY_KEY
                "NullableWrapperField" -> FieldMarker.NULLABLE_WRAPPER
                "TestWrapper" -> FieldMarker.TEST
                else -> throw IllegalArgumentException("Invalid field marker wrapper $simpleName")
            }
        }

    }

    private data class UnwrappedFieldType(val declaredType: KSType, val originalFieldType: KSType, val markers: Set<FieldMarker>)


}