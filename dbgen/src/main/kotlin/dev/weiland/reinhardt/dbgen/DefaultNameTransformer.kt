package dev.weiland.reinhardt.dbgen

class DefaultNameTransformer : NameTransformer {

    private companion object {
        val theMagicRegex = Regex("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|(?<!^)_+")
    }

    override fun getName(external: String, capitalize: Boolean): String {
        return external.split(theMagicRegex)
            .mapIndexed { index, part ->
                part.lowercase().let { str ->
                    if (capitalize || index > 0) str.replaceFirstChar { it.uppercase() } else str
                }
            }
            .joinToString(separator = "")
    }
}