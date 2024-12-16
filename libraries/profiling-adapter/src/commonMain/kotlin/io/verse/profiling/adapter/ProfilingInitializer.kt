package io.verse.profiling.adapter

import io.tagd.arch.access.library
import io.tagd.arch.control.application
import io.tagd.arch.data.dao.DataAccessObject
import io.tagd.arch.data.gateway.Gateway
import io.tagd.arch.data.repo.Repository
import io.tagd.arch.domain.usecase.Command
import io.tagd.arch.scopable.AbstractWithinScopableInitializer
import io.tagd.arch.scopable.Scopable
import io.tagd.arch.scopable.library.Library
import io.tagd.core.BidirectionalDependentOn
import io.tagd.core.Dependencies
import io.tagd.di.Scope
import io.tagd.di.bindLazy
import io.tagd.di.key
import io.tagd.di.layer
import io.tagd.langx.Callback
import io.tagd.langx.Context
import io.verse.app.AppBundleLibrary
import io.verse.profiling.analyzer.AnalyticsReporterFactory
import io.verse.profiling.analyzer.Analyzing
import io.verse.profiling.analyzer.AnalyzingConfig
import io.verse.profiling.analyzer.Event
import io.verse.profiling.anomaly.Anomaly
import io.verse.profiling.anomaly.AnomalyConfig
import io.verse.profiling.anomaly.AnomalyReporterFactory
import io.verse.profiling.core.ProfilingLibrary
import io.verse.profiling.core.ProfilingScopeLifecycleWatcher
import io.verse.profiling.core.SandBoxing
import io.verse.profiling.logger.Log
import io.verse.profiling.logger.LogReporter
import io.verse.profiling.logger.Logging
import io.verse.profiling.logger.LoggingConfig
import io.verse.profiling.logger.TypedLogReporterFactory
import io.verse.profiling.reporter.IReportDao
import io.verse.profiling.reporter.IReportGateway
import io.verse.profiling.reporter.IReportRepository
import io.verse.profiling.reporter.PostReportUsecase
import io.verse.profiling.reporter.Report
import io.verse.profiling.reporter.ReportDao
import io.verse.profiling.reporter.ReportGateway
import io.verse.profiling.reporter.ReportRepository
import io.verse.profiling.tracer.Trace
import io.verse.profiling.tracer.TraceForest
import io.verse.profiling.tracer.TraceReporterFactory
import io.verse.profiling.tracer.Tracing
import io.verse.profiling.tracer.TracingConfig
import io.verse.storage.core.sql.SQLiteConfig
import io.verse.storage.core.sql.SQLiteOpenHelper

