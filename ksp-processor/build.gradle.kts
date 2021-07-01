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
    val kspVersion: String by project

    implementation(kotlin("stdlib"))
    implementation(project(":class-names"))
    implementation(project(":generator"))
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
    implementation("com.google.auto.service:auto-service-annotations:1.0")
    kapt("com.google.auto.service:auto-service:1.0")
}

kotlin {
    configureKotlinReinhardt(isPublic = false)
}