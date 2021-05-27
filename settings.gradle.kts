rootProject.name = "reinhardt"
include("dbgen")
include("core")
include("postgres")
include("processor")
include("example")
include("generator")
include("model-state")
include("class-names")
include("jdbc")

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        kotlin("multiplatform") version kotlinVersion apply false
        kotlin("jvm")version kotlinVersion apply false
        kotlin("plugin.serialization") version kotlinVersion apply false
    }
}
include("ksp-processor")
