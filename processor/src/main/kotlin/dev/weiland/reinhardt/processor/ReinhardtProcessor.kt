package dev.weiland.reinhardt.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.*
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import com.squareup.kotlinpoet.metadata.specs.classFor
import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import kotlinx.metadata.*
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

private const val MODEL_ANNOTATION = "dev.weiland.reinhardt.ModelAnnotation"
private const val MODEL_CLASS = "dev.weiland.reinhardt.Model"

private const val MODEL_PACKAGE = "dev.weiland.reinhardt"
private val MODEL_REF_CLASS_NAME = ClassName(MODEL_PACKAGE, "ModelRef")
private val MODEL_REF_MODEL_FUN = "model"

private val FIELD_REF_CLASS_NAME = ClassName(MODEL_PACKAGE, "FieldRef")

private val FIELD_CLASS_NAME = ClassName(MODEL_PACKAGE, "Field")
private val RELATION_FIELD_CLASS_NAME = ClassName(MODEL_PACKAGE, "RelationField")
private val SIMPLE_FIELD_CLASS_NAME = ClassName(MODEL_PACKAGE, "SimpleField")
private val NULLABLE_FIELD_CLASS_NAME = ClassName(MODEL_PACKAGE, "NullableField")

private val FIELD_CLASS_NAME_KM: String = FIELD_CLASS_NAME.toKmClassName()
private val SIMPLE_FIELD_CLASS_NAME_KM: String = SIMPLE_FIELD_CLASS_NAME.toKmClassName()
private val NULLABLE_FIELD_CLASS_NAME_KM: String = NULLABLE_FIELD_CLASS_NAME.toKmClassName()
private val RELATION_FIELD_CLASS_NAME_KM: String = RELATION_FIELD_CLASS_NAME.toKmClassName()

private const val REF_CLASS_POSTFIX = "Ref"
private const val ENTITY_INTERFACE_PREFIX = "E"
private const val ENTITY_LAZY_PREFIX = "L"

// this has a silly name to avoid any conflicts
private const val LAZY_ENTITY_DELEGATE_PROPERTY = "delegate___reinhardt-no-conflict"

private val DATABASE_CLASS_NAME = ClassName(MODEL_PACKAGE, "Database")
private val DATABASE_ALL_METHOD_NAME = "all"
private val ITERABLE = ClassName("kotlin.collections", "Iterable")

private val MODEL_QUERYSET_CLASS_NAME = ClassName(MODEL_PACKAGE, "ModelQuerySet")
private val MODEL_REF_DEFAULT_PROPERTY_NAME = "rootRef"

private val DB_ROW_CLASS_NAME = ClassName(MODEL_PACKAGE, "DbRow")
private val MODEL_READER_CLASS_NAME = ClassName(MODEL_PACKAGE, "ModelReader")
private val MODEL_READER_READ_ENTITY_FUN = "readEntity"
private val MODEL_READER_READ_ENTITY_FUN_ROW_PARAM = "row"


