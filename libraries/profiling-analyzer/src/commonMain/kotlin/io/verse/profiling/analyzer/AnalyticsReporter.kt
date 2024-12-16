package io.verse.profiling.analyzer

import io.verse.profiling.reporter.Reporter
import io.verse.profiling.reporter.ReporterFactory

interface AnalyticsReporter : Reporter<HashMap<String, Any>, Event>

class AnalyticsReporterFactory : ReporterFactory<HashMap<String, Any>, Event, AnalyticsReporter>()