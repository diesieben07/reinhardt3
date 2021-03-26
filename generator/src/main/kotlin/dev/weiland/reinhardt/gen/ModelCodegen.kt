package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import dev.weiland.reinhardt.constants.KnownNames.MODEL_REF_CLASS_NAME
import dev.weiland.reinhardt.constants.KnownNames.MODEL_REF_MODEL_FUN
import dev.weiland.reinhardt.constants.KnownNames.makeRefClassName
import dev.weiland.reinhardt.model.state.ModelState

public class ModelCodegen(private val model: ModelState) {

    public fun generate() {

    }

    private fun generateRefClass(model: ModelState): TypeSpec {
        val refClassName = makeRefClassName(model.className)
        val superclass = MODEL_REF_CLASS_NAME.parameterizedBy(
            model.className,
            refClassName
        )
        val classBuilder = TypeSpec.classBuilder(refClassName)
        classBuilder
            .superclass(superclass)
            .addFunction(
                FunSpec.builder(MODEL_REF_MODEL_FUN)
                    .addModifiers(KModifier.FINAL, KModifier.OVERRIDE)
                    .returns(model.className)
                    .addCode("return %T", model.className)
                    .build()
            )

        for (property in model.fields) {
//            val propertySpec = when (property) {
//                is ModelFieldInfo.Simple -> {
//                    val fieldRefType = FIELD_REF_CLASS_NAME.parameterizedBy(property.fieldContentType)
//                    PropertySpec.builder(property.km.name, fieldRefType)
//                        .initializer("%T(this, %S)", FIELD_REF_CLASS_NAME, property.km.name)
//                }
//                is ModelFieldInfo.Relation -> {
//                    val modelRefClass = modelClassDerivedName(property.referencedModelClass, postfix = REF_CLASS_POSTFIX)
//                    PropertySpec.builder(property.km.name, modelRefClass)
//                        .delegate(
//                            "%N { %T() }",
//                            MemberName("kotlin", "lazy"),
//                            modelRefClass
//                        )
//                }
//            }

//            classBuilder.addProperty(propertySpec.build())
        }

        return classBuilder.build()
    }

}