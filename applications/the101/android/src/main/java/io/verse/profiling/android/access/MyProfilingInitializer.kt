package io.verse.profiling.android.access

import io.tagd.core.Dependencies
import io.tagd.di.key
import io.verse.latch.core.Latch
import io.verse.profiling.adapter.Profiling
import io.verse.profiling.adapter.ProfilingInitializer
import io.verse.profiling.analyzer.Analyzing
import io.verse.profiling.analyzer.AnalyzingConfig
import io.verse.profiling.analyzer.DefaultNetworkAnalyticsReporter
import io.verse.profiling.android.MyApplication
import io.verse.profiling.anomaly.Anomaly
import io.verse.profiling.anomaly.AnomalyConfig
import io.verse.profiling.anomaly.DefaultNetworkAnomalyReporter
import io.verse.profiling.app.ConsoleAnalyticsReporter
import io.verse.profiling.app.ConsoleAnomalyReporter
import io.verse.profiling.app.ConsoleTraceReporter
import io.verse.profiling.core.ProfilingLibrary
import io.verse.profiling.logger.Logging
import io.verse.profiling.logger.LoggingConfig
import io.verse.profiling.logger.registerConsoleLogReporters
import io.verse.profiling.logger.registerNetworkLogReporters
import io.verse.profiling.tracer.DefaultNetworkTraceReporter
import io.verse.profiling.tracer.Tracing
import io.verse.profiling.tracer.TracingConfig
import io.verse.storage.core.Storage
import io.verse.storage.core.sql.SQLiteContext

class MyProfilingInitializer(app: MyApplication) : ProfilingInitializer<MyApplication>(app) {

    override fun new(dependencies: Dependencies): Profiling {
        return super.new(dependencies).also { profiling ->

            profiling.dependsOn(hashMapOf(
                key<Latch>() to {
                    it as Latch
                },
                key<Storage>() to {
                    it as Storage
                },
                key<SQLiteContext>(key = ProfilingLibrary.DATABASE_NAME) to {
                    it as SQLiteContext
                }
            ))
        }
    }

    override fun Logging.Builder.registerLogReporters(): Logging.Builder {
        registerConsoleLogReporters()
        registerNetworkLogReporters()
        return this
    }

    override fun Tracing.Builder.registerTraceReporters(): Tracing.Builder {
        register(ConsoleTraceReporter())
        register(DefaultNetworkTraceReporter())
        return this
    }

    override fun Analyzing.Builder.registerEventReporters(): Analyzing.Builder {
        register(ConsoleAnalyticsReporter())
        register(DefaultNetworkAnalyticsReporter())
        return this
    }

    override fun Anomaly.Builder.registerAnomalyReporters(): Anomaly.Builder {
        register(ConsoleAnomalyReporter())
        register(DefaultNetworkAnomalyReporter())
        return this
    }

    override fun newLoggingConfig() = LoggingConfig("/post-report", 10, hashMapOf())

    override fun newTracingConfig() = TracingConfig("/post-report", 25, hashMapOf())

    override fun newAnalyzingConfig() = AnalyzingConfig("/post-report", 10, hashMapOf())

    override fun newAnomalyConfig() = AnomalyConfig("/post-report", 1, hashMapOf())

}