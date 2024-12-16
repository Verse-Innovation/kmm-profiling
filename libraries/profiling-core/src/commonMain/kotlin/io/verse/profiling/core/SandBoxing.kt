package io.verse.profiling.core

import io.tagd.arch.scopable.library.AbstractLibrary
import io.tagd.arch.scopable.library.Library
import io.tagd.di.Scope
import io.tagd.di.bind
import io.tagd.langx.IllegalAccessException

class SandBoxing(name: String, outerScope: Scope) : AbstractLibrary(name, outerScope) {

    lateinit var systemScope: ProfilingScope.SystemScope
        private set

    lateinit var mainProcessScope: ProfilingScope.ProcessScope.Main
        private set

    lateinit var forkedProcessScope: ProfilingScope.ProcessScope.Forked
        private set

    lateinit var launcherScope: ProfilingScope.LauncherScope
        private set

    lateinit var applicationScope: ProfilingScope.ApplicationScope
        private set

    lateinit var journeyScope: ProfilingScope.JourneyScope
        private set

    lateinit var sessionScope: ProfilingScope.SessionScope
        private set

    lateinit var usageScope: ProfilingScope.UsageScope
        private set

    var heroScope: ProfilingScope.HeroScope? = null
        private set

    var activeJourney: Journey? = null
        private set

    var watcher: ProfilingScopeLifecycleWatcher? = null
        private set

    private var _config: ProfilingBoxConfig = ProfilingBoxConfig()

    val config: ProfilingBoxConfig
        get() = _config


    fun newSystemScope(profileable: Profileable): ProfilingScope.SystemScope {
        return ProfilingScope.SystemScope(watcher, profileable.weak()).also {
            systemScope = it
        }
    }

    fun newMainProcessScope(profileable: Profileable): ProfilingScope.ProcessScope.Main {
        return ProfilingScope.ProcessScope.Main(systemScope, watcher, profileable.weak()).also {
            mainProcessScope = it
        }
    }

    fun newForkedProcessScope(profileable: Profileable): ProfilingScope.ProcessScope.Forked {
        return ProfilingScope.ProcessScope.Forked(mainProcessScope, watcher, profileable.weak())
            .also {
                forkedProcessScope = it
            }
    }

    fun newLauncherScope(
        processScope: ProfilingScope.ProcessScope =
            if (::mainProcessScope.isInitialized) mainProcessScope else forkedProcessScope,
        triggerType: ProfilingScope.LauncherScope.TriggerType,
        profileable: Profileable
    ): ProfilingScope.LauncherScope {

        return ProfilingScope.LauncherScope(processScope, watcher, triggerType, profileable.weak())
            .also {
                launcherScope = it
            }
    }

    fun newApplicationScope(profileable: Profileable): ProfilingScope.ApplicationScope? {
        return if (::mainProcessScope.isInitialized) {
            if (launcherScope.isWorker()) {
                ProfilingScope.ApplicationScope.Background2Worker(
                    launcherScope,
                    watcher,
                    profileable.weak()
                )
            } else {
                ProfilingScope.ApplicationScope.Background2Foreground(
                    launcherScope,
                    watcher,
                    profileable.weak()
                )
            }.also {
                applicationScope = it
            }
        } else {
            null
        }
    }

    fun newJourneyScope(
        profileable: Profileable? = applicationScope.weakProfilable?.get()
    ): ProfilingScope.JourneyScope {

        return ProfilingScope.JourneyScope(applicationScope, watcher, profileable.weak()).also {
            journeyScope = it
        }
    }

    fun newJourney(
        journeyScope: ProfilingScope.JourneyScope = this.journeyScope
    ): Journey {

        return Journey.Builder()
            .scope(journeyScope)
            .watcher(watcher)
            .config(
                config = Journey.Config(
                    ignorableInactiveTimeMs = config.ignorableSessionInactiveTimeInMs
                )
            ).build().also {
                activeJourney = it
            }
    }

    fun newSessionScope(
        profileable: Profileable? = journeyScope.weakProfilable?.get()
    ): ProfilingScope.SessionScope {

        return ProfilingScope.SessionScope(journeyScope, watcher, profileable.weak()).also {
            sessionScope = it
        }
    }

    fun newUsageScope(profileable: Profileable): ProfilingScope.UsageScope {
        return ProfilingScope.UsageScope(sessionScope, watcher, profileable.weak()).also {
            usageScope = it
        }
    }

    fun newPageScope(profileable: Profileable): ProfilingScope.HeroScope.Page {
        return activeJourney?.usageScope?.let { parentScope ->
            ProfilingScope.HeroScope.Page(parentScope, watcher, profileable.weak()).also {
                heroScope = it
            }
        } ?: throw IllegalAccessException("can not create page scope")
    }

    fun newWorkerScope(profileable: Profileable): ProfilingScope.HeroScope.Worker {
        return activeJourney?.usageScope?.let { parentScope ->
            ProfilingScope.HeroScope.Worker(parentScope, watcher, profileable.weak()).also {
                heroScope = it
            }
        } ?: throw IllegalAccessException("can not create worker scope")
    }

    fun newComponentScope(profileable: Profileable): ProfilingScope.ComponentScope {
        return heroScope?.let { parentScope ->
            ProfilingScope.ComponentScope(parentScope, watcher, profileable.weak())
        } ?: throw IllegalAccessException("can not create component scope")
    }

    fun newContentScope(
        parent: ProfilingScope.ComponentScope,
        profileable: Profileable
    ): ProfilingScope.ContentScope {
        return heroScope?.let { parentScope ->
            ProfilingScope.ContentScope(parent, watcher, profileable.weak())
        } ?: throw IllegalAccessException("can not create component scope")
    }

    fun update(config: ProfilingBoxConfig) {
        this._config = config
        activeJourney?.update(
            config = Journey.Config(
                ignorableInactiveTimeMs = config.ignorableSessionInactiveTimeInMs
            )
        )
    }

    override fun release() {
        heroScope = null
        activeJourney = null
        journeyScope.leave()
        applicationScope.leave()
        launcherScope.leave()
        if (::forkedProcessScope.isInitialized) {
            forkedProcessScope.leave()
        }
        if (::mainProcessScope.isInitialized) {
            mainProcessScope.leave()
        }
        systemScope.leave()

        watcher?.release()
        watcher = null
        super.release()
    }

    class Builder : Library.Builder<SandBoxing>() {

        private var watcher: ProfilingScopeLifecycleWatcher? = null

        override fun name(name: String?): Builder {
            this.name = name
            return this
        }

        override fun scope(outer: Scope?): Builder {
            super.scope(outer)
            return this
        }

        fun watcher(watcher: ProfilingScopeLifecycleWatcher): Builder {
            this.watcher = watcher
            return this
        }

        override fun buildLibrary(): SandBoxing {
            return SandBoxing(
                name = name ?: "${outerScope.name}/$NAME",
                outerScope = outerScope
            ).also { library ->
                library.watcher = watcher

                outerScope.bind<Library, SandBoxing>(instance = library)
            }
        }

        companion object {
            const val NAME = "sand-boxing"
        }
    }
}