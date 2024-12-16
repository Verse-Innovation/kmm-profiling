@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.reporter

import io.tagd.arch.data.dao.DataAccessObject
import io.tagd.langx.Callback
import io.tagd.core.BidirectionalDependentOn
import io.tagd.langx.datatype.UUID
import io.verse.profiling.core.ProfilingLibrary

interface IReportDao<P : Any?, T : Report<P>> : DataAccessObject,
    BidirectionalDependentOn<ProfilingLibrary> {

    fun dispatchDependenciesAreAvailable()

    fun createReports(
        reports: List<T>,
        success: Callback<Unit>? = null,
        failure: Callback<Throwable>? = null,
    )

    fun getReports(
        whereClause: String? = null,
        whereArgs: Array<String?>? = null,
        ready: Callback<List<T>>,
    )

    fun deleteReportsWherein(uuids: List<UUID>)
}

expect open class ReportDao<REPORT_PAYLOAD : Any?, REPORT : Report<REPORT_PAYLOAD>>() :
    IReportDao<REPORT_PAYLOAD, REPORT>