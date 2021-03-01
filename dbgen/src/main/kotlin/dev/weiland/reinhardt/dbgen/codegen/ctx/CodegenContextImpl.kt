package dev.weiland.reinhardt.dbgen.codegen.ctx

import dev.weiland.reinhardt.dbgen.NameTransformer
import dev.weiland.reinhardt.dbgen.type.RootTypeMapper
import schemacrawler.schema.Catalog

data class CodegenContextImpl(
    override val catalog: Catalog,
    override val typeMapper: RootTypeMapper,
    override val nameTransformer: NameTransformer
) : CodegenContext