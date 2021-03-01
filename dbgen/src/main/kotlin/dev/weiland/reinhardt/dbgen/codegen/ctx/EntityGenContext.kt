package dev.weiland.reinhardt.dbgen.codegen.ctx

import com.squareup.kotlinpoet.ClassName
import schemacrawler.schema.Table

class EntityGenContext(
    val table: Table,
    val entityClassName: ClassName,
    val companionObjectName: ClassName
)