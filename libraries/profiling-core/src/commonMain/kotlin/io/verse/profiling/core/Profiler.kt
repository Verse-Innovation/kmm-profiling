package io.verse.profiling.core

import io.tagd.core.Mappable
import io.tagd.core.Releasable
import io.tagd.langx.ref.WeakReference
import io.verse.profiling.reporter.Report
import io.verse.profiling.reporter.Reporter
import io.verse.profiling.reporter.ReporterFactory

interface Profiler<T : Profileable> : Releasable {
    
    val profileable: T?

    /**
     * The generic profileable element is just created, record the same
     */
    fun onInitialize(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is awaiting for the something, record the same
     */
    fun onAwaiting(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is ready to do the designated purpose, record the same
     */
    fun onReady(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is just started the designated purpose, record the same
     */
    fun onProcessing(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is about to bind the interested element E, record the same
     */
    fun <E : Mappable> onBinding(
        alias: String? = null,
        element: E,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is interacted by user/system, record the same
     */
    fun onInteraction(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is interested to report some arbitrary information,
     * record the same
     */
    fun onReport(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is interested to report some arbitrary information,
     * record the same
     */
    fun track(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element processing is interrupted, record the same
     */
    fun onInterrupt(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is done with processing, record the same
     */
    fun <E : Mappable> onBindFinish(
        alias: String? = null,
        element: E,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is unbinding the interest element E, record the same
     */
    fun <E : Mappable> onUnbinding(
        alias: String? = null,
        element: E,
        extras: HashMap<String, Any> = hashMapOf()
    )

    /**
     * The generic profileable element is getting released, record the same
     */
    fun onRelease(
        alias: String? = null,
        extras: HashMap<String, Any> = hashMapOf()
    )
}

abstract class BaseProfiler<T : Profileable>(
    private var profileableReference: WeakReference<T>?
) : Profiler<T> {

    final override val profileable: T?
        get() = profileableReference?.get()

    protected val KEY = "${profileable?.name}_${this::class}"

    protected open fun generateName(method: String): String =
        profileable?.generateName(method) ?: method

    protected fun <P, R : Report<P>, S : Reporter<P, R>> dispatch(
        report: R,
        factory: ReporterFactory<P, R, S>?
    ) {
        factory?.dispatch(report)
    }
    
    override fun release() {
        profileableReference?.clear()
        profileableReference = null
    }
}

fun Profileable?.generateName(method: String) = "${this?.name}:$method"

fun HashMap<*, *>.toExtraString(): String {
    return if (isEmpty()) "" else " with ${toString()}"
}
