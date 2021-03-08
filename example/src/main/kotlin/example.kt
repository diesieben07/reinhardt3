package dev.weiland.reinhardt.example

import dev.weiland.reinhardt.*

object Person : Model() {

    val name = TextField()
    val test = HulloField<Int>()
    val nullableString = TextField().nullable()
    val factory = TextField().nullable()
    val parent = ForeignKey(Person)

    fun getFoo() {

    }

}

object User : Model() {
    val id = TextField()
    val name = TextField()
}

fun main() {
    val qs = db.people.filter { it.name eq "Hello" }
    println(qs)
    for (person in qs) {
        println(person)
    }
}
