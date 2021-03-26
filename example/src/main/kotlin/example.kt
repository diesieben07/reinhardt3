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
import dev.weiland.reinhardt.model.state.kmSerializersModule
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.json.Json
import java.io.File

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
    val className = Person::class.asClassName()
    val modelState = ModelState.of(className, classInspector)
    println(modelState)

    val writer = Json {
        serializersModule = kmSerializersModule
        prettyPrint = true
    }
    val json = writer.encodeToString(ModelState.serializer().nullable, modelState)
    File("test.json").writeText(json)
}
