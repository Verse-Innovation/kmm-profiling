package io.verse.profiling.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.Identifiable
import io.tagd.core.Mappable
import io.tagd.core.State
import io.tagd.langx.time.Millis
import io.tagd.langx.datatype.UUID

class Journey private constructor(
    override val identifier: UUID = UUID()
) : DataObject(), Mappable, Identifiable<UUID> {

    data class Config(
        val ignorableInactiveTimeMs: Millis
    )

    private lateinit var config: Config

    lateinit var scope: ProfilingScope.JourneyScope
        private set

    var watcher: ProfilingScopeLifecycleWatcher? = null
        private set

    private val sessions: ArrayList<Session> = arrayListOf()

    var activeSession: Session? = null
        private set

    val usageScope
        get() = activeSession?.ongoingUsageScope

    val progressiveState: State = State()

    fun update(config: Config) {
        this.config = config
        activeSession?.update(config = Session.Config(config.ignorableInactiveTimeMs))
    }

    fun startSession() {
        if (activeSession == null) {
            newSession()
        } else {
            active()
        }
    }

    fun active() {
        try {
            activeSession?.active()
        } catch (e: Session.SessionExpiredException) {
            e.printStackTrace() // todo report to anomaly

            activeSession = null
            newSession()
        }
    }

    private fun newSession(): Session {
        val session = Session(
            config = Session.Config(config.ignorableInactiveTimeMs), journey = this
        ).apply {
            sessions.add(this)
            activeSession = this
        }
        return session
    }

    fun inactive() {
        activeSession?.inactive()
    }

    fun plusState(property: String, value: Any) {
        progressiveState.put(property, value)
    }

    override fun map(): HashMap<String, Any> {
        return hashMapOf()
    }

    class Builder {

        private lateinit var config: Config
        private lateinit var scope: ProfilingScope.JourneyScope
        private var watcher: ProfilingScopeLifecycleWatcher? = null

        fun config(config: Config): Builder {
            this.config = config
            return this
        }

        fun scope(scope: ProfilingScope.JourneyScope): Builder {
            this.scope = scope
            return this
        }

        fun watcher(watcher: ProfilingScopeLifecycleWatcher?): Builder {
            this.watcher = watcher
            return this
        }

        fun build(): Journey {
            return Journey().also { journey ->
                journey.config = config
                journey.scope = scope
                journey.watcher = watcher
            }
        }
    }
}

