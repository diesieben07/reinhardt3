import dev.weiland.reinhardt.build.configureKotlinReinhardt
import dev.weiland.reinhardt.build.kotlinPoet
import dev.weiland.reinhardt.build.kotlinxSerialization

plugins {
    id("com.google.devtools.ksp") version "1.5.10-1.0.0-beta01"
    kotlin("jvm")
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