package test

import dev.weiland.reinhardt.ResultRow
import dev.weiland.reinhardt.type.ArrayType
import dev.weiland.reinhardt.type.ColumnType
import dev.weiland.reinhardt.type.ColumnType.`get`
import dev.weiland.reinhardt.type.IntType
import dev.weiland.reinhardt.type.StringType
import kotlin.Int
import kotlin.String
import kotlin.collections.List

public class TestTable(
  public val id: Int,
  public val nameTemp: List<List<String>>,
  public val otherTableId: String,
  public val otherTableId2: String
) {
  public constructor(row: ResultRow) : this(IntType.`get`(row, "id"), ArrayType.`get`(row,
      "nameTemp"), StringType.`get`(row, "otherTableId"), StringType.`get`(row, "otherTableId2"))
}
