package io.verse.profiling.reporter

import io.tagd.langx.Callback
import io.tagd.di.Scope
import io.verse.profiling.core.ProfilingLibrary
import io.verse.storage.core.sql.SqlDao

class SqlReportDao<P, R : Report<P>>(
    scope: Scope,
    reportingType: String,
) : SqlDao<R>(scope, ProfilingLibrary.DATABASE_NAME, ReportTableBinding(reportingType)) {

    fun deleteWhereIdInAsync(
        values: List<String?>,
        callback: Callback<Int>,
    ) {
        deleteWhereInAsync(ReportTableBinding.COLUMN_REPORT_ID, values, callback)
    }
}
