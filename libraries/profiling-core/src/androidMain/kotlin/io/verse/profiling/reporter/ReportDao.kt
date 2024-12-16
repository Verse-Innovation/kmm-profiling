@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.reporter

import io.tagd.langx.Callback
import io.tagd.langx.datatype.UUID
import io.verse.profiling.core.ProfilingLibrary

actual open class ReportDao<REPORT_PAYLOAD : Any?, REPORT : Report<REPORT_PAYLOAD>>
    actual constructor() : IReportDao<REPORT_PAYLOAD, REPORT> {

    private var sqlReportDao: SqlReportDao<REPORT_PAYLOAD, REPORT>? = null

    private var library: ProfilingLibrary? = null

    override fun injectBidirectionalDependent(other: ProfilingLibrary) {
        library = other
    }

    override fun dispatchDependenciesAreAvailable() {
        initDelegateDao()
    }

    private fun initDelegateDao() {
        sqlReportDao = SqlReportDao(library!!.thisScope, library!!.type)
    }

    override fun createReports(
        reports: List<REPORT>,
        success: Callback<Unit>?,
        failure: Callback<Throwable>?,
    ) {
        sqlReportDao?.createAsync(reports) {
            if (it > 0) {
                success?.invoke(Unit)
            } else {
                failure?.invoke(Throwable("No records were added"))
            }
        }
    }

    override fun getReports(
        whereClause: String?,
        whereArgs: Array<String?>?,
        ready: Callback<List<REPORT>>
    ) {
        sqlReportDao?.getAllAsync(whereClause, whereArgs, ready)
    }

    override fun deleteReportsWherein(uuids: List<UUID>) {
        uuids.takeIf { it.isNotEmpty() }?.map { it.value }?.let { keys ->
            sqlReportDao?.deleteWhereIdInAsync(keys) { deletedCount ->
                println("deleted reports count: $deletedCount")
            }
        }
    }

    override fun release() {
        library = null
    }
}