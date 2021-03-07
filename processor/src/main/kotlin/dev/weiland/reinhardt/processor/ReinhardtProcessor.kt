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
private val FIELD_CLASS_NAME_KM: String = FIELD_CLASS_NAME.reflectionName()

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
            processModel(modelClass)
        }
        return true
    }

    private fun processModel(typeElement: TypeElement) {
        val modelClass = classInfo(typeElement)
        if (modelClass.km.name.isLocal) {
            throw IllegalArgumentException("Model class ${modelClass.km} must not be a local class")
        }
        println("model: ${modelClass.km.name} has props: ${modelClass.km.properties.map { it.name }}")
        val refClass = createRefClass(modelClass)
        val entityInterface = createEntityType(modelClass, EntityClassType.INTERFACE)
        val entityDataClass = createEntityType(modelClass, EntityClassType.DATA_CLASS)
        val readerClass = createModelReader(modelClass)
        val dbExtension = createDbExtension(modelClass)
        val fileSpec = FileSpec.builder(modelClass.className.packageName, "${modelClass.className.simpleNames.joinToString("_")}__reinhard_generated")
            .addType(entityInterface)
            .addType(entityDataClass)
            .addType(refClass)
            .addType(readerClass)
            .addProperty(dbExtension)
            .build()
        fileSpec.writeTo(outputDir)
    }

    private fun classInfo(te: TypeElement): ModelClassInfo {
        return ModelClassInfo(te)
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

        private fun derivedClassName(prefix: String = "", postfix: String = ""): ClassName {
            return className.peerClass(className.simpleNames.joinToString(separator = "_", prefix = prefix, postfix = postfix))
        }

        val refClassName: ClassName by lazy {
            derivedClassName("Ref")
        }

        fun entityClassName(type: EntityClassType): ClassName {
            return when (type) {
                EntityClassType.INTERFACE -> entityInterfaceName
                EntityClassType.DATA_CLASS -> entityDataClassName
            }
        }

        val entityInterfaceName: ClassName by lazy {
            derivedClassName(prefix = "E")
        }

        val entityDataClassName: ClassName by lazy {
            derivedClassName(prefix = "D")
        }

        val readerClassName: ClassName by lazy {
            derivedClassName(postfix = "Reader")
        }

        val fieldProperties: List<ModelFieldInfo> by lazy {
            km.properties.mapNotNull { prop ->
                modelFieldInfo(prop)
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
            val fieldRefType = FIELD_REF_CLASS_NAME.parameterizedBy(property.fieldContentType)

            classBuilder.addProperty(
                PropertySpec.builder(property.km.name, fieldRefType)
                    .initializer("%T(this, %S)", FIELD_REF_CLASS_NAME, property.km.name)
                    .build()
            )
            println("PROPERTY ${property} with type ${fieldType}")
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
        DATA_CLASS;

        fun createBuilder(name: ClassName): TypeSpec.Builder {
            return when (this) {
                INTERFACE -> TypeSpec.interfaceBuilder(name)
                DATA_CLASS -> TypeSpec.classBuilder(name).addModifiers(KModifier.DATA)
            }
        }
    }

    private fun createEntityType(modelClass: ModelClassInfo, type: EntityClassType): TypeSpec {
        return type.createBuilder(modelClass.entityClassName(type)).also { classBuilder ->
            if (type == EntityClassType.DATA_CLASS) {
                classBuilder.addSuperinterface(modelClass.entityInterfaceName)
            }
            val constructorBuilder = FunSpec.constructorBuilder()
            for (property in modelClass.fieldProperties) {
                classBuilder.addProperty(
                    PropertySpec.builder(property.km.name, property.fieldContentType)
                        .apply {
                            if (type == EntityClassType.DATA_CLASS) {
                                initializer("%N", property.km.name)
                                addModifiers(KModifier.OVERRIDE)
                            }
                        }
                        .build()
                )
                constructorBuilder.addParameter(property.km.name, property.fieldContentType)
            }
            if (type == EntityClassType.DATA_CLASS) {
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

    internal fun modelFieldInfo(property: ImmutableKmProperty): ModelFieldInfo? {
        val fieldContentType = resolveFieldType(property) ?: return null
        return ModelFieldInfo(property, fieldContentType)
    }

    inner class ModelFieldInfo(val km: ImmutableKmProperty, val fieldContentType: TypeName)

    internal fun resolveFieldType(property: ImmutableKmProperty): TypeName? {
        val classifier = doResolveFieldType(property.returnType)?.classifier as KmClassifier.Class? ?: return null
        return ClassInspectorUtil.createClassName(classifier.name)

//        val returnType = propertySpec.type
//        if (returnType is )
////        val returnTypeClassifier = returnType.classifier
////        return if (returnTypeClassifier is KmClassifier.Class) {
////
////        }
//        return returnType
    }

    internal fun doResolveFieldType(type: ImmutableKmType): ImmutableKmType? {
        return type.asSuper(FIELD_CLASS_NAME)?.arguments?.single()?.type
    }

    internal fun substitutedSuperType(kmType: KmType, parameters: Map<Int, KmType>): KmType {
        return when (val classifier = kmType.classifier) {
            is KmClassifier.Class -> {
                KmType(kmType.flags).also { newType ->
                    newType.visitClass(classifier.name)
                    for (arg in kmType.arguments) {

                    }
                }
            }
            is KmClassifier.TypeParameter -> requireNotNull(parameters[classifier.id]) {
                "TypeParameter $classifier not found"
            }
            is KmClassifier.TypeAlias -> TODO()
        }

    }

    private fun ImmutableKmType.asSuper(cls: ClassName): ImmutableKmType? {
        try {
            println("${this} AS SUPER ${cls}")
            var current = this
            do {
                println("current: $current")
                when (val classifier = current.classifier) {
                    is KmClassifier.Class -> {
                        val currentClassName = ClassInspectorUtil.createClassName(classifier.name)
                        if (currentClassName == cls) {
                            return current
                        }
                    }
                }
                current = current.supertype() ?: let {
                    println("supertype was null")
                    return null
                }
            } while (true)
        } finally {
            println()
            println()
            println()
        }
    }

    private fun ImmutableKmType.supertype(): ImmutableKmType? {
        return when (val classifier = classifier) {
            is KmClassifier.Class -> {
                inspector.classFor(ClassInspectorUtil.createClassName(classifier.name)).superClassType()
            }
            else -> TODO()
        }
    }

    private fun ImmutableKmClass.withArgs(flags: Flags, args: List<KmTypeProjection>): KmType {
        return KmType(flags).also { newType ->
            newType.classifier = KmClassifier.Class(this.name)
            newType.arguments.addAll(args)
        }
    }


    private fun ImmutableKmClass.superClassType(): ImmutableKmType? {
        println("finding superClassType for $this: $supertypes")
        return supertypes.find {
            (it.classifier as? KmClassifier.Class)?.name?.let { superClassName ->
                val superClass = inspector.classFor(
                    ClassInspectorUtil.createClassName(superClassName)
                )
                println("got superclass $superClass for ${it.classifier}")
                superClass.isClass
            } ?: false
        }
    }


}

