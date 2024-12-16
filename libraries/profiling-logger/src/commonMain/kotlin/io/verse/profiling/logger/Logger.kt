package io.verse.profiling.logger

import io.tagd.core.Mappable
import io.verse.profiling.core.BaseProfiler
import io.verse.profiling.core.Profiler
import io.tagd.langx.ref.WeakReference

interface Logger<T : Loggable> : Profiler<T> {

    sealed class SupportedType(val name: String) {
        object LogE : SupportedType(name = LogE::class.toString())
        object LogV : SupportedType(name = LogV::class.toString())
        object LogI : SupportedType(name = LogI::class.toString())
        object LogD : SupportedType(name = LogD::class.toString())
        object LogEF : SupportedType(name = LogEF::class.toString())
        object LogVF : SupportedType(name = LogVF::class.toString())
        object LogIF : SupportedType(name = LogIF::class.toString())
        object LogDF : SupportedType(name = LogDF::class.toString())
    }

    fun e(method: String, message: String?)

    fun ef(method: String, message: String?, vararg args: Any?)

    fun v(method: String, message: String?)

    fun vf(method: String, message: String?, vararg args: Any?)

    fun i(method: String, message: String?)

    fun `if`(method: String, message: String?, vararg args: Any?)

    fun d(method: String, message: String?)

    fun df(method: String, message: String?, vararg args: Any?)
}

open class BaseLogger<T : Loggable>(
    loggableReference: WeakReference<T>,
    private var factory: TypedLogReporterFactory<LogReporter<*, Log>>,
) : BaseProfiler<T>(profileableReference = loggableReference), Logger<T> {

    override fun e(method: String, message: String?) {
        factory.get(Logger.SupportedType.LogE.name)?.let { factory ->
            dispatch(LogE(name = generateName(method), message = message), factory)
        }
    }

    override fun ef(method: String, message: String?, vararg args: Any?) {
        factory.get(Logger.SupportedType.LogEF.name)?.let { factory ->
            dispatch(LogEF(name = generateName(method), message = message, args = args), factory)
        }
    }

    override fun v(method: String, message: String?) {
        factory.get(Logger.SupportedType.LogV.name)?.let { factory ->
            dispatch(LogV(name = generateName(method), message = message), factory)
        }
    }

    override fun vf(method: String, message: String?, vararg args: Any?) {
        factory.get(Logger.SupportedType.LogVF.name)?.let { factory ->
            dispatch(LogVF(name = generateName(method), message = message, args = args), factory)
        }
    }

    override fun i(method: String, message: String?) {
        factory.get(Logger.SupportedType.LogI.name)?.let { factory ->
            dispatch(LogI(name = generateName(method), message = message), factory)
        }
    }

    override fun `if`(method: String, message: String?, vararg args: Any?) {
        factory.get(Logger.SupportedType.LogIF.name)?.let { factory ->
            dispatch(LogIF(name = generateName(method), message = message, args = args), factory)
        }
    }

    override fun d(method: String, message: String?) {
        factory.get(Logger.SupportedType.LogD.name)?.let { factory ->
            dispatch(LogD(name = generateName(method), message = message), factory)
        }
    }

    override fun df(method: String, message: String?, vararg args: Any?) {
        factory.get(Logger.SupportedType.LogDF.name)?.let { factory ->
            dispatch(LogDF(name = generateName(method), message = message, args = args), factory)
        }
    }

    override fun onInitialize(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onInitialize", message = toExtraString(extras))
    }

    override fun onAwaiting(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onAwaiting", message = toExtraString(extras))
    }

    override fun onReady(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onReady", message = toExtraString(extras))
    }

    override fun onProcessing(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onProcessing", message = toExtraString(extras))
    }

    override fun <E : Mappable> onBinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        i(method = alias ?: "onBinding", "$element - $extras")
    }

    override fun onInteraction(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onInteraction", message = toExtraString(extras))
    }

    override fun onReport(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onReport", message = toExtraString(extras))
    }

    override fun track(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "track", message = toExtraString(extras))
    }

    override fun onInterrupt(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onInterrupt", message = toExtraString(extras))
    }

    override fun <E : Mappable> onBindFinish(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        i(method = alias ?: "onBindFinish", message = toExtraString(extras))
    }

    override fun <E : Mappable> onUnbinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        i(method = alias ?: "onBinding", "$element - $extras")
    }

    override fun onRelease(alias: String?, extras: HashMap<String, Any>) {
        i(method = alias ?: "onRelease", message = toExtraString(extras))
    }

    private fun toExtraString(extras: HashMap<String, Any>) =
        if (extras.isEmpty()) "" else extras.toString()
}