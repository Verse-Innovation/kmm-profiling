package io.verse.profiling.adapter

import io.tagd.arch.control.IApplication
import io.tagd.arch.domain.usecase.Args
import io.tagd.arch.domain.usecase.argsOf
import io.tagd.arch.scopable.library.Library
import io.tagd.core.Mappable
import io.tagd.arch.scopable.module.Module
import io.tagd.langx.ref.WeakReference
import io.verse.profiling.analyzer.Analyzer
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.logger.Logger
import io.verse.profiling.tracer.Branch
import io.verse.profiling.tracer.Tracer

open class ProfilingDelegate<T : ProfileableSubject>(
    subject: T,
    override val name: String,
    override val profilingScope: ProfilingScope,
    callee: Branch? = null
) : ProfileableSubject, Analyzer<T>, Tracer<T>, Logger<T> {

    private var subjectReference: WeakReference<T>? = WeakReference(subject)

    override val profileable: T?
        get() = subjectReference?.get()

    override fun application(): IApplication {
        return profileable?.application()!!
    }

    private var adapter: ProfilingAdapter<T>? = ProfilingAdapter(subjectReference, callee)

    override fun e(method: String, message: String?) {
        adapter?.e(method, message)
    }

    override fun ef(method: String, message: String?, vararg args: Any?) {
        adapter?.ef(method, message, args)
    }

    override fun v(method: String, message: String?) {
        adapter?.v(method, message)
    }

    override fun vf(method: String, message: String?, vararg args: Any?) {
        adapter?.vf(method, message, args)
    }

    override fun i(method: String, message: String?) {
        adapter?.i(method, message)
    }

    override fun `if`(method: String, message: String?, vararg args: Any?) {
        adapter?.`if`(method, message, args)
    }

    override fun d(method: String, message: String?) {
        adapter?.d(method, message)
    }

    override fun df(method: String, message: String?, vararg args: Any?) {
        adapter?.df(method, message, args)
    }

    override fun visit(method: String): Branch? {
        return adapter?.visit(method)
    }

    override fun signal(message: String, method: String?) {
        adapter?.signal(message, method)
    }

    override fun mark(checkpoint: String, method: String?) {
        adapter?.mark(checkpoint, method)
    }

    override fun leave(method: String) {
        adapter?.leave(method)
    }

    override fun onInitialize(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onInitialize(alias, extras)
    }

    override fun onAwaiting(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onAwaiting(alias, extras)
    }

    override fun onReady(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onReady(alias, extras)
    }

    override fun onProcessing(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onProcessing(alias, extras)
    }

    override fun <E : Mappable> onBinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        adapter?.onBinding(alias, element, extras)
    }

    override fun onInteraction(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onInteraction(alias, extras)
    }

    override fun onReport(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onReport(alias, extras)
    }

    override fun track(alias: String?, extras: HashMap<String, Any>) {
        adapter?.track(alias, extras)
    }

    override fun onInterrupt(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onInterrupt(alias, extras)
    }

    override fun <E : Mappable> onBindFinish(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        adapter?.onBindFinish(alias, element, extras)
    }

    override fun <E : Mappable> onUnbinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        adapter?.onUnbinding(alias, element, extras)
    }

    override fun onRelease(alias: String?, extras: HashMap<String, Any>) {
        adapter?.onRelease(alias, extras)
    }

    fun escalate(exception: Exception) {
        adapter?.escalate(exception)
    }

    @Suppress("unused")
    inline fun <reified M : Module, reified T : AnalyticsEventUsecase> trackModuleEvent(
        args: Args = argsOf(),
        extras: Map<String, Any> = emptyMap()
    ) {

        trackModuleEvent<M, T>(this@ProfilingDelegate, args, extras)
    }

    @Suppress("unused")
    inline fun <reified L : Library, reified T : AnalyticsEventUsecase> trackLibraryEvent(
        args: Args = argsOf(),
        extras: Map<String, Any> = emptyMap()
    ) {

        trackLibraryEvent<L, T>(this@ProfilingDelegate, args, extras)
    }

    @Suppress("unused")
    inline fun <reified T : AnalyticsEventUsecase> Module.trackModuleEvent(
        args: Args = argsOf(),
        extras: Map<String, Any> = emptyMap()
    ) {

        this.trackModuleEvent<T>(this@ProfilingDelegate, args, extras)
    }

    @Suppress("unused")
    inline fun <reified T : AnalyticsEventUsecase> Library.trackLibraryEvent(
        args: Args = argsOf(),
        extras: Map<String, Any> = emptyMap()
    ) {

        this.trackLibraryEvent<T>(this@ProfilingDelegate, args, extras)
    }

    override fun release() {
        subjectReference?.clear()
        subjectReference = null
        adapter?.release()
        adapter = null
    }
}