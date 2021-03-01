package dev.weiland.reinhardt.dbgen

import com.squareup.kotlinpoet.FileSpec
import dev.weiland.reinhardt.dbgen.analyze.SqlDialect
import dev.weiland.reinhardt.dbgen.codegen.ctx.CodegenContextImpl
import dev.weiland.reinhardt.dbgen.codegen.EntityGenerator
import dev.weiland.reinhardt.dbgen.type.RootTypeMapper
import dev.weiland.reinhardt.dbgen.type.TypeMapper
import schemacrawler.schemacrawler.*
import schemacrawler.utility.SchemaCrawlerUtility
import java.io.Closeable
import java.nio.file.Paths
import java.sql.Connection
import java.sql.JDBCType
import javax.sql.DataSource

class DatabaseAnalyzer(
    val dialect: SqlDialect,
    dataSource: DataSource,
    typeMappers: List<(DatabaseAnalyzer) -> TypeMapper>
) : Closeable {

    private val typeMapper = RootTypeMapper(typeMappers.map { it(this) })

    @Suppress("UsePropertyAccessSyntax")
    val connection: Connection = dataSource.getConnection()

    fun run() {
        val fileBuilder = FileSpec.builder("test", "test")

        val sco = SchemaCrawlerOptions(
            LimitOptionsBuilder.newLimitOptions(),
            FilterOptionsBuilder.newFilterOptions(),
            GrepOptionsBuilder.newGrepOptions(),
            LoadOptionsBuilder.builder()
                .withSchemaInfoLevel(
                    SchemaInfoLevelBuilder.builder()
                        .withInfoLevel(InfoLevel.maximum)
                        .toOptions()
                )
                .toOptions()
        )

        val catalog = SchemaCrawlerUtility.getCatalog(connection, sco)

        val ctx = CodegenContextImpl(catalog, typeMapper, DefaultNameTransformer())
        val gen = EntityGenerator(ctx)

        for (table in catalog.tables.filter { it.name == "test_table" }) {
//            fileBuilder.addType(gen.generate(table))
            fileBuilder.addType(gen.generateImpl(table))
            println(table)

            for (column in table.columns) {
                println("${column.name}: " + JDBCType.valueOf(column.type.javaSqlType.vendorTypeNumber))
                if (column.type.baseType != null) {
                    println("${column.name}: " + JDBCType.valueOf(column.type.baseType.javaSqlType.vendorTypeNumber))
                }
            }
            println()
            println()
        }

        fileBuilder.build().writeTo(Paths.get("dbgen/src/main/generated"))
    }

    override fun close() {
        this.connection.close()
    }
}