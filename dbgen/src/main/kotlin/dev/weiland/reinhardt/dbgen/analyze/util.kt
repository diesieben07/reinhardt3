package dev.weiland.reinhardt.dbgen.analyze

import java.sql.ResultSet

inline fun <R> ResultSet.toList(each: (ResultSet) -> R): List<R> {
    return buildList {
        while (next()) {
            val value = each(this@toList)
            add(value)
        }
    }
}