import dev.weiland.reinhardt.build.configureKotlinReinhardt
import dev.weiland.reinhardt.build.kotlinPoet

plugins {
    kotlin("jvm")
    idea
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":class-names"))
    implementation(project(":model-state"))

    api(kotlinPoet())
    implementation(kotlinPoet("metadata"))
    implementation(kotlinPoet("metadata-specs"))
    implementation(kotlinPoet("classinspector-reflective"))
}

kotlin.configureKotlinReinhardt(isPublic = true)