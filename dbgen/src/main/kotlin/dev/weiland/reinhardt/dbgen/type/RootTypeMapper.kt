package dev.weiland.reinhardt.dbgen.type

import schemacrawler.schema.Catalog
import schemacrawler.schema.Column
import schemacrawler.schema.ColumnDataType

class RootTypeMapper(private val delegates: List<TypeMapper>) : TypeMapper {

    constructor(vararg delegates: TypeMapper): this(delegates.asList())

    fun getColumnType(catalog: Catalog, column: Column, type: ColumnDataType): CodegenType {
        return requireNotNull(getColumnType(catalog, column, type, this)) {
            "Failed to find a ColumnType for column $column"
        }
    }

    override fun getColumnType(catalog: Catalog, column: Column, type: ColumnDataType, root: RootTypeMapper): CodegenType? {
        require(root === this) { "Invalid root TypeMapper given" }
        for (delegate in delegates) {
            val result = delegate.getColumnType(catalog, column, type, this)
            if (result != null) {
                return result
            }
        }
        return null
    }
}