import dev.weiland.reinhardt.build.configureKotlinReinhardt
import dev.weiland.reinhardt.build.kotlinPoet
import dev.weiland.reinhardt.build.kotlinxSerialization

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "dev.weiland.reinhardt"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation(project(":core"))
    implementation(project(":class-names"))

    implementation(kotlinPoet())
    implementation(kotlinPoet("metadata"))
    implementation(kotlinPoet("metadata-specs"))
    implementation(kotlinPoet("classinspector-reflective"))

    implementation(kotlinxSerialization("core"))
    implementation(kotlinxSerialization("json"))
}

kotlin.configureKotlinReinhardt()

kotlin {
    sourceSets.all {

    }
}