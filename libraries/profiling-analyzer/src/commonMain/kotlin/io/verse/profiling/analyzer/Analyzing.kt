package io.verse.profiling.analyzer

import io.tagd.arch.scopable.library.Library
import io.tagd.di.Scope
import io.tagd.di.bind
import io.verse.profiling.core.BaseProfilingLibrary
import io.verse.profiling.core.ProfilingLibrary
import io.verse.profiling.reporter.IReportRepository

typealias AnalyzerProvider<T> = (analyzable: T, factory: AnalyticsReporterFactory) -> Analyzer<T>

class Analyzing private constructor(
    name: String,
    outerScope: Scope
) : BaseProfilingLibrary<AnalyzingConfig>(name = name, outerScope = outerScope, type = TYPE) {

    lateinit var factory: AnalyticsReporterFactory
        private set

    fun <T : Analyzable> newAnalyzer(analyzable: T, provider: AnalyzerProvider<T>): Analyzer<T> {
        return provider.invoke(analyzable, factory)
    }

    override fun release() {
        factory.release()
        super.release()
    }

    companion object {
        const val TYPE = "analyzing"
    }

    class Builder : ProfilingLibrary.Builder<Analyzing, AnalyzingConfig>() {

        private var factory: AnalyticsReporterFactory? = null
        private val reporters = arrayListOf<AnalyticsReporter>()

        override fun name(name: String?): Builder {
            this.name = name
            return this
        }

        override fun scope(outer: Scope?): Builder {
            super.scope(outer)
            return this
        }

        override fun config(config: AnalyzingConfig): Builder {
            this.config = config
            return this
        }

        override fun addRepository(repository: IReportRepository<*, *>?): Builder {
            super.addRepository(repository)
            return this
        }

        fun factory(factory: AnalyticsReporterFactory): Builder {
            this.factory = factory
            return this
        }

        fun register(reporter: AnalyticsReporter): Builder {
            reporters.add(reporter)
            return this
        }

        override fun buildLibrary(): Analyzing {
            return Analyzing(name ?: "${outerScope.name}/$NAME", outerScope).also { library ->
                library.factory = setupReporterFactory()
                library.config = config
                library.repositories.addAll(repositories)

                outerScope.bind<Library, Analyzing>(instance = library)
            }
        }

        @Suppress("LocalVariableName")
        private fun setupReporterFactory(): AnalyticsReporterFactory {
            val _factory = factory ?: AnalyticsReporterFactory()
            reporters.forEach { reporter ->
                _factory.register(reporter.name, reporter)
            }
            return _factory
        }

        companion object {
            const val NAME = "analyzing"
        }
    }
}