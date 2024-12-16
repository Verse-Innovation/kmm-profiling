package io.verse.profiling.tracer

import io.tagd.arch.domain.usecase.argsOf
import io.tagd.arch.scopable.library.usecase
import io.tagd.core.BidirectionalDependentOn
import io.verse.profiling.reporter.PostReportUsecase

open class DefaultNetworkTraceReporter(
    override val name: String = NAME,
) : TraceReporter, BidirectionalDependentOn<Tracing> {

    private var library: Tracing? = null

    override fun injectBidirectionalDependent(other: Tracing) {
        this.library = other
    }

    override fun report(report: Trace<*>) {
        val postTraceUseCase = library?.usecase<PostReportUsecase<String, Trace<*>>>()
        postTraceUseCase?.execute(args = argsOf(PostReportUsecase.ARG_REPORT to report))
    }

    override fun release() {
        library = null
    }

    companion object {
        const val NAME = "default_network_trace_reporter"
    }
}