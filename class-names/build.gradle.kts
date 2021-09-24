import dev.weiland.reinhardt.build.configureKotlinReinhardt

plugins {
    kotlin("jvm")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlinpoet.core)
}

kotlin.configureKotlinReinhardt()