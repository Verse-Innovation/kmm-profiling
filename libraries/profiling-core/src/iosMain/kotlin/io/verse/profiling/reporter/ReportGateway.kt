@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.reporter

import io.tagd.langx.Callback
import io.tagd.core.BidirectionalDependentOn
import io.verse.latch.core.InterceptorGateway
import io.verse.profiling.core.ProfilingLibraryConfig
import io.verse.profiling.core.ProfilingLibrary

actual class ReportGateway<
    REPORT_PAYLOAD : Any?,
    REPORT : Report<REPORT_PAYLOAD>
> actual constructor() :
    InterceptorGateway<List<REPORT>, String, Unit>(),
    IReportGateway<REPORT_PAYLOAD, REPORT>,
    BidirectionalDependentOn<ProfilingLibrary> {

    private lateinit var config: ProfilingLibraryConfig

    override fun injectBidirectionalDependent(other: ProfilingLibrary) {
        config = other.config
    }

    override fun release() {
        TODO("Not yet implemented")
    }

    override fun postReports(
        reports: List<REPORT>,
        success: Callback<Unit>,
        failure: Callback<Throwable>,
    ) {
        TODO("Not yet implemented")
    }
}