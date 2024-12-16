package io.verse.profiling.core

import io.tagd.arch.datatype.DataObject
import io.tagd.langx.time.Millis

data class ProfilingBoxConfig(
    val ignorableSessionInactiveTimeInMs: Millis =
        Millis(DEFAULT_IGNORABLE_SESSION_INACTIVE_TIME_IN_MS)
) : DataObject() {
    
    companion object {
        const val DEFAULT_IGNORABLE_SESSION_INACTIVE_TIME_IN_MS = 15 * 60 * 1000L
    }
}