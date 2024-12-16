package io.verse.profiling.logger

import io.tagd.arch.domain.usecase.argsOf
import io.tagd.arch.scopable.library.usecase
import io.tagd.core.BidirectionalDependentOn
import io.verse.profiling.reporter.PostReportUsecase

open class DefaultNetworkLogReporter<S : Logger.SupportedType, T : Log>(
    override val name: String = NAME,
) : LogReporter<S, T>, BidirectionalDependentOn<Logging> {

    private var library: Logging? = null

    override fun injectBidirectionalDependent(other: Logging) {
        this.library = other
    }

    override fun report(report: T) {
        val postLogUseCase = library?.usecase<PostReportUsecase<String?, T>>()
        postLogUseCase?.execute(args = argsOf(PostReportUsecase.ARG_REPORT to report))
    }

    override fun release() {
        library = null
    }

    companion object {
        const val NAME = "default_network_log_reporter"
    }
}