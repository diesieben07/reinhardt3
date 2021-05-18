import dev.weiland.reinhardt.build.configureKotlinReinhardt

plugins {
    kotlin("multiplatform")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    configureKotlinReinhardt(true)
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.2.0")
            }
        }
    }
}