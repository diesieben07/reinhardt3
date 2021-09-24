
plugins {
    kotlin("multiplatform")
    kotlin("kapt")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm() {

    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }

        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.autoService.annotations)
                implementation("us.fatehi:schemacrawler:16.11.7")
            }
        }
    }
}

dependencies {
    add("kapt", libs.autoService.processor)
}
