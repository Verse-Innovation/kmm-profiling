package io.verse.profiling.analyzer

import io.tagd.arch.domain.usecase.argsOf
import io.tagd.arch.scopable.library.usecase
import io.tagd.core.BidirectionalDependentOn
import io.verse.profiling.reporter.PostReportUsecase

open class DefaultNetworkAnalyticsReporter(
    override val name: String = NAME,
) : AnalyticsReporter, BidirectionalDependentOn<Analyzing> {

    private var library: Analyzing? = null

    override fun injectBidirectionalDependent(other: Analyzing) {
        this.library = other
    }

    override fun report(report: Event) {
        val postEventUseCase = library?.usecase<PostReportUsecase<HashMap<String, Any>, Event>>()
        postEventUseCase?.execute(args = argsOf(PostReportUsecase.ARG_REPORT to report))
    }

    override fun release() {
        library = null
    }

    companion object {
        const val NAME = "default_network_event_reporter"
    }
}