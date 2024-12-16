package io.verse.profiling.anomaly

import io.tagd.arch.access.library
import io.tagd.arch.scopable.library.Library
import io.tagd.di.Global
import io.tagd.di.Scope
import io.tagd.di.bind
import io.verse.profiling.core.BaseProfilingLibrary
import io.verse.profiling.core.ProfilingLibrary
import io.verse.profiling.reporter.IReportRepository
import io.verse.profiling.reporter.Report

class Anomaly(
    name: String,
    outerScope: Scope
) : BaseProfilingLibrary<AnomalyConfig>(name = name, outerScope = outerScope, type = TYPE) {

    private lateinit var reporters: ArrayList<AnomalyReporter>
    lateinit var factory: AnomalyReporterFactory
        private set

    private var enablePrintStackTrace = true

    fun enablePrintStackTrace(enable: Boolean) {
        enablePrintStackTrace = enable
    }

    fun escalate(exception: Throwable) {
        if (enablePrintStackTrace) {
            exception.printStackTrace()
        }

        reporters.forEach {
            it.report(Report(name = "Exception", payload = exception))
        }
    }

    override fun release() {
        reporters.clear()
        super.release()
    }

    companion object {
        const val TYPE = "anomaly"
    }

    class Builder : ProfilingLibrary.Builder<Anomaly, AnomalyConfig>() {

        private var factory: AnomalyReporterFactory? = null
        private val reporters: ArrayList<AnomalyReporter> = arrayListOf()

        override fun name(name: String?): Builder {
            this.name = name
            return this
        }

        override fun scope(outer: Scope?): Builder {
            super.scope(outer)
            return this
        }

        override fun config(config : AnomalyConfig): Builder {
            this.config = config
            return this
        }

        override fun addRepository(repository: IReportRepository<*, *>?): Builder {
            super.addRepository(repository)
            return this
        }

        fun factory(factory: AnomalyReporterFactory): Builder {
            this.factory = factory
            return this
        }

        fun register(reporter: AnomalyReporter): Builder {
            reporters.add(reporter)
            return this
        }

        override fun buildLibrary(): Anomaly {
            return Anomaly(name ?: "${outerScope.name}/$NAME", outerScope).also { anomaly ->
                anomaly.factory = factory ?: AnomalyReporterFactory()
                anomaly.reporters = reporters
                anomaly.config = config
                anomaly.repositories.addAll(repositories)


                factory?.let { factory ->
                    reporters.forEach { reporter ->
                        val key = "${anomaly.name}_${reporter::class}"
                        factory.register(key, reporter)
                    }
                }

                outerScope.bind<Library, Anomaly>(instance = anomaly)
            }
        }

        companion object {
            const val NAME = "anomaly"
        }
    }
}

fun anomaly(): Anomaly? {
    return Global.anomaly()
}

fun Scope.anomaly(): Anomaly? {
    return this.library<Anomaly>()
}

fun logException(exception: Throwable) {
    Global.logException(exception)
}

fun Scope.logException(exception: Throwable) {
    this.anomaly()?.escalate(exception)
}