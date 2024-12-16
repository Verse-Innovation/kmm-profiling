package io.verse.profiling.logger

import io.verse.profiling.core.ProfilingLibraryConfig

open class LoggingConfig(
    reportingUrl: String,
    batchSize: Int,
    reporterConfig: HashMap<String, HashMap<String, Any>>
) : ProfilingLibraryConfig(
    reportingUrl = reportingUrl,
    batchSize = batchSize,
    reporterConfig = reporterConfig
)