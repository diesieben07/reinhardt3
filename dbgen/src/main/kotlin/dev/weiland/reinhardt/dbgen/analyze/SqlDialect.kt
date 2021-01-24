package dev.weiland.reinhardt.dbgen.analyze

import schemacrawler.schemacrawler.InformationSchemaViewsBuilder
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder

interface SqlDialect {

    fun SchemaRetrievalOptionsBuilder.configure(): SchemaRetrievalOptionsBuilder = this
    fun InformationSchemaViewsBuilder.configure(): InformationSchemaViewsBuilder = this

}