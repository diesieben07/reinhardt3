package dev.weiland.reinhardt.dbgen

import schemacrawler.schema.Column
import schemacrawler.schema.Table

interface NameTransformer {

    fun getName(external: String, capitalize: Boolean): String

    fun getColumnName(column: Column): String = getName(column.name, false)
    fun getTableName(table: Table): String = getName(table.name, true)

}