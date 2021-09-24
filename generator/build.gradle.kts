import dev.weiland.reinhardt.build.configureKotlinReinhardt

plugins {
    kotlin("jvm")
    kotlin("kapt")
    idea
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

val kotlinPoetVersion: String by project

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":class-names"))
    implementation(project(":model-state"))

//    api("com.squareup:kotlinpoet:$kotlinPoetVersion")
//    implementation("com.squareup:kotlinpoet-metadata:$kotlinPoetVersion")
//    implementation("com.squareup:kotlinpoet-metadata-specs:$kotlinPoetVersion")
//    implementation("com.squareup:kotlinpoet-classinspector-reflective:$kotlinPoetVersion")


    implementation(libs.bundles.kotlinpoet)

    implementation(libs.autoService.annotations)
    kapt(libs.autoService.processor)
}

kotlin.configureKotlinReinhardt(isPublic = true)