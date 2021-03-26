package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.metadata.specs.internal.ClassInspectorUtil
import dev.weiland.reinhardt.model.state.FieldState
import dev.weiland.reinhardt.model.state.ModelState
import kotlinx.metadata.KmClassifier

public object BuiltInCodegenFactory : (ModelState, FieldState) -> FieldCodegen? {

    override fun invoke(model: ModelState, field: FieldState): FieldCodegen? {
//        for (supertype in field.fieldType.) {
//            val classifier = supertype.classifier as? KmClassifier.Class ?: continue
//            when (classifier.name) {
//                SIMPLE_FIELD_CLASS_NAME_KM -> {
//                    return supertype.arguments.single().type?.makeTypeName()?.let {
//                        ModelFieldInfo.Simple(property, it)
//                    }
//                }
//                NULLABLE_FIELD_CLASS_NAME_KM -> {
//                    val nested = makeModelFieldInfo(context, property, supertype.arguments.single().type ?: return null) ?: return null
//                    return nested.makeNullable(true)
//                }
//                RELATION_FIELD_CLASS_NAME_KM -> {
//                    val modelClass = supertype.arguments.single().type ?: return null
//                    val modelClassClassifier = modelClass.classifier
//                    require(modelClassClassifier is KmClassifier.Class) { "RelationField argument must be a class" }
//                    val modelClassName = ClassInspectorUtil.createClassName(modelClassClassifier.name)
//                    return ModelFieldInfo.Relation(property, modelClassName)
//                }
//            }
//        }
        return null
    }
}