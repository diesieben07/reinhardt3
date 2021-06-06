package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.metadata.ImmutableKmType
import kotlinx.metadata.ClassName
import kotlinx.metadata.KmClassifier

//public data class FieldState(
//    public val name: String,
//    public val fieldType: ImmutableKmType,
//) {
//
//    init {
//        require(fieldType.classifier is KmClassifier.Class)
//    }
//
//    public val fieldClassName: ClassName get() = (fieldType.classifier as KmClassifier.Class).name
//
//}
//
