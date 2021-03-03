package dev.weiland.reinhardt.example

import dev.weiland.reinhardt.ForeignKey
import dev.weiland.reinhardt.Model
import dev.weiland.reinhardt.TextField

object Person : Model() {

    val name = TextField()
//    val parent = ForeignKey(Person)
    val foo = TextField.NestedFieldClass()

    fun getFoo() {

    }

}

fun main() {
//    val qs = db.people.filter { it.parent.name eq "Hello" }
//    println(qs)
//    for (person in qs) {
//        println(person)
//    }
}
