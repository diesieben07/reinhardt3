package dev.weiland.reinhardt.dbgen.type

import dev.weiland.reinhardt.type.IntType
import dev.weiland.reinhardt.type.StringType
import schemacrawler.schema.Catalog
import schemacrawler.schema.Column
import schemacrawler.schema.ColumnDataType
import schemacrawler.schema.JavaSqlTypeGroup
import java.sql.JDBCType

class DefaultTypeMapper : TypeMapper {

    private val typeGroupMapping: Map<JavaSqlTypeGroup, SimpleColumnType> = mapOf(
        JavaSqlTypeGroup.character to StringType.codegenType()
    )

    private val jdbcTypeMapping = mapOf(
        JDBCType.TINYINT to IntType.codegenType(),
        JDBCType.SMALLINT to IntType.codegenType(),
        JDBCType.INTEGER to IntType.codegenType()
    )

    override fun getColumnType(catalog: Catalog, column: Column, type: ColumnDataType, root: RootTypeMapper): SimpleColumnType? {
        val typeGroup = JavaSqlTypeGroup.valueOf(type.javaSqlType.vendorTypeNumber)
        val typeGroupResult = typeGroupMapping[typeGroup]
        if (typeGroupResult != null) {
            return typeGroupResult
        }

        val jdbcType = try {
            JDBCType.valueOf(column.columnDataType.javaSqlType.vendorTypeNumber)
        } catch (e: IllegalArgumentException) {
            null
        }
        if (jdbcType != null) {
            val jdbcTypeResult = jdbcTypeMapping[jdbcType]
            if (jdbcTypeResult != null) {
                return jdbcTypeResult
            }
        }

        return null
    }
}