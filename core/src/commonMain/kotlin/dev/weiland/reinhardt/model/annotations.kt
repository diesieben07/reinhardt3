package dev.weiland.reinhardt.model

@Target(AnnotationTarget.ANNOTATION_CLASS)
public annotation class FieldAnnotation

@Target(AnnotationTarget.PROPERTY)
@FieldAnnotation
public annotation class PrimaryKey()

@Target(AnnotationTarget.PROPERTY)
@FieldAnnotation
public annotation class DatabaseName(val name: String)