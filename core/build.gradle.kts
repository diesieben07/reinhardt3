plugins {
    kotlin("multiplatform")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
//    explicitApi()

    jvm() {
        compilations.all {
            kotlinOptions {
                jvmTarget = "1.8"
                useIR = true
                freeCompilerArgs = listOf("-Xjvm-default=all")
            }
        }
    }

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        }

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation(kotlin("reflect"))
                implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.2.0")
            }
        }
    }
}