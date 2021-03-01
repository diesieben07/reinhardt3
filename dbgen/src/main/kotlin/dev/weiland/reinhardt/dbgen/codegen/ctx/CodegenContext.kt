package dev.weiland.reinhardt.dbgen.codegen.ctx

import dev.weiland.reinhardt.dbgen.NameTransformer
import dev.weiland.reinhardt.dbgen.type.RootTypeMapper
import schemacrawler.schema.Catalog

interface CodegenContext {

    val catalog: Catalog
    val typeMapper: RootTypeMapper
    val nameTransformer: NameTransformer

}