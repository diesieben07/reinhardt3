package dev.weiland.reinhardt.db

public interface DbRow {

    public fun getInt(column: String): Int
    public fun getLong(column: String): Long
    public fun getDouble(column: String): Double

    public fun wasNull(column: String): Boolean

    public fun getString(column: String): String?

}