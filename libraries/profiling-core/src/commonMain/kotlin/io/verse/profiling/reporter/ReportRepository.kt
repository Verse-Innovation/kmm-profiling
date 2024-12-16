package io.verse.profiling.reporter

import io.tagd.arch.data.repo.Repository
import io.tagd.langx.Callback
import io.tagd.arch.scopable.library.dao
import io.tagd.arch.scopable.library.gateway
import io.tagd.core.BidirectionalDependentOn
import io.tagd.langx.ref.concurrent.atomic.AtomicBoolean
import io.verse.profiling.core.ProfilingLibrary

interface IReportRepository<P : Any?, T : Report<P>> : Repository,
    BidirectionalDependentOn<ProfilingLibrary> {

    fun dispatchDependenciesAreAvailable()

    fun load()

    fun post(
        report: T,
        success: Callback<Unit>? = null,
        failure: Callback<Throwable>? = null,
    )

    fun postIfAwaiting() //loss of callbacks
}

class ReportRepository<P : Any?, T : Report<P>> : IReportRepository<P, T> {

    private lateinit var library: ProfilingLibrary

    private var gateway: IReportGateway<P, T>? = null
    private var dao: IReportDao<P, T>? = null
    private var batchSize: Int = DEFAULT_BATCH_SIZE

    private lateinit var cache: ArrayList<T>

    override fun injectBidirectionalDependent(other: ProfilingLibrary) {
        this.library = other
        gateway = other.gateway()
        dao = other.dao()
        batchSize = other.config.batchSize
        cache = ArrayList(batchSize)
    }

    override fun dispatchDependenciesAreAvailable() {
        dao?.dispatchDependenciesAreAvailable()
        load()
        postIfAwaiting()
    }

    override fun load() {
        cache.clear()
        dao?.getReports(
            whereClause = "${ReportTableBinding.COLUMN_REPORTING_TYPE}=?",
            whereArgs = arrayOf(library.type)
        ) { reports ->
            if (reports.isNotEmpty()) {
                cache(reports)
                fire(reports)
            }
        }
    }

    override fun postIfAwaiting() {
        fireCache()
    }

    override fun post(
        report: T,
        success: Callback<Unit>?,
        failure: Callback<Throwable>?,
    ) {

        val reports = listOf(report)
        cache(reports)

        val state = library.reportingStateProvider.value()
        if (state.off) {
            success?.invoke(Unit)
        } else {
            if (state.canStore) {
                dao?.createReports(reports)
            }
            if (state.canSync) {
                fire(reports, failure)
            }
        }
    }

    private fun cache(reports: List<T>) {
        cache.addAll(reports) //todo avoid duplicate adds, must be an insertion ordered set
    }

    private val syncing: AtomicBoolean = AtomicBoolean(false)

    private fun fire(
        reports: List<T>,
        failure: Callback<Throwable>? = null,
    ) {

        if (!syncing.get() && cache.size >= batchSize) {
            syncing.set(true)
            val outBatch = ArrayList(cache.subList(0, batchSize))

            gateway?.postReports(reports = outBatch, success = {
                postSuccess(outBatch, reports, failure)
            }, {
                postFailure(failure, it)
            })
        }
    }

    private fun postSuccess(
        outBatch: MutableList<T>,
        reports: List<T>,
        failure: Callback<Throwable>?
    ) {
        cache.removeAll(outBatch)
        dao?.deleteReportsWherein(reports.map { it.identifier })
        syncing.set(false)
        fireCache(failure)
    }

    private fun postFailure(
        failure: Callback<Throwable>?,
        exception: Throwable
    ) {

        failure?.invoke(exception)
        syncing.set(false)
    }

    private fun fireCache(failure: Callback<Throwable>? = null) {
        if (cache.isNotEmpty()) {
            fire(cache, failure)
        }
    }

    override fun release() {
        cache.clear()
        gateway = null
        dao = null
    }

    companion object {
        const val DEFAULT_BATCH_SIZE = 10
    }
}