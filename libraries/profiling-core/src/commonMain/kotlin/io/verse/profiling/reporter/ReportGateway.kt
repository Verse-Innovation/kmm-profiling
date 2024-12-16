@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.reporter


import io.tagd.arch.data.gateway.Gateway
import io.tagd.langx.Callback
import io.tagd.core.BidirectionalDependentOn
import io.verse.profiling.core.ProfilingLibrary

interface IReportGateway<P : Any?, T : Report<P>> : Gateway,
    BidirectionalDependentOn<ProfilingLibrary> {

    fun postReports(
        reports: List<T>,
        success: Callback<Unit>,
        failure: Callback<Throwable>
    )
}

expect class ReportGateway<REPORT_PAYLOAD : Any?, REPORT : Report<REPORT_PAYLOAD>>() :
    IReportGateway<REPORT_PAYLOAD, REPORT>
