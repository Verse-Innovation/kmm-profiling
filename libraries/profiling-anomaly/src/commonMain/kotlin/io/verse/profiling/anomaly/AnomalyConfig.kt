package io.verse.profiling.anomaly

import io.verse.profiling.core.ProfilingLibraryConfig

open class AnomalyConfig(
    reportingUrl: String,
    batchSize: Int,
    reporterConfig: HashMap<String, HashMap<String, Any>>
) : ProfilingLibraryConfig(
    reportingUrl = reportingUrl,
    batchSize = batchSize,
    reporterConfig = reporterConfig
)