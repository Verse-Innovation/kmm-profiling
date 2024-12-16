package io.verse.profiling.adapter

import io.tagd.arch.scopable.library.AbstractLibrary
import io.tagd.arch.scopable.library.Library
import io.tagd.core.Service
import io.tagd.di.Key
import io.tagd.di.Scope
import io.tagd.di.bind
import io.verse.profiling.analyzer.Analyzing
import io.verse.profiling.analyzer.AnalyzingConfig
import io.verse.profiling.anomaly.Anomaly
import io.verse.profiling.anomaly.AnomalyConfig
import io.verse.profiling.core.Journey
import io.verse.profiling.core.SandBoxing
import io.verse.profiling.core.ProfilingBoxConfig
import io.verse.profiling.core.ProfilingLibrary
import io.verse.profiling.core.ProfilingLibraryConfig
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.logger.Logging
import io.verse.profiling.logger.LoggingConfig
import io.verse.profiling.tracer.Tracing
import io.verse.profiling.tracer.TracingConfig

class Profiling private constructor(name: String, outerScope: Scope) :
    AbstractLibrary(name, outerScope) {

    var sandBoxing: SandBoxing? = null
        private set

    var logging: Logging? = null
        private set

    var tracing: Tracing? = null
        private set

    var analyzing: Analyzing? = null
        private set

    var anomaly: Anomaly? = null
        private set

    val activeJourney: Journey?
        get() = sandBoxing?.activeJourney

    fun dependsOn(dependencies: HashMap<Key<out Service>, (Service) -> Unit>) {
        val libraries = arrayListOf<ProfilingLibrary>().apply {
            logging?.let { add(it) }
            tracing?.let { add(it) }
            analyzing?.let { add(it) }
            anomaly?.let { add(it) }
        }

        libraries.forEach { library ->
            dependencies.forEach { dependency ->
                library.registerCallback(key = dependency.key, callback = dependency.value)
            }

            library.dependsOn(dependencies.keys.toList())
        }
    }

    fun newJourney(journeyScope: ProfilingScope.JourneyScope): Journey {
        return sandBoxing!!.newJourney(journeyScope)
    }

    fun update(config: ProfilingBoxConfig) {
        sandBoxing?.update(config)
    }

    fun <C : ProfilingLibraryConfig> update(config: C) {
        when (config) {
            is LoggingConfig -> logging?.update(config)
            is TracingConfig -> tracing?.update(config)
            is AnalyzingConfig -> analyzing?.update(config)
            is AnomalyConfig -> anomaly?.update(config)
            else -> {
                val message = "$config must be one of derived instance of ProfilingLibraryConfig"
                throw IllegalArgumentException(message)
            }
        }
    }

    override fun release() {
        logging = null
        tracing = null
        analyzing = null
        anomaly = null
        sandBoxing = null
        super.release()
    }

    class Builder : Library.Builder<Profiling>() {

        private var sandBoxing: SandBoxing? = null
        private var logging: Logging? = null
        private var tracing: Tracing? = null
        private var analyzing: Analyzing? = null
        private var anomaly: Anomaly? = null

        override fun name(name: String?): Builder {
            this.name = name
            return this
        }

        override fun scope(outer: Scope?): Builder {
            super.scope(outer)
            return this
        }

        fun logging(library: Logging?): Builder {
            this.logging = library
            return this
        }

        fun tracing(library: Tracing?): Builder {
            this.tracing = library
            return this
        }

        fun analyzing(library: Analyzing?): Builder {
            this.analyzing = library
            return this
        }

        fun anomaly(library: Anomaly?): Builder {
            this.anomaly = library
            return this
        }

        fun boxing(box: SandBoxing?): Builder {
            this.sandBoxing = box
            return this
        }

        override fun buildLibrary(): Profiling {
            return Profiling(
                name = name ?: "${outerScope.name}/$NAME",
                outerScope = outerScope
            ).also { profiling ->

                profiling.logging = logging
                profiling.tracing = tracing
                profiling.analyzing = analyzing
                profiling.anomaly = anomaly
                profiling.sandBoxing = sandBoxing

                outerScope.bind<Library, Profiling>(instance = profiling)
            }
        }

        companion object {
            const val NAME = "profiling"
        }
    }
}