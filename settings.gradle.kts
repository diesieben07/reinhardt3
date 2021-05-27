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
    plugins {
        kotlin("multiplatform") version "1.5.10" apply false
        kotlin("jvm")version "1.5.10" apply false
        kotlin("plugin.serialization") version "1.5.10" apply false
    }
}