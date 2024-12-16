package io.verse.profiling.analyzer

import io.verse.profiling.reporter.Report

open class Event(
    name: String,
    val extras: HashMap<String, Any>, //todo experiment by changing this to Serializable
) : Report<HashMap<String, Any>>(
    name = name,
    payload = extras
) {

    override fun copy(name: String, payload: HashMap<String, Any>): Event {
        return Event(name = name, extras = payload)
    }

    override fun toString(): String {
        return "$name ${if (extras.isEmpty()) "" else "- values $extras"}"
    }
}