@SupportedAnnotationTypes(MODEL_ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
@OptIn(KotlinPoetMetadataPreview::class)
class ReinhardtProcessor : AbstractProcessor() {

    private lateinit var outputDir: Path
    private lateinit var inspector: ClassInspector

    private val typeUtils: Types get() = processingEnv.typeUtils
    private val elementUtils: Elements get() = processingEnv.elementUtils


    private val fieldElement by lazy {
        processingEnv.elementUtils.getTypeElement(FIELD_CLASS_NAME.canonicalName) ?: error("Failed finding Reinhardt Field class")
    }

    private val fieldType by lazy {
        fieldElement.asType()
    }

    override fun init(processingEnv: ProcessingEnvironment) {
        super.init(processingEnv)
        inspector = ElementsClassInspector.create(elementUtils, typeUtils)
        outputDir = Paths.get(processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.")
            return
        })
    }

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        if (!this::outputDir.isInitialized) {
            return false
        }

        val modelAnnotation = annotations.find { it.qualifiedName.contentEquals(MODEL_ANNOTATION) } ?: return false
        val modelClasses = roundEnv.getElementsAnnotatedWith(modelAnnotation)
            .filterIsInstance<TypeElement>()
            .filterNot { it.qualifiedName.contentEquals(MODEL_CLASS) }
        for (modelClass in modelClasses) {
            if (!processModel(modelClass)) {
                return false
            }
        }
        return true
    }

    private fun processModel(typeElement: TypeElement): Boolean {
        val modelClass = ModelClassInfo(typeElement)
        if (!modelClass.validate()) {
            return false
        }
        val refClass = createRefClass(modelClass)
        val entityInterface = createEntityType(modelClass, EntityClassType.INTERFACE)
        val entityDataClass = createEntityType(modelClass, EntityClassType.DATA_CLASS)
        val entityLazyClass = createEntityType(modelClass, EntityClassType.LAZY_ENTITY)
        val readerClass = createModelReader(modelClass)
        val dbExtension = createDbExtension(modelClass)
        val fileSpec = FileSpec.builder(modelClass.className.packageName, "${modelClass.className.simpleNames.joinToString("_")}__reinhard_generated")
            .addType(entityInterface)
            .addType(entityDataClass)
            .addType(entityLazyClass)
            .addType(refClass)
            .addType(readerClass)
            .addProperty(dbExtension)
            .build()
        fileSpec.writeTo(outputDir)
        return true
    }

    companion object {
        private fun modelClassDerivedName(modelClassName: ClassName, prefix: String = "", postfix: String = ""): ClassName {
            return modelClassName.peerClass(modelClassName.simpleNames.joinToString(separator = "_", prefix = prefix, postfix = postfix))
        }
    }

    internal inner class ModelClassInfo(val te: TypeElement) {

        val km: ImmutableKmClass by lazy {
            te.toImmutableKmClass()
        }

        val className: ClassName by lazy {
            ClassInspectorUtil.createClassName(km.name)
        }

        val dbExtensionName: String by lazy {
            className.simpleNames.last().decapitalize().pluralizeEnglish()
        }

        fun validate(): Boolean {
            val enclosingElement = te.enclosingElement
            if (enclosingElement == null) {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR, "You cannot have a model object here", te
                )
                return false
            }
            return if (enclosingElement.kind == ElementKind.PACKAGE || enclosingElement.kind == ElementKind.CLASS) {
                if (!km.isObject) {
                    processingEnv.messager.printMessage(
                        Diagnostic.Kind.ERROR, "Models must be objects", te
                    )
                }
                check(!km.name.isLocal) { "Model class name is local unexpectedly"}
                true
            } else {
                processingEnv.messager.printMessage(
                    Diagnostic.Kind.ERROR, "Model object must be top level or non-inner nested classes", te
                )
                false
            }
        }

        private fun derivedClassName(prefix: String = "", postfix: String = ""): ClassName {
            return modelClassDerivedName(className, prefix, postfix)
        }

        val refClassName: ClassName by lazy {
            derivedClassName(postfix = REF_CLASS_POSTFIX)
        }

        fun entityClassName(type: EntityClassType): ClassName {
            return when (type) {
                EntityClassType.INTERFACE -> entityInterfaceName
                EntityClassType.DATA_CLASS -> entityDataClassName
                EntityClassType.LAZY_ENTITY -> entityLazyClassName
            }
        }

        val entityInterfaceName: ClassName by lazy {
            derivedClassName(prefix = ENTITY_INTERFACE_PREFIX)
        }

        val entityDataClassName: ClassName by lazy {
            derivedClassName(prefix = "D")
        }

        val entityLazyClassName: ClassName by lazy {
            derivedClassName(prefix = ENTITY_LAZY_PREFIX)
        }

        val readerClassName: ClassName by lazy {
            derivedClassName(postfix = "Reader")
        }

        val fieldProperties: List<ModelFieldInfo> by lazy {
            km.properties.mapNotNull { prop ->
                makeModelFieldInfo(prop.findElement(te), prop)
            }
        }
    }

    private fun createDbExtension(modelClass: ModelClassInfo): PropertySpec {
        val querySetType = MODEL_QUERYSET_CLASS_NAME.parameterizedBy(
            modelClass.className,
            modelClass.entityInterfaceName,
            modelClass.refClassName
        )
        return PropertySpec.builder(modelClass.dbExtensionName, querySetType)
            .receiver(DATABASE_CLASS_NAME)
            .getter(
                FunSpec.getterBuilder()
                    .addCode(
                        "return this.%N(%M, %T)",
                        DATABASE_ALL_METHOD_NAME,
                        MemberName(modelClass.refClassName.nestedClass("Companion"), MODEL_REF_DEFAULT_PROPERTY_NAME),
                        modelClass.readerClassName
                    )
                    .build()
            )
            .build()
    }

    private fun createRefClass(modelClass: ModelClassInfo): TypeSpec {
        val superclass = MODEL_REF_CLASS_NAME.parameterizedBy(
            modelClass.className,
            modelClass.entityInterfaceName
        )

        val classBuilder = TypeSpec.classBuilder(modelClass.refClassName)
        classBuilder
            .superclass(superclass)
            .addFunction(
                FunSpec.builder(MODEL_REF_MODEL_FUN)
                    .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
                    .returns(modelClass.className)
                    .addCode("return %T", modelClass.className)
                    .build()
            )

        for (property in modelClass.fieldProperties) {
            val propertySpec = when (property) {
                is ModelFieldInfo.Simple -> {
                    val fieldRefType = FIELD_REF_CLASS_NAME.parameterizedBy(property.fieldContentType)
                    PropertySpec.builder(property.km.name, fieldRefType)
                        .initializer("%T(this, %S)", FIELD_REF_CLASS_NAME, property.km.name)
                }
                is ModelFieldInfo.Relation -> {
                    val modelRefClass = modelClassDerivedName(property.referencedModelClass, postfix = REF_CLASS_POSTFIX)
                    PropertySpec.builder(property.km.name, modelRefClass)
                        .delegate(
                            "%N { %T() }",
                            MemberName("kotlin", "lazy"),
                            modelRefClass
                        )
                }
            }

            classBuilder.addProperty(propertySpec.build())
        }

        val companionObject = TypeSpec.companionObjectBuilder()
            .addProperty(
                PropertySpec.builder(MODEL_REF_DEFAULT_PROPERTY_NAME, modelClass.refClassName)
                    .delegate("%M { %T() }", MemberName("kotlin", "lazy"), modelClass.refClassName)
                    .build()
            )
            .build()

        classBuilder.addType(companionObject)

        return classBuilder.build()
    }

    internal enum class EntityClassType {
        INTERFACE,
        DATA_CLASS,
        LAZY_ENTITY;

        fun createBuilder(modelClass: ModelClassInfo): TypeSpec.Builder {
            val className = modelClass.entityClassName(this)
            val builder = when (this) {
                INTERFACE -> TypeSpec.interfaceBuilder(className)
                DATA_CLASS -> TypeSpec.classBuilder(className).addModifiers(KModifier.DATA)
                LAZY_ENTITY -> TypeSpec.classBuilder(className)
            }
            return when (this) {
                DATA_CLASS, LAZY_ENTITY -> {
                    builder.addSuperinterface(modelClass.entityInterfaceName)
                }
                else -> builder
            }
        }
    }

    private fun createEntityType(modelClass: ModelClassInfo, type: EntityClassType): TypeSpec {
        val entityInterfaceProvider = LambdaTypeName.get(
            returnType = modelClass.entityInterfaceName
        )
        return type.createBuilder(modelClass).also { classBuilder ->
            val constructorBuilder = FunSpec.constructorBuilder()

            if (type == EntityClassType.LAZY_ENTITY) {
                constructorBuilder.addParameter("factory", entityInterfaceProvider)
                classBuilder.addProperty(
                    PropertySpec.builder(LAZY_ENTITY_DELEGATE_PROPERTY, modelClass.entityInterfaceName)
                        .addModifiers(KModifier.PRIVATE)
                        .delegate("%M(factory)", MemberName("kotlin", "lazy"))
                        .build()
                )
            }

            for (property in modelClass.fieldProperties) {
                classBuilder.addProperty(
                    PropertySpec.builder(property.km.name, property.entityFieldType)
                        .also {
                            when (type) {
                                EntityClassType.DATA_CLASS -> {
                                    it.initializer("%N", property.km.name)
                                    it.addModifiers(KModifier.OVERRIDE)
                                }
                                EntityClassType.LAZY_ENTITY -> {
                                    it.addModifiers(KModifier.OVERRIDE)
                                    it.getter(
                                        FunSpec.getterBuilder()
                                            .addCode("return %N.%N", LAZY_ENTITY_DELEGATE_PROPERTY, property.km.name)
                                            .build()
                                    )
                                }
                                EntityClassType.INTERFACE -> {}
                            }
                        }
                        .build()
                )
                if (type == EntityClassType.DATA_CLASS) {
                    constructorBuilder.addParameter(property.km.name, property.entityFieldType)
                }
            }
            if (type == EntityClassType.LAZY_ENTITY || type == EntityClassType.DATA_CLASS) {
                classBuilder.primaryConstructor(constructorBuilder.build())
            }
        }.build()
    }

    private fun createModelReader(model: ModelClassInfo): TypeSpec {
        val superType = MODEL_READER_CLASS_NAME.parameterizedBy(
            model.className, model.entityInterfaceName
        )
        return TypeSpec.objectBuilder(model.readerClassName)
            .addSuperinterface(superType)
            .addFunction(
                FunSpec.builder(MODEL_READER_READ_ENTITY_FUN)
                    .addModifiers(KModifier.OVERRIDE)
                    .addParameter(ParameterSpec(MODEL_READER_READ_ENTITY_FUN_ROW_PARAM, DB_ROW_CLASS_NAME))
                    .returns(model.entityInterfaceName)
                    .addCode("return %M()", MemberName("kotlin", "TODO"))
                    .build()
            )
            .build()
    }

    sealed class ModelFieldInfo {

        abstract val km: ImmutableKmProperty
        abstract fun makeNullable(nullable: Boolean): ModelFieldInfo

        abstract val entityFieldType: TypeName

        data class Simple(override val km: ImmutableKmProperty, val fieldContentType: TypeName) : ModelFieldInfo() {
            override fun makeNullable(nullable: Boolean): ModelFieldInfo {
                return copy(fieldContentType = fieldContentType.copy(nullable = nullable))
            }

            override val entityFieldType: TypeName
                get() = fieldContentType
        }
        data class Relation(override val km: ImmutableKmProperty, val referencedModelClass: ClassName): ModelFieldInfo() {
            override fun makeNullable(nullable: Boolean): ModelFieldInfo {
                return copy(referencedModelClass = referencedModelClass.copy(tags = referencedModelClass.tags, nullable = nullable))
            }

            override val entityFieldType: TypeName by lazy {
                modelClassDerivedName(referencedModelClass, prefix = ENTITY_INTERFACE_PREFIX)
            }
        }

    }

    internal fun makeModelFieldInfo(context: Element, property: ImmutableKmProperty, type: ImmutableKmType = property.returnType): ModelFieldInfo? {
        for (supertype in type.walkSuperClassTypes(context)) {
            val classifier = supertype.classifier as? KmClassifier.Class ?: continue
            when (classifier.name) {
                SIMPLE_FIELD_CLASS_NAME_KM -> {
                    return supertype.arguments.single().type?.makeTypeName()?.let {
                        ModelFieldInfo.Simple(property, it)
                    }
                }
                NULLABLE_FIELD_CLASS_NAME_KM -> {
                    val nested = makeModelFieldInfo(context, property, supertype.arguments.single().type ?: return null) ?: return null
                    return nested.makeNullable(true)
                }
                RELATION_FIELD_CLASS_NAME_KM -> {
                    val modelClass = supertype.arguments.single().type ?: return null
                    val modelClassClassifier = modelClass.classifier
                    require(modelClassClassifier is KmClassifier.Class) { "RelationField argument must be a class" }
                    val modelClassName = ClassInspectorUtil.createClassName(modelClassClassifier.name)
                    return ModelFieldInfo.Relation(property, modelClassName)
                }
            }
        }
        return null
    }

    internal fun ImmutableKmType.makeTypeName(): TypeName {
        val classifier = classifier
        require(classifier is KmClassifier.Class) { "Only classes are supported for now" }
        require(arguments.isEmpty()) { "TypeArgs not yet supported" }
        val className = ClassInspectorUtil.createClassName(classifier.name)
        return className.copy(nullable = Flag.Type.IS_NULLABLE(flags))
    }

    internal fun ImmutableKmType.walkSuperClassTypes(context: Element): Sequence<ImmutableKmType> {
        return sequence {
            var current: ImmutableKmType? = this@walkSuperClassTypes
            while (current != null) {
                yield(current)
                val currentClassifier = current.classifier
                // TODO: Support TypeParameters if necessary?
                if (currentClassifier !is KmClassifier.Class) {
                    processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "TypeParameters not supported here yet", context)
                    return@sequence
                }
                val currentClass = inspector.classFor(ClassInspectorUtil.createClassName(currentClassifier.name))
                current = currentClass.superClassType(current.arguments)
            }
        }
    }

    private fun ImmutableKmClass.superClassType(arguments: List<ImmutableKmTypeProjection>): ImmutableKmType? {
        require(isClass) { "Must be a class" }
        val superClassType = supertypes.first()
        val superClassClassifier = superClassType.classifier
        check(superClassClassifier is KmClassifier.Class) { "SuperClass is not a class" }
        return if (superClassClassifier.name == "kotlin/Any") {
            null
        } else {
            val paramMap = HashMap<Int, ImmutableKmType?>()
            check(arguments.size == typeParameters.size) { "TypeParameter size mismatch" }
            for ((index, typeParameter) in typeParameters.withIndex()) {
                val arg = arguments[index]
                paramMap[typeParameter.id] = arg.type
            }
            return superClassType.replaceTypeParams(paramMap)
        }
    }

    private fun ImmutableKmType.replaceTypeParams(map: Map<Int, ImmutableKmType?>): ImmutableKmType? {
        val classifier = classifier
        if (classifier is KmClassifier.TypeParameter) {
            // type parameters don't have args
            return map[classifier.id]?.toMutable()?.let { result ->
                result.flags = result.flags or this.flags
                result.toImmutable()
            }
        } else {
            val result = KmType(flags)
            result.classifier = classifier
            arguments.mapTo(result.arguments) { arg ->
                arg.toMutable().also { result ->
                    result.type = result.type?.toImmutable()?.replaceTypeParams(map)?.toMutable()
                }
            }
            return result.toImmutable()
        }
    }
}
