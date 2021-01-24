
plugins {
    kotlin("multiplatform")
    kotlin("kapt")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val autoServiceVersion = "1.0-rc7"
kotlin {
    jvm() {

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
                implementation("com.google.auto.service:auto-service-annotations:$autoServiceVersion")
                implementation("us.fatehi:schemacrawler:16.11.7")
            }
        }
    }
}

dependencies {
    add("kapt", "com.google.auto.service:auto-service:$autoServiceVersion")
}
