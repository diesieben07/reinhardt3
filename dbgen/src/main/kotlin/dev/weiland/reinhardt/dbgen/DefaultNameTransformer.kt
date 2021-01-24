package dev.weiland.reinhardt.dbgen

import java.util.*

class DefaultNameTransformer : NameTransformer {

    private companion object {
        val theMagicRegex = Regex("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])|(?<!^)_+")
    }

    override fun getName(external: String, capitalize: Boolean): String {
        return external.split(theMagicRegex)
            .mapIndexed { index, part ->
                part.toLowerCase(Locale.ENGLISH).let { str ->
                    if (capitalize || index > 0) str.capitalize(Locale.ENGLISH) else str
                }
            }
            .joinToString(separator = "")
    }
}