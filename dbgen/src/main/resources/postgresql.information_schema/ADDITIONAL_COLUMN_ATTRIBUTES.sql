select '' TABLE_CATALOG, n.nspname TABLE_SCHEMA, c.relname TABLE_NAME, a.attname COLUMN_NAME,
       a.attndims PG_EXTRA_DIMS, a.atttypid PG_EXTRA_TYPE_OID,
       t.typelem PG_EXTRA_ARRAY_ELEM_OID, el_t.typname PG_EXTRA_ARRAY_ELEM_NAME
from pg_catalog.pg_namespace n
         join pg_catalog.pg_class c on c.relnamespace = n.oid
         join pg_catalog.pg_attribute a on a.attrelid = c.oid
         join pg_catalog.pg_type t on a.atttypid = t.oid
         left join pg_catalog.pg_type el_t on t.typelem != 0 and t.typelem = el_t.oid
WHERE c.relkind in ('r', 'p', 'v', 'f', 'm')
  and a.attnum > 0
  AND NOT a.attisdropped