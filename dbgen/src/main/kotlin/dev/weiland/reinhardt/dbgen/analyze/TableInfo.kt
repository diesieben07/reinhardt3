package dev.weiland.reinhardt.dbgen.analyze

import java.sql.JDBCType

data class TableInfo(
    val catalog: String?,
    val schema: String?,
    val table: String
)

data class ColumnInfo(
    val column: String,
    val jdbcType: JDBCType
)