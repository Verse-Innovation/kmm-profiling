package io.verse.profiling.logger

import io.tagd.arch.access.library
import io.tagd.arch.control.IApplication
import io.tagd.core.Releasable
import io.tagd.langx.ref.WeakReference
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.core.SandBoxing

class DefaultLoggable(factory: TypedLogReporterFactory<LogReporter<*, Log>>) : Loggable,
    Releasable {

    override val name: String
        get() = "default-logging"

    override fun application(): IApplication {
        return io.tagd.arch.control.application()!!
    }

    override val profilingScope: ProfilingScope
        get() = library<SandBoxing>()?.applicationScope!!

    var logger: BaseLogger<DefaultLoggable>? = BaseLogger(WeakReference(this), factory)

    override fun release() {
        logger?.release()
        logger = null
    }
}