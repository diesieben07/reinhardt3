plugins {
    kotlin("jvm")
    idea
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.ExperimentalStdlibApi")
        }
        main {
            resources.srcDir("src/main/generated")

            println(resources.sourceDirectories.files.map { it.absoluteFile })
        }
    }
}


idea {
    module {
        sourceDirs = sourceDirs + project.file("src/main/generated")
        generatedSourceDirs = generatedSourceDirs + project.file("src/main/generated")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
//    api(project(":core"))

    implementation(libs.kotlinpoet.core)
    implementation("org.postgresql:postgresql:42.2.18")
    implementation("us.fatehi:schemacrawler:16.11.7")
    implementation("us.fatehi:schemacrawler-postgresql:16.11.7")
}
