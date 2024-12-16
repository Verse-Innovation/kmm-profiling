package io.verse.profiling.tracer

import io.tagd.arch.scopable.library.Library
import io.tagd.di.Scope
import io.tagd.di.bind
import io.verse.profiling.core.BaseProfilingLibrary
import io.verse.profiling.core.ProfilingLibrary
import io.verse.profiling.reporter.IReportRepository

typealias TracerProvider<T> =
    (traceable: T, callee: Branch?, factory: TraceReporterFactory) -> Tracer<T>

class Tracing private constructor(
    name: String,
    outerScope: Scope,
) : BaseProfilingLibrary<TracingConfig>(name = name, outerScope = outerScope, type = TYPE) {

    lateinit var factory: TraceReporterFactory
        private set

    fun <T : Traceable> newTracer(
        traceable: T,
        callee: Branch? = null,
        provider: TracerProvider<T>,
    ): Tracer<T> {
        return provider.invoke(traceable, callee, factory)
    }

    override fun release() {
        factory.release()
        super.release()
    }

    companion object {
        const val TYPE = "tracing"
    }

    class Builder : ProfilingLibrary.Builder<Tracing, TracingConfig>() {

        private var factory: TraceReporterFactory? = null
        private val reporters: ArrayList<TraceReporter> = arrayListOf()

        override fun name(name: String?): Builder {
            this.name = name
            return this
        }

        override fun scope(outer: Scope?): Builder {
            super.scope(outer)
            return this
        }

        override fun config(config: TracingConfig): Builder {
            this.config = config
            return this
        }

        override fun addRepository(repository: IReportRepository<*, *>?): Builder {
            super.addRepository(repository)
            return this
        }

        fun factory(factory: TraceReporterFactory): Builder {
            this.factory = factory
            return this
        }

        fun register(reporter: TraceReporter): Builder {
            reporters.add(reporter)
            return this
        }

        override fun buildLibrary(): Tracing {
            return Tracing(name ?: "${outerScope.name}/$NAME", outerScope).also { library ->
                library.factory = setupReporterFactory()
                library.config = config
                library.repositories.addAll(repositories)

                outerScope.bind<Library, Tracing>(instance = library)
            }
        }

        @Suppress("LocalVariableName")
        private fun setupReporterFactory(): TraceReporterFactory {
            val _factory = factory ?: TraceReporterFactory(TraceForest())
            reporters.forEach { reporter ->
                _factory.register(reporter.name, reporter)
            }
            return _factory
        }

        companion object {
            const val NAME = "tracing"
        }
    }
}