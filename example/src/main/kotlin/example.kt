package dev.weiland.reinhardt.example

import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.classinspector.reflective.ReflectiveClassInspector
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.specs.classFor
import com.squareup.kotlinpoet.metadata.specs.toTypeSpec
import dev.weiland.reinhardt.model.*
import dev.weiland.reinhardt.model.state.ClassInspectorFieldTypeResolver
import dev.weiland.reinhardt.model.state.ModelState

object Person : Model() {

    val name = TextField()
    val parent = ForeignKey(Person)

    fun getFoo() {

    }

}

object User : Model() {
    val id = TextField()
    val name = TextField()

    val foo = Any()
}

@KotlinPoetMetadataPreview
fun main() {
    val classInspector = ReflectiveClassInspector.create()
    val fieldTypeResolver = ClassInspectorFieldTypeResolver(classInspector)
    val typeSpec = classInspector.classFor(Person::class.asClassName()).toTypeSpec(classInspector)
    println(ModelState.of(typeSpec, fieldTypeResolver))
}
