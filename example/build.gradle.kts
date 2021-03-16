import dev.weiland.reinhardt.build.configureKotlinReinhardt
import dev.weiland.reinhardt.build.kotlinPoet

plugins {
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
    implementation(project(":core"))
    implementation(project(":model-state"))

    implementation(kotlinPoet("classinspector-reflective"))

    kapt(project(":processor"))
}

kapt {
    mapDiagnosticLocations = true
}

kotlin.configureKotlinReinhardt(false)