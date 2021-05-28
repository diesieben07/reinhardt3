package dev.weiland.reinhardt.example

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import dev.weiland.reinhardt.db.Database
import dev.weiland.reinhardt.db.DbRow
import dev.weiland.reinhardt.model.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import java.util.*


object User : Model() {
    val id = TextField().primaryKey().testWrapper()
    val name = TextField().nullable()
    val parent = ForeignKey(User).nullable()
}

// following is generated code

fun User.primaryKey() = id

object UserInfo : ModelInfo {

    init {
        User.setInfo(this)
    }

    override val qualifiedName: String
        get() = "dev.weiland.reinhardt.example.User"
    override val fields: List<Field> = listOf(User.id, User.name, User.parent)
    override val primaryKey: BasicField<*>?
        get() = User.id
}

interface UserEntity {
    val id: String
    val name: String?
    val parentId: String?
    suspend fun parent(): UserEntity
}

private class UserEntityD(
    private val database: Database,
    override val id: String,
    override val name: String?,
    override val parentId: String?,
): UserEntity {

    private var parent: Deferred<UserEntity>? = null

    override suspend fun parent(): UserEntity {
        var parent = this.parent
        if (parent == null) {
            parent = if (parentId == null) {
                CompletableDeferred(null)
            } else {
                database.async {
                    checkNotNull(database.getEntity(UserReader, parentId))
                }
            }
            this.parent = parent
        }
        return parent.await()
    }

}

object UserReader : ModelReader<User, UserEntity> {

    override fun readEntityNullable(row: DbRow, columnPrefix: String): UserEntity? {
        val id = User.id.fromDbNullable(row, "${columnPrefix}id") ?: return null
        val name = User.name.fromDb(row, "${columnPrefix}name")
        val parent = User.primaryKey().fromDbNullable(
            row, "${columnPrefix}parent_id"
        )
        return UserEntityD(row.database, id, name, parent)
    }
}

fun main() {
    print(ServiceLoader.load(SymbolProcessorProvider::class.java).toList())
}
