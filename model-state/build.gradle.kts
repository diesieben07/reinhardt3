import dev.weiland.reinhardt.build.configureKotlinReinhardt

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":core"))
    implementation(project(":class-names"))

    implementation(libs.bundles.kotlinpoet)
    implementation(libs.bundles.serialization)
}

kotlin.configureKotlinReinhardt()

kotlin {
    sourceSets.all {

    }
}