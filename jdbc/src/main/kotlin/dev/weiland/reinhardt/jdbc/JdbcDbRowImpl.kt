package dev.weiland.reinhardt.jdbc

import dev.weiland.reinhardt.db.DbResults
import java.sql.ResultSet

internal class JdbcDbRowImpl(override val resultSet: ResultSet) : JdbcDbRow, DbResults {

    override fun getInt(column: String): Int = resultSet.getInt(column)
    override fun getLong(column: String): Long = resultSet.getLong(column)
    override fun getDouble(column: String): Double = resultSet.getDouble(column)
    override fun getString(column: String): String? = resultSet.getString(column)
    override fun wasNull(column: String): Boolean = resultSet.wasNull()

    override fun next(): Boolean = resultSet.next()

}