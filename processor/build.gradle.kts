import dev.weiland.reinhardt.build.configureKotlinReinhardt

plugins {
    java
    kotlin("jvm")
    kotlin("kapt")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":generator"))
    implementation(project(":class-names"))
    implementation(project(":model-state"))
    implementation(libs.metadata)
    implementation(libs.bundles.kotlinpoet)
    implementation(libs.asm)

    implementation("io.github.encryptorcode:pluralize:1.0.0")

    implementation(libs.autoService.annotations)
    kapt(libs.autoService.processor)
}

kotlin.configureKotlinReinhardt(isPublic = false)

kotlin {
    sourceSets.all {
        languageSettings.optIn("com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview")
    }
}