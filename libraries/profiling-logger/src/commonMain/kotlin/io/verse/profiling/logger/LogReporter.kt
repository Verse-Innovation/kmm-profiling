package io.verse.profiling.logger

import io.verse.profiling.reporter.AbstractReporterFactory
import io.verse.profiling.reporter.Reporter

interface LogReporter<S : Logger.SupportedType, T : Log> : Reporter<String?, T>

open class TypedLogReporterFactory<T : LogReporter<*, Log>> :
    AbstractReporterFactory<String?, Log, T>()