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
import dev.weiland.reinhardt.gen.field.FieldCodegenFactories

internal class KspProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val modelClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("dev.weiland.reinhardt.model.Model")
        ) ?: error("Failed to find model class")
        val modelType = modelClass.asStarProjectedType()

        environment.logger.warn("Processor is running")

        val writerLazy = lazy {
            environment.codeGenerator.createNewFile(
                dependencies = Dependencies.ALL_FILES,
                "foo",
                "output.dummy",
                extensionName = "txt"
            ).bufferedWriter()
        }
        val writer by writerLazy

        for (file in resolver.getNewFiles()) {
            environment.logger.warn("File: ${file.filePath}")
            for (cls in file.declarations.filterIsInstance<KSClassDeclaration>()) {
                if (modelType.isAssignableFrom(cls.asStarProjectedType())) {
                    writer.write(ModelInfoFromKspResolver(resolver).makeModelInfoFromKsp(cls).toString())
                    writer.newLine()
                    ModelGenerator(environment, resolver, cls, CodegenTargetImpl(file)).run()
                }
            }
        }

        if (writerLazy.isInitialized()) {
            writer.close()
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

            val codegenModel = CodegenModel(
                ClassName.bestGuess(checkNotNull(cls.qualifiedName).asString()),
            )
            val fields = fieldProperties.mapNotNull { fieldProperty ->
                makeCodegenField(codegenModel, fieldProperty)
            }.sortedBy { (data, _) -> !data.isPrimaryKey }.map { (_, gen) -> gen }.toList()
            ModelCodegen(codegenModel, codegenTarget).generate(fields)
            environment.logger.warn("Found fields: $fields")
        }

        private fun makeCodegenField(model: CodegenModel, property: KSPropertyDeclaration): Pair<CodegenField, FieldCodegen>? {
            val fieldResolvedType = property.type.resolve()
            if (!fieldType.isAssignableFrom(fieldResolvedType)) {
                return null
            }

            val fieldData = CodegenField(
                property.simpleName.asString(), property.isPrimaryKey(), fieldResolvedType.toKotlinPoet()
            )
            val lookup = object : FieldInfoLookup {

                override fun lookupPropertyType(propertyClassName: ClassName, propertyName: String): TypeName? {
                    val propertyClass = resolver.getClassDeclarationByName(propertyClassName.toKSName()) ?: return null
                    return if (propertyClass.asStarProjectedType().isAssignableFrom(fieldResolvedType)) {
                        val lookupProperty = propertyClass.getDeclaredProperties().single { it.simpleName.getShortName() == propertyName }
                        lookupProperty.asMemberOf(fieldResolvedType).toKotlinPoet()
                    } else {
                        null
                    }
                }

                override fun lookupFunctionReturnType(functionClassName: ClassName, functionName: String): TypeName? {
                    val functionClass = resolver.getClassDeclarationByName(functionClassName.toKSName()) ?: return null
                    return if (functionClass.asStarProjectedType().isAssignableFrom(fieldResolvedType)) {
                        val lookupFunction = functionClass.getDeclaredFunctions().single { it.simpleName.getShortName() == functionName }
                        lookupFunction.asMemberOf(fieldResolvedType).returnType?.toKotlinPoet()
                    } else {
                        null
                    }
                }

                override fun isSubtypeOf(rawClass: ClassName): Boolean {
                    val supertype = resolver.getClassDeclarationByName(rawClass.toKSName()) ?: return false
                    return supertype.asStarProjectedType().isAssignableFrom(fieldResolvedType)
                }

                override fun hasAnnotation(annotationClassName: ClassName): Boolean {
                    val ksName = annotationClassName.toKSName()
                    return property.annotations.any {
                        it.shortName.getShortName() == ksName.getShortName() && it.annotationType.resolve().declaration.qualifiedName == ksName
                    }
                }

                override fun lookupPrimaryKeyInfo(modelClassName: ClassName): PrimaryKeyInfo? {
                    // TODO this is terrible, we need caching here
                    // and also not hardcoding BasicField
                    val modelClass = resolver.getClassDeclarationByName(modelClassName.toKSName()) ?: return null
                    for (modelClassProperty in modelClass.getDeclaredProperties()) {
                        if (modelClassProperty.hasAnnotation(primaryKeyAnnotationName)) {
                            val resolvedPropertyType = modelClassProperty.type.resolve()
                            check(basicFieldType.isAssignableFrom(resolvedPropertyType))
                            return basicFieldFromDb.asMemberOf(resolvedPropertyType).returnType?.toKotlinPoet()?.let { pkType ->
                                PrimaryKeyInfo(modelClassProperty.simpleName.getShortName(), pkType)
                            }
                        }
                    }
                    return null
                }
            }

            val fieldCodegen = FieldCodegenFactories.getCodeGenerator(model, fieldData, lookup)
            return fieldData to checkNotNull(fieldCodegen) {
                "Failed to find code generator for field $fieldData"
            }
        }

        private fun ClassName.toKSName(): KSName {
            return resolver.getKSNameFromString(this.canonicalName)
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