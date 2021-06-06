package dev.weiland.reinhardt.model

public class ForeignKey<M : Model>(
    referencedModel: M,
) : RelationField<M>(referencedModel) {

    public fun nullable(): NullableForeignKey<M> = NullableForeignKey(referencedModel)

}

@Target(AnnotationTarget.PROPERTY)
public annotation class Eager

public class NullableForeignKey<M : Model>(
    referencedModel: M
) : RelationField<M>(referencedModel)

public class ManyToMany<M : Model>(
    referencedModel: M
) : RelationField<M>(referencedModel)