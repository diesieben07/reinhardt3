package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

public class BasicFieldCodegen(private val field: CodegenField) : FieldCodegen {

    override fun generateEntityInterface(typeBuilder: TypeSpec.Builder) {
        val basicFieldType = checkNotNull(field.basicFieldContentType)
        val interfaceProperty = PropertySpec.builder(
            field.name, basicFieldType
        )
        typeBuilder.addProperty(interfaceProperty.build())
    }

}