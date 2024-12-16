package io.verse.profiling.tracer

import io.verse.profiling.reporter.Reporter
import io.verse.profiling.reporter.ReporterFactory

interface TraceReporter : Reporter<String, Trace<*>>

class TraceReporterFactory(private val forest: TraceForest) :
    ReporterFactory<String, Trace<*>, TraceReporter>(), Foresting {

    private var branch: Branch? = null

    override fun plant(seed: Traceable): Tree<Traceable> {
        return forest.plant(seed)
    }

    override fun branch(branch: String, parent: Branch?, plant: Traceable): Branch {
        return forest.branch(branch, parent, plant).also {
            this.branch = it
        }
    }

    override fun cut(plant: Traceable) {
        forest.cut(plant)
    }

    override fun dispatch(report: Trace<*>) {
        branch?.add(report)
        super.dispatch(report)
    }
}
