package example

import dev.weiland.reinhardt.*

interface PersonE {
    val name: String
    val parent: PersonE
}

open class PersonRef(through: FieldRef<*>? = null) : ModelRef<Person, PersonE>(through) {

    override fun model(): Person {
        return Person
    }

    val name = FieldRef<String>(this, "name")
    val parentId = FieldRef<Int?>(this, "parentId")
    val parent by lazy { PersonRef(through = parentId) }
    companion object : PersonRef()
}

object PersonModelReader : ModelReader<Person, PersonE> {
    override fun readEntity(row: DbRow): PersonE {
        TODO("Not yet implemented")
    }
}

val Database.people: ModelQuerySet<Person, PersonE, PersonRef> get() = all(PersonRef, PersonModelReader)