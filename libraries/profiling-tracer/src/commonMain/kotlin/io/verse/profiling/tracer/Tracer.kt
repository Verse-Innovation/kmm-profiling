package io.verse.profiling.tracer

import io.tagd.core.Mappable
import io.tagd.langx.IllegalAccessException
import io.tagd.langx.isNull
import io.tagd.langx.ref.WeakReference
import io.verse.profiling.core.BaseProfiler
import io.verse.profiling.core.Profiler
import io.verse.profiling.core.toExtraString
import io.verse.profiling.reporter.Report
import kotlin.jvm.Transient

/**
 * 1. Every public method must be traced using visit and leave calls
 * 2. If overridable methods are there, then do the visit call at the parent method and leave call
 * at the child method implementation
 * 3. In between to inform something, use signal method.
 * 4. If anything meaningful is achieved/reached then use mark method. Treat mark as a milestone/
 * stage indicator. For ex - In an Order flow, dispatched, shipped, transit, delivered are the
 * markable stages.
 */
interface Tracer<T : Traceable> : Profiler<T> {

    fun visit(method: String): Branch?

    fun signal(message: String, method: String? = null)

    fun mark(checkpoint: String, method: String? = null)

    fun leave(method: String)
}

open class BaseTracer<T : Traceable>(
    private var traceable: WeakReference<T>,
    callee: Branch? = null,
    private var factory: TraceReporterFactory,
) : BaseProfiler<T>(profileableReference = traceable), Tracer<T> {

    private var currentBranch: Branch? = null
    private var lastLeftBranch: Branch? = null
    private lateinit var tree: Tree<Traceable>

    init {
        profileable?.let { seed ->
            tree = factory.plant(seed)
            callee?.forked = tree
            tree.forkedFrom = callee

            val trace = Trace(tree, null, "tree planted")
            dispatch(trace, factory)
        }
    }

    override fun visit(method: String): Branch? { // branches
        profileable?.let { traceable ->
            if (currentBranch?.name != method) {
                currentBranch = factory.branch(method, currentBranch, traceable)
            }
            val trace = Trace(tree, currentBranch, "visited")
            dispatch(trace, factory)
        }
        return currentBranch
    }

    override fun signal(message: String, method: String?) { // leaves
        profileable?.let {
            resolveBranch(method ?: currentBranch?.name)?.let { visited ->
                val trace = Signal(tree, visited, message)
                dispatch(trace, factory)
            }
        }
    }

    override fun mark(checkpoint: String, method: String?) { // fruits
        profileable?.let {
            resolveBranch(method ?: currentBranch?.name)?.let { visited ->
                val trace = Checkpoint(tree, visited, checkpoint)
                dispatch(trace, factory)
            }
        }
    }

    override fun leave(method: String) {
        val leaving = resolveBranch(method)

        val trace = Trace(tree, leaving, "leaving")
        dispatch(trace, factory)
        lastLeftBranch = leaving
        currentBranch = leaving?.parent
    }

    private fun resolveBranch(method: String?): Branch? {
        val resolved = if (lastLeftBranch?.name == method
            && currentBranch == lastLeftBranch?.parent
        ) {
            lastLeftBranch //consider all the last left branches
        } else {
            if (currentBranch?.name == method) {
                currentBranch
            } else {
                tree.branch(method)
            }
        }

        if (resolved == null || resolved.name != method) {
            resolved?.let {
                throw IllegalAccessException("expecting ${resolved.name} but found $method")
            }
        }
        return resolved
    }

    override fun onInitialize(alias: String?, extras: HashMap<String, Any>) {
        signal("initializing${extras.toExtraString()}")
    }

    override fun onAwaiting(alias: String?, extras: HashMap<String, Any>) {
        signal("awaiting${extras.toExtraString()}")
    }

    override fun onReady(alias: String?, extras: HashMap<String, Any>) {
        signal("ready${extras.toExtraString()}")
    }

    override fun onProcessing(alias: String?, extras: HashMap<String, Any>) {
        signal("processing${extras.toExtraString()}")
    }

    override fun <E : Mappable> onBinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        signal("binding with $element${extras.toExtraString()}")
    }

    override fun onInteraction(alias: String?, extras: HashMap<String, Any>) {
        signal("interacting${extras.toExtraString()}")
    }

    override fun onReport(alias: String?, extras: HashMap<String, Any>) {
        signal("report${extras.toExtraString()}")
    }

    override fun track(alias: String?, extras: HashMap<String, Any>) {
        signal("track${extras.toExtraString()}")
    }

    override fun onInterrupt(alias: String?, extras: HashMap<String, Any>) {
        signal("interrupt${extras.toExtraString()}")
    }

    override fun <E : Mappable> onBindFinish(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        signal("bindFinish${extras.toExtraString()}")
    }

    override fun <E : Mappable> onUnbinding(
        alias: String?,
        element: E,
        extras: HashMap<String, Any>
    ) {
        signal("unbinding with $element${extras.toExtraString()}")
    }

    override fun onRelease(alias: String?, extras: HashMap<String, Any>) {
        signal("release${extras.toExtraString()}")
        profileable?.let { plant ->
            factory.cut(plant)
            val trace = Signal(tree, null, "tree cut")
            dispatch(trace, factory)
        }
    }

    override fun release() {
        super.release()
        traceable.clear()
    }
}

open class Trace<T : Traceable>(
    @Transient val tree: Tree<T>?,
    @Transient val method: Branch?,
    val message: String,
) : Report<String>(
    name = "${tree?.name.orEmpty()}:${method?.name.orEmpty()}",
    payload = message
) {

    private val treeName = tree?.name
    private val branchName = method?.name

    override fun toString(): String {
        val builder = StringBuilder()
        tree?.forkedFrom?.let {
            format(builder, it)
        }
        format(builder, method)

        if (tree.isNull() || tree?.isEmpty() == true) {
            builder.append("|| ")
        }

        return "$builder$name $message"
    }

    private fun format(builder: StringBuilder, method: Branch?) {
        var parent = method
        while (parent != null) {
            builder.append("---- | ")
            parent = parent.parent
        }
    }
}

open class Signal<T : Traceable>(
    tree: Tree<T>?,
    method: Branch?,
    message: String,
) : Trace<T>(tree = tree, method = method, message = message), Leaf {

    override fun toString(): String {
        return "---- | " + super.toString()
    }
}

open class Checkpoint<T : Traceable>(
    tree: Tree<T>?,
    method: Branch,
    message: String,
) : Trace<T>(tree = tree, method = method, message = message), Fruit {

    override fun toString(): String {
        return "---- | " + super.toString()
    }
}

