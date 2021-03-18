package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import dev.weiland.reinhardt.constants.KnownNames.FIELD_REF_CLASS_NAME
import dev.weiland.reinhardt.model.state.FieldState

public class SimpleFieldCodegen(private val field: FieldState) : FieldCodegen {

    override fun generateRef(classBuilder: TypeSpec.Builder) {
        val fieldContentType =
        val fieldRefType = FIELD_REF_CLASS_NAME.parameterizedBy(property.fieldContentType)
        PropertySpec.builder(property.km.name, fieldRefType)
            .initializer("%T(this, %S)", FIELD_REF_CLASS_NAME, property.km.name)
    }
}