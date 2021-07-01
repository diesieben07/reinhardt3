package dev.weiland.reinhardt.expr

public data class Eq<T>(val lhs: Expression<out T>, val rhs: Expression<out T>) : BooleanExpression

public infix fun <T> Expression<out T>.eq(rhs: Expression<out T>): Eq<T> = Eq(this, rhs)