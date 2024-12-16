@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.logger

abstract class BaseConsoleLogReporter<S : Logger.SupportedType, T : Log> : LogReporter<S, T> {

    override val name: String
        get() = "console-logger"


    override fun release() {
    }
}

expect class ConsoleLogEReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogE, LogE>

expect class ConsoleLogVReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogV, LogV>

expect class ConsoleLogIReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogI, LogI>

expect class ConsoleLogDReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogD, LogD>

expect class ConsoleLogEFReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogEF, LogEF>

expect class ConsoleLogVFReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogVF, LogVF>

expect class ConsoleLogIFReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogIF, LogIF>

expect class ConsoleLogDFReporter() : BaseConsoleLogReporter<Logger.SupportedType.LogDF, LogDF>
