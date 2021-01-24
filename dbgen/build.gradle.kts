plugins {
    kotlin("jvm")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlin.RequiresOptIn")
            languageSettings.useExperimentalAnnotation("kotlin.ExperimentalStdlibApi")
        }
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    api(project(":core"))

    implementation("com.squareup:kotlinpoet:1.7.2")
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("us.fatehi:schemacrawler:16.11.7")
    implementation("us.fatehi:schemacrawler-postgresql:16.11.7")
}
