plugins {
    kotlin("jvm") version "1.4.21"
}

repositories {
    mavenCentral()
}

dependencies {
//    implementation(kotlin("gradle-plugin"))
}

//import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget
//
//fun KotlinJvmTarget.configureReinhardtJvmSettings() {
//    compilations.all {
//        kotlinOptions {
//            jvmTarget = "1.8"
//            freeCompilerArgs = kotlin.collections.listOf("-Xjvm-default=all")
//        }
//    }
//}