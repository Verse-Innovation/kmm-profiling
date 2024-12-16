package io.verse.profiling.core

import io.tagd.arch.scopable.library.AbstractLibrary
import io.tagd.arch.scopable.library.Library
import io.tagd.core.Service
import io.tagd.core.ValueProvider
import io.tagd.di.DependentService
import io.tagd.di.Key
import io.tagd.di.Scope
import io.tagd.di.key
import io.verse.latch.core.Latch
import io.verse.profiling.reporter.IReportRepository
import io.verse.profiling.reporter.ReportingConfig
import io.verse.profiling.reporter.ReportingState
import io.verse.storage.core.sql.SQLiteContext

interface ProfilingLibrary : Library, DependentService {

    val type: String

    val config: ProfilingLibraryConfig

    val reportingStateProvider: ValueProvider<ReportingState>

    abstract class Builder<T : ProfilingLibrary, C : ProfilingLibraryConfig> :
        Library.Builder<T>() {

        protected open lateinit var config: C

        protected open val repositories = arrayListOf<IReportRepository<*, *>>()

        open fun config(config: C): Builder<T, C> {
            this.config = config
            return this
        }

        open fun addRepository(repository: IReportRepository<*, *>?): Builder<T, C> {
            repository?.let {
                repositories.add(repository)
            }
            return this
        }
    }

    companion object {
        const val DATABASE_NAME = "reporting_database.db"
        val CREATE_SCHEME_PATHS = listOf("create_report_schema.sql")
        val DELETE_SCHEME_PATHS = listOf("delete_report_schema.sql")
    }
}

open class ProfilingLibraryConfig(
    override val reportingUrl: String,
    override val batchSize: Int,
    override val reporterConfig: HashMap<String, HashMap<String, Any>>
) : ReportingConfig(reportingUrl, batchSize, reporterConfig = reporterConfig)

abstract class BaseProfilingLibrary<C : ProfilingLibraryConfig>(
    name: String,
    outerScope: Scope,
    override val type: String,
) : AbstractLibrary(name, outerScope), ProfilingLibrary {

    override lateinit var config: C
        protected set

    override val dependencyAvailableCallbacks:
        HashMap<Key<out Service>, (service: Service) -> Unit> = hashMapOf()

    override val dependsOnServices: ArrayList<Key<out Service>> = arrayListOf()

    override var state: DependentService.State = DependentService.State.INITIALIZING

    protected val repositories = arrayListOf<IReportRepository<*, *>>()

    override var reportingStateProvider: ValueProvider<ReportingState> =
        ValueProvider {
            ReportingState(canStore(), canSync())
        }

    private fun canStore() =
        ready() || !dependsOnServices.contains(key<SQLiteContext>(ProfilingLibrary.DATABASE_NAME))

    private fun canSync() = ready() || !dependsOnServices.contains(key<Latch>())

    fun addRepository(repository: IReportRepository<*, *>) {
        repositories.add(repository)
        if (state == DependentService.State.READY) {
            dispatchReady(repository)
        }
    }

    fun update(config: C) {
        this.config = config
    }

    override fun onReady() {
        super.onReady()
        repositories.forEach { repository ->
            dispatchReady(repository)
        }
    }

    private fun dispatchReady(repository: IReportRepository<*, *>) {
        repository.dispatchDependenciesAreAvailable()
    }

    override fun release() {
        repositories.clear()
        dependsOnServices.clear()
        super<ProfilingLibrary>.release()
        super<AbstractLibrary>.release()
    }
}