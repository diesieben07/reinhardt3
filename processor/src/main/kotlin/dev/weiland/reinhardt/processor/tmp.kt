package dev.weiland.reinhardt.processor

fun main() {
    val type = AsmType.getType(Array<Array<IntArray>>::class.java)
    val elementType = type.fixedElementType()
    println("${type.descriptor}, ${type.dimensions}")
    println("${type.elementType.descriptor}, ${type.elementType.dimensions}")
    println("${elementType.descriptor}, ${elementType.dimensions}")
}