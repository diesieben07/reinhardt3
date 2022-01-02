package dev.weiland.reinhardt.db

public interface BindingTarget {

    public fun bind(column: String, value: Boolean?)
    public fun bind(column: String, value: Int?)
    public fun bind(column: String, value: Long?)
    public fun bind(column: String, value: Double?)

    public fun bind(column: String, value: String?)

}