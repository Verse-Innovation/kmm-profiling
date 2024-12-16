package io.verse.profiling.app

import android.util.Log
import io.verse.profiling.analyzer.Event
import io.verse.profiling.analyzer.AnalyticsReporter
import io.verse.profiling.anomaly.AnomalyReporter
import io.verse.profiling.reporter.Report
import io.verse.profiling.tracer.Trace
import io.verse.profiling.tracer.TraceReporter

class ConsoleAnalyticsReporter : AnalyticsReporter {

    override val name: String
        get() = "console-analyzer"

    override fun report(report: Event) {
        Log.i(name, "$report")
    }

    override fun release() {
        Log.i(name, "release")
    }
}

class ConsoleTraceReporter : TraceReporter {

    override val name: String
        get() = "console-tracer"

    override fun report(report: Trace<*>) {
        Log.i(name, "$report")
    }

    override fun release() {
        Log.i(name, "release")
    }
}

class ConsoleAnomalyReporter : AnomalyReporter {

    override val name: String
        get() = "console-anomalizer"

    override fun report(report: Report<Throwable>) {
        Log.i(name, "$report")
    }

    override fun release() {
        Log.i(name, "release")
    }
}