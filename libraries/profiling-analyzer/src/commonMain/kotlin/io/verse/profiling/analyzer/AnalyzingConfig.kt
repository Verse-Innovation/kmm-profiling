package io.verse.profiling.analyzer

import io.verse.profiling.core.ProfilingLibraryConfig

open class AnalyzingConfig(
    reportingUrl: String,
    batchSize: Int,
    reporterConfig: HashMap<String, HashMap<String, Any>>
) : ProfilingLibraryConfig(
    reportingUrl = reportingUrl,
    batchSize = batchSize,
    reporterConfig = reporterConfig
)