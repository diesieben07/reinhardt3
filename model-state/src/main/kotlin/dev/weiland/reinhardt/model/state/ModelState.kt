package dev.weiland.reinhardt.model.state

import com.squareup.kotlinpoet.TypeSpec

public data class ModelState(
    public val name: String,
    public val field: List<FieldState>
) {

    public companion object {

        public fun of(typeSpec: TypeSpec, fieldTypeResolver: FieldTypeResolver): ModelState {
            require(typeSpec.kind == TypeSpec.Kind.OBJECT)
            require(!typeSpec.isAnonymousClass)

            val fields = typeSpec.propertySpecs.mapNotNull { property ->
                val propertyType = property.type
                if (fieldTypeResolver.isFieldType(propertyType)) {
                    FieldState(property.name, propertyType)
                } else null
            }

            return ModelState(checkNotNull(typeSpec.name), fields)
        }

    }

}