package dev.weiland.reinhardt.dbgen.type

import dev.weiland.reinhardt.dbgen.DatabaseAnalyzer
import dev.weiland.reinhardt.type.ArrayType
import schemacrawler.schema.Catalog
import schemacrawler.schema.Column
import schemacrawler.schema.ColumnDataType
import java.sql.Types
import kotlin.reflect.KTypeProjection
import kotlin.reflect.full.createType

class PostgresqlTypeMapper(private val analyzer: DatabaseAnalyzer) : TypeMapper {

//    private val arrayDimsStatement: PreparedStatement by lazy {
//        val sql = """
//            select a.attndims, a.atttypid from pg_catalog.pg_namespace n
//            join pg_catalog.pg_class c on c.relnamespace = n.oid
//            join pg_catalog.pg_attribute a on a.attrelid = c.oid
//            join pg_catalog.pg_type t on a.atttypid = t.oid
//            WHERE c.relkind in ('r', 'p', 'v', 'f', 'm')
//              and a.attnum > 0
//              AND NOT a.attisdropped
//              AND n.nspname = ?
//              AND c.relname = ?
//              AND a.attname = ?
//        """
//        analyzer.connection.prepareStatement(sql)
//    }
//
//    private fun getArrayDimensionsAndOid(column: Column): Pair<Int, Int> {
//        val statement = arrayDimsStatement
//        statement.setString(1, column.schema.name)
//        statement.setString(2, column.parent.name)
//        statement.setString(3, column.name)
//        statement.executeQuery().use { rs ->
//            require(rs.next()) { "Failed to query Array dimensions for $column" }
//            return Pair(rs.getInt(1), rs.getInt(2))
//        }
//    }
//
//    private fun getArrayElementType(typeOid: Int): Int {
//        val pgConnection = analyzer.connection.unwrap(PgConnection::class.java)
//        return pgConnection.typeInfo.getPGArrayElement(typeOid)
//    }

    override fun getColumnType(catalog: Catalog, column: Column, type: ColumnDataType, root: RootTypeMapper): SimpleColumnType? {
        return if (type.javaSqlType.vendorTypeNumber == Types.ARRAY) {
            val elementTypeName = column.lookupAttribute<String>("PG_EXTRA_ARRAY_ELEM_NAME").orElse(null)
            requireNotNull(elementTypeName) { "Could not find element type name for array column $column" }

            val arrayDims = column.lookupAttribute<Int>("PG_EXTRA_DIMS").orElse(null)
            requireNotNull(arrayDims) { "Could not find dimensions for array column $column" }
            require(arrayDims > 0) { "Invalid array dimensions $arrayDims for array column $column" }

            val elementType = catalog.lookupColumnDataType<ColumnDataType>(column.parent.schema, elementTypeName)
                .or { catalog.lookupSystemColumnDataType(elementTypeName) }
                .orElse(null)

            requireNotNull(elementType) { "invalid element type $elementTypeName for array column $column" }

            println("element type for $column is $elementType")
            val elementCodegenType = root.getColumnType(catalog, column, elementType)
            var resultType = elementCodegenType
            repeat(arrayDims) {
                val listType = List::class.createType(
                    arguments = listOf(KTypeProjection.invariant(resultType.kotlinType))
                )
                resultType = SimpleColumnType(
                    ArrayType(resultType.columnType),
                    listType
                )
            }
            return resultType
        } else null
    }
}