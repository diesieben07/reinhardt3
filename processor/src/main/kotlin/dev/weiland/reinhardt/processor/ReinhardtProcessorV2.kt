package dev.weiland.reinhardt.processor

import com.google.auto.service.AutoService
import com.squareup.kotlinpoet.classinspector.elements.ElementsClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.ClassInspector
import dev.weiland.reinhardt.constants.KnownNames
import java.nio.file.Path
import java.nio.file.Paths
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types
import javax.tools.Diagnostic

@SupportedAnnotationTypes(KnownNames.MODEL_ANNOTATION)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@AutoService(Processor::class)
@OptIn(KotlinPoetMetadataPreview::class)
class ReinhardtProcessorV2 : AbstractProcessor() {

    companion object {
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }

    private lateinit var outputDir: Path
    private lateinit var inspector: ClassInspector

    private val typeUtils: Types get() = processingEnv.typeUtils
    private val elementUtils: Elements get() = processingEnv.elementUtils


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

        val modelAnnotation = annotations.find { it.qualifiedName.contentEquals(KnownNames.MODEL_ANNOTATION) } ?: return false
        val modelClasses = roundEnv.getElementsAnnotatedWith(modelAnnotation)
            .filterIsInstance<TypeElement>()
            .filterNot { it.qualifiedName.contentEquals(KnownNames.MODEL_CLASS_NAME.canonicalName) }
        for (modelClass in modelClasses) {
            if (!processModel(modelClass)) {
                return false
            }
        }
        return true
    }

    private fun processModel(typeElement: TypeElement): Boolean {
//        val modelState = ModelState.of()
        return false
    }

}