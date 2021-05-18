package dev.weiland.reinhardt.jdbc

import dev.weiland.reinhardt.db.DbRow
import java.sql.ResultSet

public interface JdbcDbRow : DbRow {

    public val resultSet: ResultSet

}