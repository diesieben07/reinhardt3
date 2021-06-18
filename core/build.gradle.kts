import dev.weiland.reinhardt.build.configureKotlinReinhardt
import dev.weiland.reinhardt.build.coroutinesVersion

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
        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation(project(":class-names"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.2.0")
            }
        }
    }
}