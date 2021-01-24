package dev.weiland.reinhardt.dbgen.type

import schemacrawler.schema.Catalog
import schemacrawler.schema.Column
import schemacrawler.schema.ColumnDataType

interface TypeMapper {

    fun getColumnType(catalog: Catalog, column: Column, type: ColumnDataType, root: RootTypeMapper): CodegenType?

}