package io.verse.profiling.anomaly

import io.verse.profiling.reporter.Report
import io.verse.profiling.reporter.Reporter
import io.verse.profiling.reporter.ReporterFactory

interface AnomalyReporter : Reporter<Throwable, Report<Throwable>>

class AnomalyReporterFactory :
    ReporterFactory<Throwable, Report<Throwable>, Reporter<Throwable, Report<Throwable>>>()