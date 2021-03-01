package dev.weiland.reinhardt.dbgen

import dev.weiland.reinhardt.dbgen.analyze.PostgresqlDialect
import dev.weiland.reinhardt.dbgen.type.DefaultTypeMapper
import dev.weiland.reinhardt.dbgen.type.PostgresqlTypeMapper
import dev.weiland.reinhardt.dbgen.type.TypeMapper
import org.postgresql.ds.PGSimpleDataSource

fun main() {
    val dataSource = PGSimpleDataSource().apply {
        setURL("jdbc:postgresql://localhost/")
        user = "postgres"
        password = "postgres"
        databaseName = "postgres"
    }

//    dataSource.connection.use { connection ->
//        val tables = connection.metaData.getTables("asd", "public", null, arrayOf("TABLE")).toList {
//            TableInfo(
//                catalog = it.getString("TABLE_CAT"),
//                schema = it.getString("TABLE_SCHEM"),
//                table = it.getString("TABLE_NAME")
//            )
//        }
//        for (table in tables) {
//            val columns = connection.metaData.getColumns(table.catalog, table.schema, table.table, null).toList {
//                println(it.getString("TYPE_NAME"))
//                ColumnInfo(
//                    column = it.getString("COLUMN_NAME"),
//                    jdbcType = JDBCType.valueOf(it.getInt("DATA_TYPE"))
//                )
//            }
//            println("${table.table}: \n$columns\n")
//        }
//    }

//    println(DatabaseAnalyzer::class.java.getResource("/postgresql.information_schema/ADDITIONAL_COLUMN_ATTRIBUTES.sql").readText())
//    exitProcess(0)
    val typeMappers: List<(DatabaseAnalyzer) -> TypeMapper> = listOf({ DefaultTypeMapper() }, ::PostgresqlTypeMapper)

    DatabaseAnalyzer(PostgresqlDialect, dataSource, typeMappers).use { analyzer ->
        analyzer.run()
    }
}

