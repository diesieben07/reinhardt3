import dev.weiland.reinhardt.build.configureKotlinReinhardt
import dev.weiland.reinhardt.build.kotlinPoet

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

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":class-names"))
    implementation(project(":model-state"))

    api(kotlinPoet())
    implementation(kotlinPoet("metadata"))
    implementation(kotlinPoet("metadata-specs"))
    implementation(kotlinPoet("classinspector-reflective"))

    implementation("com.google.auto.service:auto-service-annotations:1.0")
    kapt("com.google.auto.service:auto-service:1.0")
}

kotlin.configureKotlinReinhardt(isPublic = true)