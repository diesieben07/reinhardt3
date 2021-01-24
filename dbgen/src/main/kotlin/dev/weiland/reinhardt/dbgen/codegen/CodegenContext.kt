package dev.weiland.reinhardt.dbgen.codegen

import dev.weiland.reinhardt.dbgen.NameTransformer
import dev.weiland.reinhardt.dbgen.type.RootTypeMapper
import schemacrawler.schema.Catalog

data class CodegenContext(
    val catalog: Catalog,
    val typeMapper: RootTypeMapper,
    val nameTransformer: NameTransformer
)