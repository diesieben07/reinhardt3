package dev.weiland.reinhardt.pg.gen

import schemacrawler.schemacrawler.DatabaseServerType
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder
import schemacrawler.schemacrawler.LimitOptionsBuilder
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnectionUrlBuilder
import schemacrawler.tools.databaseconnector.DatabaseConnector
import java.sql.Connection
import java.util.function.BiConsumer
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier

private fun buildSchemaRetrievalOptions(schemaRetrievalOptionsBuilder: SchemaRetrievalOptionsBuilder, connection: Connection) {

}

class SchemaCrawlerConnector(
    dbServerType: DatabaseServerType?,
    supportsUrl: Predicate<String>?,
    informationSchemaViewsBuildProcess: BiConsumer<InformationSchemaViewsBuilder, Connection>?,
    schemaRetrievalOptionsBuildProcess: BiConsumer<SchemaRetrievalOptionsBuilder, Connection>?,
    limitOptionsBuildProcess: Consumer<LimitOptionsBuilder>?,
    urlBuildProcess: Supplier<DatabaseConnectionUrlBuilder>?
) : DatabaseConnector(dbServerType, supportsUrl, informationSchemaViewsBuildProcess, schemaRetrievalOptionsBuildProcess, limitOptionsBuildProcess, urlBuildProcess) {



}