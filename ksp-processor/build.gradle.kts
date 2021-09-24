import dev.weiland.reinhardt.build.configureKotlinReinhardt

plugins {
    kotlin("jvm")
    kotlin("kapt")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":class-names"))
    implementation(project(":generator"))
    implementation(libs.ksp)
    implementation(libs.autoService.annotations)
    kapt(libs.autoService.processor)
}

kotlin {
    configureKotlinReinhardt(isPublic = false)
}