package dev.weiland.reinhardt.build

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

val kotlinPoetVersion = "1.7.2"
val serializationVersion = "1.1.0"
val coroutinesVersion = "1.5.0"

fun DependencyHandler.kotlinPoet(module: String? = null, version: String = kotlinPoetVersion): String {
    return buildString {
        append("com.squareup:kotlinpoet")
        if (module != null) {
            append("-")
            append(module)
        }
        append(":")
        append(version)
    }
}

fun DependencyHandler.kotlinxSerialization(module: String, version: String = serializationVersion): String {
    return "org.jetbrains.kotlinx:kotlinx-serialization-$module:$version"
}

fun KotlinProjectExtension.configureKotlinReinhardt(isPublic: Boolean = true) {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        languageSettings.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        languageSettings.useExperimentalAnnotation("dev.weiland.reinhardt.ReinhardtInternalApi")
        languageSettings.useExperimentalAnnotation("com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview")
    }

    if (isPublic) {
        explicitApi()
    }
}

fun KotlinMultiplatformExtension.configureKotlinReinhardt(isPublic: Boolean = true) {
    jvm()

    (this as KotlinProjectExtension).configureKotlinReinhardt(isPublic)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
    }
}
