package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

public data class FieldGenContext(
    val entityInterfaceName: ClassName,
    val entityInterface: TypeSpec.Builder,
    val entityClassName: ClassName,
    val entityClass: TypeSpec.Builder,
    val entityClassConstructor: FunSpec.Builder,
    val entityCompanionName: ClassName,
    val entityCompanion: TypeSpec.Builder,
    val entityReaderReadNullableFun: FunSpec.Builder,
    val entityClassCallParams: MutableList<CodeBlock>,
    val entityReaderReadPKNullableFun: FunSpec.Builder?
)
