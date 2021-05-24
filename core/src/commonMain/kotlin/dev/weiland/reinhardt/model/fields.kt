package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.type.ColumnType
import dev.weiland.reinhardt.type.IntType
import dev.weiland.reinhardt.type.StringType

public class TextField : TypeBasedField<String>() {
    override val type: ColumnType<String>
        get() = StringType
}

public class IntField : TypeBasedField<Int>() {

    override val type: ColumnType<Int>
        get() = IntType

}