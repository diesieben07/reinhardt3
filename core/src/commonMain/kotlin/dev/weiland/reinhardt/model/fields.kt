package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.db.DbRow

public class TextField : SimpleField<String>() {
    override fun fromDbNullable(row: DbRow, column: String): String? {
        return row.getString(column)
    }
}

public class IntField : SimpleField<Int>() {

    override fun fromDbNullable(row: DbRow, column: String): Int? {
        val value = row.getInt(column)
        return if (row.wasNull(column)) null else value
    }

}