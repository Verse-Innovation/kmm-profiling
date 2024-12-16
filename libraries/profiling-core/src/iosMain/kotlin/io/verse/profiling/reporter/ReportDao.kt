@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.reporter

import io.tagd.langx.Callback
import io.tagd.langx.datatype.UUID
import io.verse.profiling.core.ProfilingLibrary

actual open class ReportDao<REPORT_PAYLOAD : Any?, REPORT : Report<REPORT_PAYLOAD>>
    actual constructor() : IReportDao<REPORT_PAYLOAD, REPORT> {

    override fun injectBidirectionalDependent(other: ProfilingLibrary) {
        TODO("Not yet implemented")
    }

    override fun dispatchDependenciesAreAvailable() {
        TODO("Not yet implemented")
    }

    override fun createReports(
        reports: List<REPORT>,
        success: Callback<Unit>?,
        failure: Callback<Throwable>?,
    ) {
        TODO("Not yet implemented")
    }

    override fun getReports(
        whereClause: String?,
        whereArgs: Array<String?>?,
        ready: Callback<List<REPORT>>
    ) {
        TODO("Not yet implemented")
    }

    override fun deleteReportsWherein(uuids: List<UUID>) {
        TODO("Not yet implemented")
    }

    override fun release() {
        TODO("Not yet implemented")
    }
}