package io.verse.profiling.reporter

import io.tagd.core.Nameable
import io.tagd.core.Releasable
import io.tagd.core.Service

interface Reporter<P, T : Report<P>> : Nameable, Releasable {

    fun report(report: T)
}

open class ReporterFactory<P, R : Report<P>, T : Reporter<P, R>> : Service {

    private val reporters = HashMap<String, T>()

    fun register(key: String, reporter: T) {
        reporters[key] = (reporter)
    }

    fun unregister(key: String) {
        reporters.remove(key)
    }

    fun get(key: String): T? {
        return reporters[key]
    }

    fun all(): MutableCollection<T> {
        return reporters.values
    }

    open fun dispatch(report: R) {
        reporters.forEach { reporter ->
            reporter.value.report(report)
        }
    }

    override fun release() {
        reporters.clear()
    }
}

open class AbstractReporterFactory<P, R : Report<P>, T : Reporter<P, R>> : Service {

    private val factories = HashMap<String, ReporterFactory<P, R, T>>()

    fun register(typed: String, factory: ReporterFactory<P, R, T>) {
        factories[typed] = factory
    }

    fun get(typed: String): ReporterFactory<P, R, T>? {
        return factories[typed]
    }

    fun all(): MutableCollection<ReporterFactory<P, R, T>> {
        return factories.values
    }

    fun unregister(typed: String) {
        factories.remove(typed)
    }

    fun dispatch(typed: String, report: R) {
        factories[typed]?.dispatch(report)
    }

    override fun release() {
        factories.clear()
    }
}