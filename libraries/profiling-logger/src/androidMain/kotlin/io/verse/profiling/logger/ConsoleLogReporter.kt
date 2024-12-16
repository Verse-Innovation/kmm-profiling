@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.logger

import android.util.Log

actual class ConsoleLogEReporter : BaseConsoleLogReporter<Logger.SupportedType.LogE, LogE>() {

    override fun report(report: LogE) {
        Log.e("console-logger", report.toString())
    }
}

actual class ConsoleLogVReporter : BaseConsoleLogReporter<Logger.SupportedType.LogV, LogV>() {

    override fun report(report: LogV) {
        Log.v("console-logger", report.toString())
    }
}

actual class ConsoleLogIReporter : BaseConsoleLogReporter<Logger.SupportedType.LogI, LogI>() {

    override fun report(report: LogI) {
        Log.i("console-logger", report.toString())
    }
}

actual class ConsoleLogDReporter : BaseConsoleLogReporter<Logger.SupportedType.LogD, LogD>() {

    override fun report(report: LogD) {
        Log.d("console-logger", report.toString())
    }
}

actual class ConsoleLogEFReporter : BaseConsoleLogReporter<Logger.SupportedType.LogEF, LogEF>() {

    override fun report(report: LogEF) {
        System.out.printf(report.toString(), report.args)
    }
}

actual class ConsoleLogVFReporter : BaseConsoleLogReporter<Logger.SupportedType.LogVF, LogVF>() {

    override fun report(report: LogVF) {
        System.out.printf(report.toString(), report.args)
    }
}

actual class ConsoleLogIFReporter : BaseConsoleLogReporter<Logger.SupportedType.LogIF, LogIF>() {

    override fun report(report: LogIF) {
        System.out.printf(report.toString(), report.args)
    }
}

actual class ConsoleLogDFReporter : BaseConsoleLogReporter<Logger.SupportedType.LogDF, LogDF>() {

    override fun report(report: LogDF) {
        System.out.printf(report.toString(), report.args)
    }
}