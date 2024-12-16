package io.verse.profiling.anomaly

import io.tagd.arch.domain.usecase.argsOf
import io.tagd.arch.scopable.library.usecase
import io.tagd.core.BidirectionalDependentOn
import io.verse.profiling.reporter.PostReportUsecase
import io.verse.profiling.reporter.Report

open class DefaultNetworkAnomalyReporter(
    override val name: String = NAME
): AnomalyReporter, BidirectionalDependentOn<Anomaly> {

    private var library: Anomaly? = null

    override fun injectBidirectionalDependent(other: Anomaly) {
        this.library = other
    }

    override fun report(report: Report<Throwable>) {
        val postAnomalyUsecase = library?.usecase<PostReportUsecase<Throwable, Report<Throwable>>>()
        postAnomalyUsecase?.execute(args = argsOf(PostReportUsecase.ARG_REPORT to report))
    }

    override fun release() {
        library = null
    }

    companion object {
        const val NAME = "default_network_anomaly_reporter"
    }
}