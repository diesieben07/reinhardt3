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
        val commonMain by getting {
            dependencies {
                api(libs.coroutines.core)
                implementation(project(":class-names"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation(kotlin("reflect"))
                implementation(libs.metadata)
            }
        }
    }
}