rootProject.name = "reinhardt"
//include("dbgen")
include("core")
include("postgres")
include("processor")
include("example")
include("generator")
include("model-state")
include("class-names")
include("jdbc")
include("ksp-processor")

pluginManagement {
    val kotlinVersion: String by settings

    repositories {
        gradlePluginPortal()
        google()
    }

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
        kotlin("jvm")version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
    }
}
