rootProject.name = "reinhardt"

enableFeaturePreview("VERSION_CATALOGS")

include("dbgen")
include("core")
include("postgres")
include("processor")
include("example")
include("generator")
include("model-state")
include("class-names")
include("jdbc")
include("ksp-processor")
include("model")

pluginManagement {
    val kotlinVersion: String by settings
    val kspVersion: String by settings

    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
        kotlin("jvm")version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
        id("com.google.devtools.ksp") version kspVersion apply false
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    versionCatalogs {
        val kspVersion: String by settings

        create("libs") {
            version("asm", "9.2")
            version("autoService", "1.0")
            version("kotlinxCoroutines", "1.5.2")
            version("kotlinxMetadata", "0.2.0")
            version("kotlinxSerialization", "1.2.2")
            version("kotlinPoet", "1.7.2")
            version("ksp", kspVersion)

            alias("ksp").to("com.google.devtools.ksp", "symbol-processing-api").versionRef("ksp")
            alias("kotlinpoet-core").to("com.squareup", "kotlinpoet").versionRef("kotlinPoet")
            alias("kotlinpoet-metadata").to("com.squareup", "kotlinpoet-metadata").versionRef("kotlinPoet")
            alias("kotlinpoet-metadataSpecs").to("com.squareup", "kotlinpoet-metadata-specs").versionRef("kotlinPoet")
            alias("kotlinpoet-classinspectorReflective").to("com.squareup", "kotlinpoet-classinspector-reflective").versionRef("kotlinPoet")

            alias("serialization-core").to("org.jetbrains.kotlinx", "kotlinx-serialization-core").versionRef("kotlinxSerialization")
            alias("serialization-json").to("org.jetbrains.kotlinx", "kotlinx-serialization-json").versionRef("kotlinxSerialization")

            alias("coroutines-core").to("org.jetbrains.kotlinx", "kotlinx-coroutines-core").versionRef("kotlinxCoroutines")

            alias("metadata").to("org.jetbrains.kotlinx", "kotlinx-metadata-jvm").versionRef("kotlinxMetadata")

            alias("asm").to("org.ow2.asm", "asm").versionRef("asm")

            alias("autoService-processor").to("com.google.auto.service", "auto-service").versionRef("autoService")
            alias("autoService-annotations").to("com.google.auto.service", "auto-service-annotations").versionRef("autoService")

            bundle("kotlinpoet", listOf("kotlinpoet-core", "kotlinpoet-metadata", "kotlinpoet-metadataSpecs", "kotlinpoet-classinspectorReflective"))
            bundle("serialization", listOf("serialization-core", "serialization-json"))
        }
    }
}