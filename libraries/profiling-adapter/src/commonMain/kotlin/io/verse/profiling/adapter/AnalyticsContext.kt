package io.verse.profiling.adapter

import io.tagd.arch.access.reference
import io.tagd.arch.infra.ReferenceHolder
import io.tagd.core.Releasable
import io.tagd.core.ValueProvider
import io.tagd.di.key2

abstract class AnalyticsContext : Releasable {

    private val dynamicProperties = hashMapOf<String, ValueProvider<*>>()
    private val staticProperties = hashMapOf<String, Any>()

    private val whitelisted = HashSet<String>()

    abstract fun load()

    open fun set(key: String, value: Any): AnalyticsContext {
        staticProperties[key] = value
        return this
    }

    open fun set(vararg pairs: Pair<String, Any>): AnalyticsContext {
        staticProperties.putAll(pairs)
        return this
    }

    open fun <T> set(key: String, provider: ValueProvider<T>): AnalyticsContext {
        dynamicProperties[key] = provider
        return this
    }

    open fun get(): HashMap<String, Any> {
        val context = hashMapOf<String, Any>()
        context.putAll(staticProperties)
        dynamicProperties.forEach { entry ->
            context[entry.key] = entry.value.value()!!
        }
        return context
    }

    open fun whitelist(event: String) {
        whitelisted.add(event)
    }

    open fun whitelisted(event: String): Boolean {
        return whitelisted.contains(event)
    }

    open fun isSet(event: String): Boolean {
        return staticProperties.contains(event) || dynamicProperties.contains(event)
    }

    override fun release() {
        staticProperties.clear()
        dynamicProperties.clear()
        whitelisted.clear()
    }
}

inline fun <reified T : AnalyticsContext> analyticsContext(): T? {
    return reference(key2<ReferenceHolder<T>, T>())
}