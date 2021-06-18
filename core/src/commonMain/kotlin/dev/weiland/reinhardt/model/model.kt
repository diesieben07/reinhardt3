package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.ReinhardtInternalApi
import dev.weiland.reinhardt.db.Database
import dev.weiland.reinhardt.db.DbRow

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
public annotation class ModelAnnotation

//public interface Database {
//
//    public fun <M : Model, ME : Any, REF : ModelRef<M, ME>> all(modelRef: REF, reader: ModelReader<M, ME>): ModelQuerySet<M, ME, REF>
//
//}
//
//public object DummyDatabase : Database {
//    override fun <M : Model, ME : Any, REF : ModelRef<M, ME>> all(modelRef: REF, reader: ModelReader<M, ME>): ModelQuerySet<M, ME, REF> {
//        return ModelQuerySetImpl(modelRef)
//    }
//}

public interface ModelReader<M : Model, R : Any> {

    public fun readEntity(database: Database, row: DbRow, columnPrefix: String): R {
        return checkNotNull(readEntityNullable(database, row, columnPrefix)) {
            "Entity was null unexpectedly"
        }
    }

    public fun readEntityNullable(database: Database, row: DbRow, columnPrefix: String): R?
}

public interface ModelReaderWithPK<M : Model, R : Any, PK : Any> {

    public fun readPrimaryKey(database: Database, row: DbRow, columnPrefix: String): PK {
        return checkNotNull(readPrimaryKeyNullable(database, row, columnPrefix)) {
            "Primary Key was null unexpectedly"
        }
    }
    public fun readPrimaryKeyNullable(database: Database, row: DbRow, columnPrefix: String): PK?

}


public interface ModelCompanion<M : Model, E : Any> : ModelReader<M, E>

public interface ModelCompanionWithPK<M : Model, E : Any, PK : Any> : ModelCompanion<M, E>, ModelReaderWithPK<M, E, PK> {
    public val primaryKeyField: BasicField<PK>?
}


public interface QuerySet<R> : Iterable<R> {


}

public interface ModelQuerySet<M : Model, ME : Any, REF : ModelRef<M, ME>> : QuerySet<ME> {

    public val ref: REF

    @ReinhardtInternalApi
    public fun addPrefetch(ref: ModelRef<*, *>): ModelQuerySet<M, ME, REF>

    public fun filter(filter: (REF) -> Expr<Boolean>): ModelQuerySet<M, ME, REF>

}

public interface PrefetchDsl {

    public operator fun ModelRef<*, *>.unaryPlus()

}

@PublishedApi
internal object PrefetchDslImpl : PrefetchDsl {
    override fun ModelRef<*, *>.unaryPlus() {
        TODO("Not yet implemented")
    }

}

//inline fun <M : Model, ME : Any, REF : ModelRef<M, ME>, Q : ModelQuerySet<M, ME, REF>> Q.prefetch(refs: (REF) -> ModelRef<*, *>): Q {
//    TODO()
//}

public inline fun <M : Model, ME : Any, REF : ModelRef<M, ME>, Q : ModelQuerySet<M, ME, REF>> Q.prefetch(refs: PrefetchDsl.(REF) -> Unit): Q {
    TODO()
}

public class ModelQuerySetImpl<M : Model, ME : Any, REF : ModelRef<M, ME>>(
    override val ref: REF,
    public val filter: Expr<Boolean>? = null
) : ModelQuerySet<M, ME, REF> {
    override fun iterator(): Iterator<ME> {
        return iterator { }
    }

    @ReinhardtInternalApi
    override fun addPrefetch(ref: ModelRef<*, *>): ModelQuerySet<M, ME, REF> {
        TODO("Not yet implemented")
    }

    override fun filter(filter: (REF) -> Expr<Boolean>): ModelQuerySet<M, ME, REF> {
        return ModelQuerySetImpl(ref, filter = filter(ref))
    }

    override fun toString(): String {
        return "ModelQuerySetImpl(ref=$ref, filter=$filter)"
    }


}

public interface BaseModel {

}

@ModelAnnotation
public abstract class Model : BaseModel {

    private var info: ModelInfo? = null

    internal fun infoOrThrow(): ModelInfo {
        return info ?: throw IllegalStateException("ModelInfo for $this accessed before initialization")
    }

    public fun info(): ModelInfo {
        return info ?: DeferredModelInfo(this)
    }

    @ReinhardtInternalApi
    public fun setInfo(info: ModelInfo) {
        check(this.info == null) {
            "Duplicate ModelInfo for $this"
        }
        this.info = info
    }

    override fun toString(): String {
        return "Model(${this::class.simpleName ?: "<anonymous>"})"
    }
}

private class DeferredModelInfo(private val model: Model) : ModelInfo {
    override val qualifiedName: String
        get() = model.infoOrThrow().qualifiedName
    override val fields: List<Field>
        get() = model.infoOrThrow().fields
    override val primaryKey: BasicField<*>?
        get() = model.infoOrThrow().primaryKey
}

public interface Expr<T>

public data class ValueExpr<T>(val value: T) : Expr<T>
public data class EqExpr<T>(val lhs: Expr<T>, val rhs: Expr<T>) : Expr<Boolean>

public infix fun <T> Expr<T>.eq(rhs: T): Expr<Boolean> = EqExpr(this, ValueExpr(rhs))

// refs
public open class FieldRef<T>(private val modelRef: ModelRef<*, *>, public val fieldName: String) : Expr<T> {

    public fun modelRef(): ModelRef<*, *> = modelRef

    override fun toString(): String {
        return buildString {
            append(modelRef.toString())
            append('.')
            append(fieldName)
        }
    }

}

public abstract class ModelRef<M : Model, ME : Any>(private val through: FieldRef<*>? = null) : Expr<ME> {

    public abstract fun model(): M

    override fun toString(): String {
        return buildString {
            append("Ref(model=")
            append(model())
            if (through != null) {
                append(", through=")
                append(through)
            }
            append(')')
        }
    }

}

//public val db: Database = DummyDatabase

