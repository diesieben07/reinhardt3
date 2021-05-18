package dev.weiland.reinhardt.db

public interface DbResults : DbRow {

    public fun next(): Boolean

}