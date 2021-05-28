package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.FileSpec

public interface CodegenTarget {

    public fun accept(file: FileSpec)

}