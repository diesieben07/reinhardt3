package dev.weiland.reinhardt.build

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

val kotlinPoetVersion = "1.7.2"

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

fun KotlinProjectExtension.configureKotlinReinhardt(isPublic: Boolean = true) {
    sourceSets.all {
        languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
        languageSettings.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        languageSettings.useExperimentalAnnotation("dev.weiland.reinhardt.ReinhardtInternalApi")
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
                implementation(kotlin("stdlib-common"))
            }
        }
    }
}
