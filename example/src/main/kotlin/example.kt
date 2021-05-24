package dev.weiland.reinhardt.example

import dev.weiland.reinhardt.db.DbRow
import dev.weiland.reinhardt.model.ForeignKey
import dev.weiland.reinhardt.model.Model
import dev.weiland.reinhardt.model.ModelReader
import dev.weiland.reinhardt.model.TextField


object User : Model() {
    val id = TextField()
    val name = TextField()
    val parent = ForeignKey(User).nullable()


}

interface UserEntity {
    val id: String
    val name: String
    val parentId: String?
    suspend fun parent(): UserEntity
}

class UserEntityD(
    val id: String,
    val name: String,
    val parentId: String?
) {

    private var parent: Defe

}

object UserReader : ModelReader<User, UserEntityD> {

    override fun readEntityNullable(row: DbRow, columnPrefix: String): UserEntityD? {
        val id = User.id.fromDbNullable(row, "${columnPrefix}id") ?: return null
        val name = User.name.fromDb(row, "${columnPrefix}name")
        val parent = readEntityNullable(row, "${columnPrefix}parent__")
        return UserEntityD(id, name, parent)
    }
}

fun main() {

}
