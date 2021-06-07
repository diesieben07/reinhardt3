package dev.weiland.reinhardt.gen

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName

public interface ModelInfoLookup {

    public fun getPrimaryKeyType(modelClass: ClassName): TypeName?

}