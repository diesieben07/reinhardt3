package dev.weiland.reinhardt.model

public class ForeignKey<M : Model>(
    referencedModel: M,
    public val nullable: Boolean = false,
    public val eager: Boolean = false,
) : RelationField<M>(referencedModel) {

    public fun nullable(): ForeignKey<M> = ForeignKey(referencedModel, true, eager)
    public fun eager(): ForeignKey<M> = ForeignKey(referencedModel, nullable, true)

}

public class ManyToMany<M : Model>(
    referencedModel: M
) : RelationField<M>(referencedModel)