package dev.weiland.reinhardt.model

import dev.weiland.reinhardt.ReinhardtInternalApi
import java.lang.UnsupportedOperationException
import java.lang.annotation.Inherited

@Inherited
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.CLASS)
annotation class ModelAnnotation

interface Database {

    fun <M : Model, ME : Any, REF : ModelRef<M, ME>> all(modelRef: REF, reader: ModelReader<M, ME>): ModelQuerySet<M, ME, REF>

}

object DummyDatabase : Database {
    override fun <M : Model, ME : Any, REF : ModelRef<M, ME>> all(modelRef: REF, reader: ModelReader<M, ME>): ModelQuerySet<M, ME, REF> {
        return ModelQuerySetImpl(modelRef)
    }
}

interface DbRow {

}

interface ModelReader<M : Model, R : Any> {

    fun readEntity(row: DbRow): R

}

interface QuerySet<R> : Iterable<R> {


}

interface ModelQuerySet<M : Model, ME : Any, REF : ModelRef<M, ME>> : QuerySet<ME> {

    val ref: REF

    @ReinhardtInternalApi
    fun addPrefetch(ref: ModelRef<*, *>): ModelQuerySet<M, ME, REF>

    fun filter(filter: (REF) -> Expr<Boolean>): ModelQuerySet<M, ME, REF>

}

interface PrefetchDsl {

    operator fun ModelRef<*, *>.unaryPlus()

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

inline fun <M : Model, ME : Any, REF : ModelRef<M, ME>, Q : ModelQuerySet<M, ME, REF>> Q.prefetch(refs: PrefetchDsl.(REF) -> Unit): Q {
    TODO()
}

class ModelQuerySetImpl<M : Model, ME : Any, REF : ModelRef<M, ME>>(
    override val ref: REF,
    val filter: Expr<Boolean>? = null
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

interface BaseModel {

}

@ModelAnnotation
abstract class Model : BaseModel {
    override fun toString(): String {
        return "Model(${this::class.simpleName ?: "<anonymous>"})"
    }
}

interface Expr<T>

data class ValueExpr<T>(val value: T) : Expr<T>
data class EqExpr<T>(val lhs: Expr<T>, val rhs: Expr<T>) : Expr<Boolean>

infix fun <T> Expr<T>.eq(rhs: T): Expr<Boolean> = EqExpr(this, ValueExpr(rhs))

// refs
open class FieldRef<T>(private val modelRef: ModelRef<*, *>, val fieldName: String) : Expr<T> {

    fun modelRef(): ModelRef<*, *> = modelRef

    override fun toString(): String {
        return buildString {
            append(modelRef.toString())
            append('.')
            append(fieldName)
        }
    }

}

abstract class ModelRef<M : Model, ME : Any>(private val through: FieldRef<*>? = null) : Expr<ME> {

    abstract fun model(): M

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

// fields
sealed class Field

fun <F : Field> F.nullable(): NullableField<F> = NullableField(this)

abstract class RelationField<M : Model>(val referencedModel: M) : Field()



abstract class SimpleField<T> : Field()
class NullableField<F : Field>(val delegate: F) : Field()

class TextField : SimpleField<String>() {

}

class HulloField<T> : SimpleField<T>()

class ForeignKey<M : Model>(referencedModel: M) : RelationField<M>(referencedModel) {

}

val db: Database = DummyDatabase

