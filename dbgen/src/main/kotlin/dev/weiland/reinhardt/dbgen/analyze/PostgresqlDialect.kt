package dev.weiland.reinhardt.dbgen.analyze

import org.intellij.lang.annotations.Language
import schemacrawler.schemacrawler.InformationSchemaKey
import schemacrawler.schemacrawler.InformationSchemaViewsBuilder

object PostgresqlDialect : SqlDialect {

    override fun InformationSchemaViewsBuilder.configure(): InformationSchemaViewsBuilder {
        @Language("PostgreSQL")
        val sql = """
            select '' TABLE_CATALOG, n.nspname TABLE_SCHEMA, c.relname TABLE_NAME, a.attname COLUMN_NAME,
            a.attndims n_dims, a.atttypid type_id
            from pg_catalog.pg_namespace n
            join pg_catalog.pg_class c on c.relnamespace = n.oid
            join pg_catalog.pg_attribute a on a.attrelid = c.oid
            join pg_catalog.pg_type t on a.atttypid = t.oid
            WHERE c.relkind in ('r', 'p', 'v', 'f', 'm')
              and a.attnum > 0
              AND NOT a.attisdropped
        """
        return withSql(InformationSchemaKey.ADDITIONAL_COLUMN_ATTRIBUTES, sql)
    }
}