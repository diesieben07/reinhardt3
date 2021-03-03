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
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import kotlinx.metadata.*
import kotlinx.metadata.jvm.JvmMethodSignature
import kotlinx.metadata.jvm.KotlinClassMetadata
import kotlinx.metadata.jvm.getterSignature
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.ArrayType
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"

private const val MODEL_ANNOTATION = "dev.weiland.reinhardt.ModelAnnotation"
private const val MODEL_CLASS = "dev.weiland.reinhardt.Model"

private const val MODEL_PACKAGE = "dev.weiland.reinhardt"
private val MODEL_REF_CLASS_NAME = ClassName(MODEL_PACKAGE, "ModelRef")
private val MODEL_REF_MODEL_FUN = "model"

private val FIELD_CLASS_NAME = ClassName(MODEL_PACKAGE, "Field")
private val FIELD_CLASS_NAME_KM: String = FIELD_CLASS_NAME.reflectionName()
private val FIELD_TYPE_CAPTURE_METHOD: String = "hackCaptureType"

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

    private fun processModel(modelClass: TypeElement) {
        val info = classInfo(modelClass)
        if (info.km.name.isLocal) {
            throw IllegalArgumentException("Model class ${info.km} must not be a local class")
        }
        println("model: ${info.km.name} has props: ${info.km.properties.map { it.name }}")
        val refClass = createRefClass(info)
        val fileSpec = FileSpec.builder(info.className.packageName, "${info.className.simpleNames.joinToString("_")}__reinhard_generated")
            .addType(refClass)
            .build()
        fileSpec.writeTo(outputDir)
    }

    private fun classInfo(te: TypeElement): ClassInfo {
        val metadata = te.readMetadata() ?: throw IllegalArgumentException("Kotlin Metadata missing for $te")
        if (metadata !is KotlinClassMetadata.Class) throw IllegalArgumentException("Kotlin Metadata for $te says its not a class")
        val kmClass = metadata.toKmClass()
        return ClassInfo(te, kmClass)
    }

    inner class ClassInfo(val te: TypeElement, val km: KmClass) {

        val className: ClassName by lazy {
            if (km.name.isLocal) {
                throw IllegalArgumentException("No ClassName for local classes")
            }
            ClassName(km.name.packageName, km.name.className.split('.'))
        }

//        val fieldProperties: List<ModelFieldInfo> by lazy {
//            km.properties.mapNotNull { prop ->
//                modelFieldInfo(this@ClassInfo, prop)
//            }
//        }
    }

    private fun createRefClass(modelClass: ClassInfo): TypeSpec {
        val refName = ClassName(modelClass.className.packageName, modelClass.className.simpleNames.joinToString("_", postfix = "Ref"))
        val superclass = MODEL_REF_CLASS_NAME.parameterizedBy(
            modelClass.className,
            ClassName("kotlin", "Any")
        )

        val classBuilder = TypeSpec.classBuilder(refName)
        classBuilder
            .superclass(superclass)
            .addFunction(
                FunSpec.builder(MODEL_REF_MODEL_FUN)
                    .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
                    .returns(modelClass.className)
                    .addCode("return %T", modelClass.className)
                    .build()
            )

        val kmClass = modelClass.te.toImmutableKmClass()
        val kmClassSpec = kmClass.toTypeSpec(inspector)
        for (property in kmClass.properties) {
            val fieldType = resolveFieldType(property) ?: continue
            classBuilder.addProperty(
                PropertySpec.builder(property.name, fieldType)
                    .initializer("%M()", MemberName("kotlin", "TODO"))
                    .build()
            )
            println("PROPERTY ${property} with type ${fieldType}")
        }


        return classBuilder.build()
    }

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

//    internal fun modelFieldInfo(cls: ClassInfo, property: KmProperty): ModelFieldInfo? {
//        val getterSignature = property.getterSignature ?: return null
//        val executableElement = cls.te.findMethodBySignature(getterSignature) ?: return null
//        val fieldContentType = findFieldContentType(executableElement) ?: return null
//        return ModelFieldInfo(property, executableElement, fieldContentType)
//    }

    inner class ModelFieldInfo(val km: KmProperty, val ee: ExecutableElement, val fieldType: KmType)

//    private fun findFieldContentType(ee: ExecutableElement): KmType? {
//        val fieldSupertype = ee.returnType.findTypedSuperclass(fieldElement) ?: return null
//        return when (fieldSupertype.classifier) {
//            // TODO: Variance?
//            is KmClassifier.Class -> fieldSupertype.arguments.singleOrNull()?.type ?: error("Got invalid field superclass $fieldSupertype")
//            else -> error("Field supertype is a not a class?")
//        }
//    }

    private fun TypeMirror.matchesAsmType(asmType: AsmType): Boolean {
        require(asmType.sort != AsmType.METHOD) { "Must not be a method type" }
        val erasure = processingEnv.typeUtils.erasure(this)
        println("checking types: ${this} -> ${erasure} with ${asmType}")
        return when (erasure.kind) {
            TypeKind.BOOLEAN -> asmType.sort == AsmType.BOOLEAN
            TypeKind.BYTE -> asmType.sort == AsmType.BYTE
            TypeKind.SHORT -> asmType.sort == AsmType.SHORT
            TypeKind.INT -> asmType.sort == AsmType.INT
            TypeKind.LONG -> asmType.sort == AsmType.LONG
            TypeKind.FLOAT -> asmType.sort == AsmType.FLOAT
            TypeKind.DOUBLE -> asmType.sort == AsmType.DOUBLE
            TypeKind.CHAR -> asmType.sort == AsmType.CHAR
            TypeKind.VOID -> asmType.sort == AsmType.VOID
            TypeKind.ARRAY -> {
                asmType.sort == AsmType.ARRAY && (erasure as ArrayType).componentType.matchesAsmType(asmType.fixedElementType())
            }
            TypeKind.DECLARED -> {
                asmType.sort == AsmType.OBJECT && run {
                    val element = (erasure as DeclaredType).asElement() as? TypeElement
                    println("found element ${element} with binaryName ${processingEnv.elementUtils.getBinaryName(element)} for declared type ${erasure}")
                    element != null && processingEnv.elementUtils.getBinaryName(element).contentEquals(asmType.className)
                }
            }
            TypeKind.NONE -> TODO()
            TypeKind.NULL -> TODO()
            TypeKind.ERROR -> TODO()
            TypeKind.TYPEVAR -> TODO()
            TypeKind.WILDCARD -> TODO()
            TypeKind.PACKAGE -> TODO()
            TypeKind.EXECUTABLE -> TODO()
            TypeKind.OTHER -> TODO()
            TypeKind.UNION -> TODO()
            TypeKind.INTERSECTION -> TODO()
            TypeKind.MODULE -> TODO()
            null -> TODO()
        }
    }

    private fun TypeElement.findMethodBySignature(signature: JvmMethodSignature): ExecutableElement? {
        val asmType = AsmType.getMethodType(signature.desc)
        val methods = enclosedElements.asSequence().filterIsInstance<ExecutableElement>()
            .map { it to it.returnType }
            .toList()
        println("trying to find ${signature}: ${methods}")
        return enclosedElements.asSequence().filterIsInstance<ExecutableElement>()
            .filter { it.simpleName.contentEquals(signature.name) }
            .filter { it.returnType.matchesAsmType(asmType.returnType) }
            .filter {
                val asmArgs = asmType.argumentTypes
                it.parameters.size == asmArgs.size && it.parameters.withIndex().all { (i, p) ->
                    p.asType().matchesAsmType(asmArgs[i])
                }
            }
            .singleOrNull()
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

