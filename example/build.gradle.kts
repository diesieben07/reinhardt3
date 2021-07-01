import dev.weiland.reinhardt.build.addGeneratedSource
import dev.weiland.reinhardt.build.configureKotlinReinhardt

plugins {
    id("com.google.devtools.ksp")
    kotlin("jvm")
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
    implementation(project(":core"))
//    implementation(project(":model-state"))
//
//    implementation(kotlinPoet("classinspector-reflective"))
//    implementation(kotlinxSerialization("core"))
//    implementation(kotlinxSerialization("json"))

//    implementation(project(":ksp-processor"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.10-1.0.0-beta01")
    implementation(project(":ksp-processor"))
    ksp(project(":ksp-processor"))
}

//kapt {
//    mapDiagnosticLocations = true
//}

kotlin.configureKotlinReinhardt(false)
idea.addGeneratedSource("build/generated/ksp/main/kotlin/")