package io.verse.profiling.adapter

import io.tagd.arch.access.library
import io.tagd.arch.scopable.library.Library
import io.tagd.core.Mappable
import io.tagd.di.scopeOf
import io.tagd.langx.IllegalAccessException
import io.tagd.langx.ref.WeakReference
import io.verse.profiling.analyzer.Analyzable
import io.verse.profiling.analyzer.Analyzer
import io.verse.profiling.analyzer.Analyzing
import io.verse.profiling.analyzer.BaseAnalyzer
import io.verse.profiling.anomaly.Anomaly
import io.verse.profiling.core.BaseProfiler
import io.verse.profiling.logger.BaseLogger
import io.verse.profiling.logger.Loggable
import io.verse.profiling.logger.Logger
import io.verse.profiling.logger.Logging
import io.verse.profiling.tracer.BaseTracer
import io.verse.profiling.tracer.Branch
import io.verse.profiling.tracer.Traceable
import io.verse.profiling.tracer.Tracer
import io.verse.profiling.tracer.Tracing

open class ProfilingAdapter<T : ProfileableSubject>(
    private var subjectReference: WeakReference<T>?,
    callee: Branch? = null
) : Tracer<T>, Logger<T>, Analyzer<T> {

    override val profileable: T?
        get() = subjectReference?.get()

    private var analyzer: Analyzer<Analyzable>? = null
        set(value) {
            if (field != null && value != null) {
                throw IllegalAccessException("already set")
            } else {
                field = value
            }
        }

    private var logger: Logger<Loggable>? = null
        set(value) {
            if (field != null && value != null) {
                throw IllegalAccessException("already set")
            } else {
                field = value
            }
        }

    private var tracer: Tracer<Traceable>? = null
        set(value) {
            if (field != null && value != null) {
                throw IllegalAccessException("already set")
            } else {
                field = value
            }
        }

    private var anomaly: Anomaly? = null

    private val profilers: ArrayList<BaseProfiler<*>>

    init {
        val injectedScope = scopeOf<Library, Profiling>()

        anomaly = injectedScope?.library()

        analyzer = injectedScope?.library<Analyzing>()?.newAnalyzer(
            analyzable = subjectReference?.get() as T,
            provider = { analyzable, factory ->
                BaseAnalyzer(WeakReference(analyzable), factory)
            })

        logger = injectedScope?.library<Logging>()?.newLogger(
            loggable = subjectReference?.get() as T,
            provider = { loggable, factory ->
                BaseLogger(WeakReference(loggable), factory)
            })

        tracer = injectedScope?.library<Tracing>()?.newTracer(
            traceable = subjectReference?.get() as T,
            callee = callee,
            provider = { traceable, calleeBranch, factory ->
                BaseTracer(WeakReference(traceable), calleeBranch, factory)
            })

        profilers = arrayListOf<BaseProfiler<*>>().apply {
            analyzer?.let { add(it as BaseProfiler<*>) }
            logger?.let { add(it as BaseProfiler<*>) }
            tracer?.let { add(it as BaseProfiler<*>) }
        }
    }

    override fun onInitialize(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onInitialize(alias, extras)
        }
    }

    override fun onAwaiting(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onAwaiting(alias, extras)
        }
    }

    override fun onReady(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onReady(alias, extras)
        }
    }

    override fun onProcessing(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onProcessing(alias, extras)
        }
    }

    override fun <E : Mappable> onBinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        profilers.forEach {
            it.onBinding(alias, element, extras)
        }
    }

    override fun onInteraction(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onInteraction(alias, extras)
        }
    }

    override fun onReport(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onReport(alias, extras)
        }
    }

    override fun track(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.track(alias, extras)
        }
    }

    override fun onInterrupt(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onInterrupt(alias, extras)
        }
    }

    override fun <E : Mappable> onBindFinish(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        profilers.forEach {
            it.onBindFinish(alias, element, extras)
        }
    }

    override fun <E : Mappable> onUnbinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        profilers.forEach {
            it.onUnbinding(alias, element, extras)
        }
    }

    override fun onRelease(alias: String?, extras: HashMap<String, Any>) {
        profilers.forEach {
            it.onRelease(alias, extras)
        }
    }

    override fun e(method: String, message: String?) {
        logger?.e(method, message)
    }

    override fun ef(method: String, message: String?, vararg args: Any?) {
        logger?.ef(method, message, args)
    }

    override fun v(method: String, message: String?) {
        logger?.v(method, message)
    }

    override fun vf(method: String, message: String?, vararg args: Any?) {
        logger?.vf(method, message, args)
    }

    override fun i(method: String, message: String?) {
        logger?.i(method, message)
    }

    override fun `if`(method: String, message: String?, vararg args: Any?) {
        logger?.`if`(method, message, args)
    }

    override fun d(method: String, message: String?) {
        logger?.d(method, message)
    }

    override fun df(method: String, message: String?, vararg args: Any?) {
        logger?.df(method, message, args)
    }

    override fun visit(method: String): Branch? {
        return tracer?.visit(method)
    }

    override fun signal(message: String, method: String?) {
        tracer?.signal(message, method)
    }

    override fun mark(checkpoint: String, method: String?) {
        tracer?.mark(checkpoint, method)
    }

    override fun leave(method: String) {
        tracer?.leave(method)
    }

    fun escalate(exception: Exception) {
        anomaly?.escalate(exception)
    }

    override fun release() {
        profilers.clear()
        anomaly = null
        analyzer = null
        tracer = null
        logger = null
        subjectReference?.clear()
        subjectReference = null
    }
}