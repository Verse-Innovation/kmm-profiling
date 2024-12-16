package io.verse.profiling.logger

import io.tagd.arch.scopable.library.Library
import io.tagd.di.Scope
import io.tagd.di.bind
import io.verse.profiling.core.BaseProfilingLibrary
import io.verse.profiling.core.ProfilingLibrary
import io.verse.profiling.reporter.IReportRepository
import io.verse.profiling.reporter.ReporterFactory

typealias LoggerProvider<T> = (
    loggable: T, factory: TypedLogReporterFactory<LogReporter<*, Log>>
) -> Logger<T>

class Logging private constructor(
    name: String,
    outerScope: Scope
) : BaseProfilingLibrary<LoggingConfig>(name = name, outerScope, TYPE) {

    lateinit var factory: TypedLogReporterFactory<LogReporter<*, Log>>
        private set

    private var loggable: DefaultLoggable? = null

    val logger: Logger<DefaultLoggable>?
        get() = loggable?.logger

    fun enableDefaultLogging() {
        loggable = loggable ?: newDefaultLogging()
    }

    private fun newDefaultLogging(): DefaultLoggable {
        return DefaultLoggable(factory).also {
            loggable = it
        }
    }

    fun disableDefaultLogging() {
        loggable?.release()
        loggable = null
    }

    fun <T : Loggable> newLogger(loggable: T, provider: LoggerProvider<T>): Logger<T> {
        return provider.invoke(loggable, factory)
    }

    override fun release() {
        disableDefaultLogging()
        factory.release()
        super.release()
    }

    companion object {
        const val TYPE = "logging"
    }

    class Builder : ProfilingLibrary.Builder<Logging, LoggingConfig>() {

        private var factory: TypedLogReporterFactory<LogReporter<*, Log>>? = null
        private val reporters = hashMapOf<Logger.SupportedType, ArrayList<LogReporter<*, Log>>>()

        override fun name(name: String?): Builder {
            this.name = name
            return this
        }

        override fun scope(outer: Scope?): Builder {
            super.scope(outer)
            return this
        }

        override fun config(config : LoggingConfig): Builder {
            this.config = config
            return this
        }

        override fun addRepository(repository: IReportRepository<*, *>?): Builder {
            super.addRepository(repository)
            return this
        }

        override fun inject(
            bindings: Scope.(Logging) -> Unit
        ): Builder {

            super.inject(bindings)
            return this
        }

        fun factory(factory: TypedLogReporterFactory<LogReporter<*, Log>>): Builder {
            this.factory = factory
            return this
        }

        @Suppress("UNCHECKED_CAST")
        fun <S : Logger.SupportedType, T : Log> register(
            typed: S, reporter: LogReporter<S, T>
        ): Builder {

            if (reporters[typed] == null) {
                reporters[typed] = arrayListOf()
            }
            reporters[typed]?.add(reporter as LogReporter<*, Log>)
            return this
        }

        override fun buildLibrary(): Logging {
            return Logging(name ?: "${outerScope.name}/$NAME", outerScope).also { logging ->
                logging.factory = setupFactoryOfFactories()
                logging.config = config
                logging.repositories.addAll(repositories)

                outerScope.bind<Library, Logging>(instance = logging)
            }
        }

        private fun setupFactoryOfFactories(): TypedLogReporterFactory<LogReporter<*, Log>> {
            val factoryOfFactories = factory ?: TypedLogReporterFactory()
            reporters.forEach { logTypeEntry ->
                val typedFactory = newTypedReporterFactory(logTypeEntry.value)
                factoryOfFactories.register(logTypeEntry.key.name, typedFactory)
            }
            return factoryOfFactories
        }

        private fun newTypedReporterFactory(typedReporters: ArrayList<LogReporter<*, Log>>) =
            ReporterFactory<String?, Log, LogReporter<*, Log>>().apply {
                typedReporters.forEach {
                    register(it.name, it)
                }
            }

        companion object {
            const val NAME = "logging"
        }
    }
}

fun Logging.Builder.registerConsoleLogReporters(): Logging.Builder {
    this.register(Logger.SupportedType.LogE, ConsoleLogEReporter())
        .register(Logger.SupportedType.LogEF, ConsoleLogEFReporter())
        .register(Logger.SupportedType.LogV, ConsoleLogVReporter())
        .register(Logger.SupportedType.LogVF, ConsoleLogVFReporter())
        .register(Logger.SupportedType.LogI, ConsoleLogIReporter())
        .register(Logger.SupportedType.LogIF, ConsoleLogIFReporter())
        .register(Logger.SupportedType.LogD, ConsoleLogDReporter())
        .register(Logger.SupportedType.LogDF, ConsoleLogDFReporter())
    return this
}

fun Logging.Builder.registerNetworkLogReporters(): Logging.Builder {
    this.register(
            Logger.SupportedType.LogE,
            DefaultNetworkLogReporter<Logger.SupportedType.LogE, LogE>()
        )
        .register(
            Logger.SupportedType.LogEF,
            DefaultNetworkLogReporter<Logger.SupportedType.LogEF, LogEF>()
        )
        .register(
            Logger.SupportedType.LogV,
            DefaultNetworkLogReporter<Logger.SupportedType.LogV, LogV>()
        )
        .register(
            Logger.SupportedType.LogVF,
            DefaultNetworkLogReporter<Logger.SupportedType.LogVF, LogVF>()
        )
        .register(
            Logger.SupportedType.LogI,
            DefaultNetworkLogReporter<Logger.SupportedType.LogI, LogI>()
        )
        .register(
            Logger.SupportedType.LogIF,
            DefaultNetworkLogReporter<Logger.SupportedType.LogIF, LogIF>()
        )
        .register(
            Logger.SupportedType.LogD,
            DefaultNetworkLogReporter<Logger.SupportedType.LogD, LogD>()
        )
        .register(
            Logger.SupportedType.LogDF,
            DefaultNetworkLogReporter<Logger.SupportedType.LogDF, LogDF>()
        )
    return this
}
