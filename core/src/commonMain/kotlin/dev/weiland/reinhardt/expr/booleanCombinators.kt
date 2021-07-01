package dev.weiland.reinhardt.expr

public enum class MultiBooleanOp {

    OR,
    AND

}

public sealed interface BooleanOpExpr : BooleanExpression {

    public val op: MultiBooleanOp

}

public data class DualBooleanExpr(override val op: MultiBooleanOp, val lhs: Expression<Boolean>, val rhs: Expression<Boolean>) : BooleanOpExpr
public data class ManyBooleanExpr(override val op: MultiBooleanOp, val expressions: List<Expression<Boolean>>) : BooleanOpExpr {
    init {
        check(expressions.size >= 2)
    }
}

public infix fun Expression<Boolean>.and(rhs: Expression<Boolean>): BooleanOpExpr {
    return DualBooleanExpr(MultiBooleanOp.AND, this, rhs)
}

public infix fun Expression<Boolean>.or(rhs: Expression<Boolean>): BooleanOpExpr {
    return DualBooleanExpr(MultiBooleanOp.OR, this, rhs)
}