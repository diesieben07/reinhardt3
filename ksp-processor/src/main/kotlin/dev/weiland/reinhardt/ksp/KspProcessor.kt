package dev.weiland.reinhardt.ksp

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.weiland.reinhardt.constants.FieldMarker
import dev.weiland.reinhardt.gen.*

internal class KspProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val modelClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.Model")
        ) ?: error("Failed to find model class")
        val modelType = modelClass.asStarProjectedType()


        environment.logger.warn("Processor is running")

        for (file in resolver.getNewFiles()) {
            environment.logger.warn("File: ${file.filePath}")
            for (cls in file.declarations.filterIsInstance<KSClassDeclaration>()) {
                if (modelType.isAssignableFrom(cls.asStarProjectedType())) {
                    ModelGenerator(environment, resolver, cls, CodegenTargetImpl(file)).run()
                }
            }
        }

        return emptyList()
    }

    private inner class CodegenTargetImpl(private val modelSourceFile: KSFile) : CodegenTarget {
        override fun accept(file: FileSpec) {
            environment.codeGenerator.createNewFile(
                dependencies = Dependencies(true, modelSourceFile),
                packageName = file.packageName,
                fileName = file.name
            ).bufferedWriter().use { writer ->
                file.writeTo(writer)
            }
        }
    }

    private class ModelGenerator(
        val environment: SymbolProcessorEnvironment,
        val resolver: Resolver,
        val cls: KSClassDeclaration,
        val codegenTarget: CodegenTarget
    ) {

        val fieldAnnotationName = resolver.getKSNameFromString(
            "dev.weiland.reinhardt.model.FieldAnnotation"
        )

        val primaryKeyAnnotationName = resolver.getKSNameFromString(
            "dev.weiland.reinhardt.model.PrimaryKey"
        )

        val fieldClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.Field")
        ) ?: error("failed to find field class")
        val fieldType = fieldClass.asStarProjectedType()

        val basicFieldClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.BasicField")
        ) ?: error("failed to find field class")
        val basicFieldType = basicFieldClass.asStarProjectedType()

        val basicFieldFromDb = basicFieldClass.getDeclaredFunctions()
            .single { it.simpleName.asString() == "fromDb" }

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

            val fields = fieldProperties.mapNotNull { fieldProperty ->
                makeCodegenField(fieldProperty)
            }.toList()
            val codegenModel = CodegenModel(
                ClassName.bestGuess(checkNotNull(cls.qualifiedName).asString()),
                fields
            )
            ModelCodegen(codegenModel, codegenTarget).generate()
            environment.logger.warn("Found fields: $fields")
        }

        private fun makeCodegenField(property: KSPropertyDeclaration): CodegenField? {
            val resolvedType = property.type.resolve()
            if (!fieldType.isAssignableFrom(resolvedType)) {
                return null
            }

            val entityProperties: List<CodegenEntityProperty>
            val basicFieldContentType: KSType?
            if (basicFieldType.isAssignableFrom(resolvedType)) {
                // TODO
                val resolvedFieldDataType = basicFieldFromDb.asMemberOf(resolvedType).returnType ?: return null
                entityProperties = listOf(
                    CodegenEntityProperty(
                        property.simpleName.asString(),
                        resolvedFieldDataType.toKotlinPoet()
                    )
                )
                basicFieldContentType = resolvedFieldDataType
            } else {
                // TODO
                entityProperties = listOf()
                basicFieldContentType = null
            }
            return CodegenField(
                property.simpleName.asString(), entityProperties, property.isPrimaryKey(),
                basicFieldContentType = basicFieldContentType?.toKotlinPoet()
            )
        }

        private fun KSPropertyDeclaration.isPrimaryKey(): Boolean {
            return annotations.any {
                it.shortName.getShortName() == "PrimaryKey" && it.annotationType.resolve().declaration.qualifiedName == primaryKeyAnnotationName
            }
        }

        private fun KSType.toKotlinPoet(): TypeName {
            val qualifiedName = requireNotNull((declaration as KSClassDeclaration).qualifiedName).asString()
            // TODO: improve this
            val className = ClassName.bestGuess(qualifiedName)
            return if (arguments.isEmpty()) {
                className
            } else {
                className.parameterizedBy(
                    arguments.map { it.toKotlinPoet() }
                )
            }.copy(nullable = this.isMarkedNullable)
        }

        private fun KSTypeArgument.toKotlinPoet(): TypeName {
            val rawType = type?.resolve()?.toKotlinPoet()
            return when (variance) {
                Variance.STAR -> STAR
                Variance.INVARIANT -> checkNotNull(rawType)
                Variance.COVARIANT -> WildcardTypeName.producerOf(checkNotNull(rawType))
                Variance.CONTRAVARIANT -> WildcardTypeName.consumerOf(checkNotNull(rawType))
            }
        }

        private fun getFieldAnnotations(property: KSPropertyDeclaration): List<KSAnnotation> {
            return property.annotations.filterTo(ArrayList()) {
                it.annotationType.resolve().declaration.hasAnnotation(fieldAnnotationName)
            }
        }

        private fun KSAnnotated.hasAnnotation(qualifiedName: KSName): Boolean {
            return annotations.any {
                it.annotationType.resolve().declaration.qualifiedName == qualifiedName
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