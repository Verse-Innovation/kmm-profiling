package io.verse.profiling.analyzer

import io.tagd.core.Mappable
import io.tagd.langx.ref.WeakReference
import io.verse.profiling.core.BaseProfiler
import io.verse.profiling.core.Profiler

interface Analyzer<T : Analyzable> : Profiler<T>

open class BaseAnalyzer<T : Analyzable>(
    analyzableReference: WeakReference<T>,
    private var factory: AnalyticsReporterFactory,
) : BaseProfiler<T>(profileableReference = analyzableReference), Analyzer<T> {

    override fun onInitialize(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "initialize", extras)
    }

    override fun onAwaiting(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "awaiting", extras)
    }

    override fun onReady(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "ready", extras)
    }

    override fun onProcessing(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "processing", extras)
    }

    override fun <E : Mappable> onBinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        dispatchEvent(alias ?: "binding", extras.apply { putAll(element.map()) })
    }

    override fun onInteraction(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "interaction", extras)
    }

    override fun onReport(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "report", extras)
    }

    override fun track(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "track", extras, false)
    }

    override fun onInterrupt(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "interrupt", extras)
    }

    override fun <E : Mappable> onBindFinish(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        dispatchEvent(
            alias ?: "bind_finish",
            extras.apply { putAll(element.map()) },
        )
    }

    override fun <E : Mappable> onUnbinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        dispatchEvent(alias ?: "unbinding", extras.apply { putAll(element.map()) })
    }

    override fun onRelease(alias: String?, extras: HashMap<String, Any>) {
        dispatchEvent(alias ?: "release", extras)
    }

    private fun dispatchEvent(
        alias: String,
        extras: HashMap<String, Any>,
        formatName: Boolean = true
    ) {

        profileable?.let { profileable ->
            dispatch(
                Event(Companion.generateName(profileable, alias, formatName), extras = extras),
                factory
            )
        }
    }

    companion object {

        @Suppress("LocalVariableName")
        fun generateName(analyzable: Analyzable, eventName: String, format: Boolean): String {
            return if (format) {
                var _name = analyzable.name.replace("-", "_")
                val _eventName = separateCamelCaseWordWithUnderscore(eventName)
                _name = separateCamelCaseWordWithUnderscore(_name)
                "event:${_name}:$_eventName"
            } else {
                eventName
            }
        }

        private fun separateCamelCaseWordWithUnderscore(string: String): String {
            val builder = StringBuilder()
            string.forEach {
                if (it.isUpperCase()) {
                    builder.append("_")
                    builder.append(it.lowercaseChar())
                } else {
                    builder.append(it)
                }
            }
            return builder.toString()
        }
    }
}



