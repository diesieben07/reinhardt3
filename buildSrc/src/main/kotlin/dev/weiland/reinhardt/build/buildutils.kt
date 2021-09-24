package dev.weiland.reinhardt.build

import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.getting
import org.gradle.kotlin.dsl.invoke
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension

fun KotlinProjectExtension.configureKotlinReinhardt(isPublic: Boolean = true) {
    sourceSets.all {
        languageSettings.optIn("kotlin.RequiresOptIn")
        languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        languageSettings.optIn("dev.weiland.reinhardt.ReinhardtInternalApi")
        languageSettings.optIn("com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview")
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

fun IdeaModel.addGeneratedSource(fileName: String) {
    module {
        sourceDirs = sourceDirs + project.file(fileName)
        generatedSourceDirs = generatedSourceDirs + project.file(fileName)
    }
}