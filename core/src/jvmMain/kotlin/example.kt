package example

import dev.weiland.reinhardt.*

object Person : Model() {

    val name = TextField()
    val parent = ForeignKey(Person)

}

fun main() {
    val qs = db.people.filter { it.parent.name eq "Hello" }
    println(qs)
    for (person in qs) {
        println(person)
    }
}