abstract class ProfilingInitializer<S : Scopable>(within: S) :
    AbstractWithinScopableInitializer<S, Profiling>(within) {

    override fun initialize(callback: Callback<Unit>) {
        outerScope.bindLazy<Library, Profiling> {
            new(newDependencies())
        }

        super.initialize(callback)
    }

    override fun new(dependencies: Dependencies): Profiling {
        val outerScope = dependencies.get<Scope>(ARG_OUTER_SCOPE)!!

        val boxing = setupSandBoxing(outerScope)
        val logging =
            setupLogging(outerScope, newReportDao(), newReportGateway(), newReportRepository())
        val tracing =
            setupTracing(outerScope, newReportDao(), newReportGateway(), newReportRepository())
        val analyzing =
            setupAnalyzing(outerScope, newReportDao(), newReportGateway(), newReportRepository())
        val anomaly =
            setupAnomaly(outerScope, newReportDao(), newReportGateway(), newReportRepository())

        return Profiling.Builder()
            .scope(outerScope)
            .boxing(boxing)
            .logging(logging)
            .tracing(tracing)
            .analyzing(analyzing)
            .anomaly(anomaly)
            .build().also { library ->
                setupSqlite(library.thisScope)
            }
    }

    protected open fun setupSandBoxing(scope: Scope?): SandBoxing {
        return SandBoxing.Builder().scope(scope).watcher(
            watcher = ProfilingScopeLifecycleWatcher(
                usageProvider = {
                    library<AppBundleLibrary>()?.appBundle!!
                }
            )
        ).build()
    }

    protected open fun <P : Any?, T : Report<P>> newReportDao(): IReportDao<P, T> {
        return ReportDao()
    }

    protected open fun <P : Any?, T : Report<P>> newReportGateway(): IReportGateway<P, T> {
        return ReportGateway()
    }

    protected open fun <P : Any?, T : Report<P>> newReportRepository(): IReportRepository<P, T> {
        return ReportRepository()
    }

    protected open fun setupLogging(
        scope: Scope?,
        reportDao: IReportDao<String?, Log>? = null,
        reportGateway: IReportGateway<String?, Log>? = null,
        reportRepository: IReportRepository<String?, Log>? = null,
    ): Logging {

        return Logging.Builder()
            .scope(scope)
            .config(newLoggingConfig())
            .addRepository(reportRepository)
            .factory(newTypedLogReporterFactory())
            .registerLogReporters()
            .injectDependencies(reportDao, reportGateway, reportRepository) { library ->
                library.factory.all().forEach { factory ->
                    factory.all().filter { reporter ->
                        reporter is BidirectionalDependentOn<*>
                    }.forEach { dependentReporter ->

                        @Suppress("UNCHECKED_CAST")
                        (dependentReporter as BidirectionalDependentOn<Logging>)
                            .injectBidirectionalDependent(library)
                    }
                }
            }
            .build().also {
                it.enableDefaultLogging()
            }
    }

    protected open fun newLoggingConfig() =
        LoggingConfig("/post-report", 50, hashMapOf())

    protected open fun newTypedLogReporterFactory(): TypedLogReporterFactory<LogReporter<*, Log>> =
        TypedLogReporterFactory()

    protected abstract fun Logging.Builder.registerLogReporters(): Logging.Builder

    protected open fun setupTracing(
        scope: Scope?,
        reportDao: IReportDao<String, Trace<*>>? = null,
        reportGateway: IReportGateway<String, Trace<*>>? = null,
        reportRepository: IReportRepository<String, Trace<*>>? = null,
    ): Tracing {

        return Tracing.Builder()
            .scope(scope)
            .config(newTracingConfig())
            .addRepository(reportRepository)
            .factory(newTraceReporterFactory())
            .registerTraceReporters()
            .injectDependencies(reportDao, reportGateway, reportRepository) { library ->
                library.factory.all().filter { reporter ->
                    reporter is BidirectionalDependentOn<*>
                }.forEach { dependentReporter ->

                    @Suppress("UNCHECKED_CAST")
                    (dependentReporter as BidirectionalDependentOn<Tracing>)
                        .injectBidirectionalDependent(library)
                }
            }
            .build()
    }

    protected open fun newTracingConfig() =
        TracingConfig("/post-report", 100, hashMapOf())

    protected open fun newTraceReporterFactory() = TraceReporterFactory(newTraceForest())

    protected open fun newTraceForest() = TraceForest()

    protected abstract fun Tracing.Builder.registerTraceReporters(): Tracing.Builder

    protected open fun setupAnalyzing(
        scope: Scope?,
        reportDao: IReportDao<HashMap<String, Any>, Event>? = null,
        reportGateway: IReportGateway<HashMap<String, Any>, Event>? = null,
        reportRepository: IReportRepository<HashMap<String, Any>, Event>? = null,
    ): Analyzing {

        return Analyzing.Builder()
            .scope(scope)
            .config(newAnalyzingConfig())
            .addRepository(reportRepository)
            .factory(newEventReporterFactory())
            .registerEventReporters()
            .injectDependencies(reportDao, reportGateway, reportRepository) { library ->
                library.factory.all().filter { reporter ->
                    reporter is BidirectionalDependentOn<*>
                }.forEach { dependentReporter ->

                    @Suppress("UNCHECKED_CAST")
                    (dependentReporter as BidirectionalDependentOn<Analyzing>)
                        .injectBidirectionalDependent(library)
                }
            }
            .build()
    }

    protected open fun newAnalyzingConfig() =
        AnalyzingConfig("/post-report", 50, hashMapOf())

    protected open fun newEventReporterFactory() = AnalyticsReporterFactory()

    protected abstract fun Analyzing.Builder.registerEventReporters(): Analyzing.Builder

    protected open fun setupAnomaly(
        scope: Scope?,
        reportDao: IReportDao<Throwable, Report<Throwable>>? = null,
        reportGateway: IReportGateway<Throwable, Report<Throwable>>? = null,
        reportRepository: IReportRepository<Throwable, Report<Throwable>>? = null,
    ): Anomaly {

        return Anomaly.Builder()
            .scope(scope)
            .config(newAnomalyConfig())
            .addRepository(reportRepository)
            .factory(newAnomalyReporterFactory())
            .registerAnomalyReporters()
            .injectDependencies(reportDao, reportGateway, reportRepository) { library ->
                library.factory.all().filter { reporter ->
                    reporter is BidirectionalDependentOn<*>
                }.forEach { dependentReporter ->

                    @Suppress("UNCHECKED_CAST")
                    (dependentReporter as BidirectionalDependentOn<Anomaly>)
                        .injectBidirectionalDependent(library)
                }
            }
            .build()
    }

    protected open fun newAnomalyConfig() =
        AnomalyConfig("/post-report", 10, hashMapOf())

    protected open fun newAnomalyReporterFactory() = AnomalyReporterFactory()

    protected abstract fun Anomaly.Builder.registerAnomalyReporters(): Anomaly.Builder

    private fun <
        T : ProfilingLibrary,
        P : Any?,
        R : Report<P>,
    > ProfilingLibrary.Builder<T, *>.injectDependencies(
        reportDao: IReportDao<P, R>? = null,
        reportGateway: IReportGateway<P, R>? = null,
        reportRepository: IReportRepository<P, R>? = null,
        continuation: Callback<T>? = null
    ): ProfilingLibrary.Builder<T, *> {

        inject { library ->
            injectInternalDependencies(library, reportDao, reportGateway, reportRepository)
            continuation?.invoke(library)
        }

        return this
    }

    private fun setupSqlite(scope: Scope) {
        val application = (application() as? Context)!!
        val config = SQLiteConfig(
            context = application,
            databaseName = ProfilingLibrary.DATABASE_NAME,
            createSchemaPaths = ProfilingLibrary.CREATE_SCHEME_PATHS,
            deleteSchemaPaths = ProfilingLibrary.DELETE_SCHEME_PATHS,
            scope = scope
        )
        val sqLiteOpenHelper = SQLiteOpenHelper()
        sqLiteOpenHelper.openWith(config) {
            println("opened sqlite")
        }
    }


    companion object {
        const val ARG_OUTER_SCOPE = "outer_scope"
    }
}

private fun <T : ProfilingLibrary, P : Any?, R : Report<P>> Scope.injectInternalDependencies(
    library: T,
    dao: IReportDao<P, R>?,
    gateway: IReportGateway<P, R>?,
    repo: IReportRepository<P, R>?,
) {

    layer<DataAccessObject> {
        dao?.let {
            bind(key(), it)
            it.injectBidirectionalDependent(library)
        }
    }
    layer<Gateway> {
        gateway?.let {
            bind(key(), it)
            it.injectBidirectionalDependent(library)
        }
    }
    layer<Repository> {
        repo?.let {
            bind(key(), it)
            it.injectBidirectionalDependent(library)
        }
    }
    layer<Command<*, *>> {
        bind(key(), PostReportUsecase<P, R>().also {
            it.injectBidirectionalDependent(library)
        })
    }
}