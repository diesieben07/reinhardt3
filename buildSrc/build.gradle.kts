import java.util.Properties

plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

val settings = Properties().also { properties ->
    project.file("../gradle.properties").bufferedReader().use { reader ->
        properties.load(reader)
    }
}

val kotlinVersion: String by settings

dependencies {
    implementation("org.jetbrains.kotlin.jvm:org.jetbrains.kotlin.jvm.gradle.plugin:$kotlinVersion")
}
