package io.verse.profiling.tracer

import io.verse.profiling.core.ProfilingLibraryConfig

open class TracingConfig(
    reportingUrl: String,
    batchSize: Int,
    reporterConfig: HashMap<String, HashMap<String, Any>>
) : ProfilingLibraryConfig(
    reportingUrl = reportingUrl,
    batchSize = batchSize,
    reporterConfig = reporterConfig
)