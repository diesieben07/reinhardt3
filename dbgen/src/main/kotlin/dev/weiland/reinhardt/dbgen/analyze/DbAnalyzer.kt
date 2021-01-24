package dev.weiland.reinhardt.dbgen.analyze

import java.io.Closeable
import javax.sql.DataSource

class DbAnalyzer(private val ds: DataSource) : Closeable {

    @Suppress("UsePropertyAccessSyntax")
    private val connection = ds.getConnection()

//    fun findTables(): List<TableInfo> {
//        connection.createStatement().use { statement ->
//            statement.executeQuery("select * from information_schema.tables where table_schema = 'public'").use { rs ->
//                return buildList {
//                    while (rs.next()) {
//                        add(
//                            TableInfo(
//                                schema = rs.getString("table_schema"),
//                                table = rs.getString("table_name")
//                            )
//                        )
//                    }
//                }
//            }
//        }
//    }

    override fun close() {
        connection.close()
    }

}