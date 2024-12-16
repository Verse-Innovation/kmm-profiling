package io.verse.profiling.core

import io.tagd.core.Nameable
import io.tagd.langx.IllegalAccessException
import io.tagd.langx.System
import io.tagd.langx.datatype.UUID
import io.tagd.langx.ref.WeakReference
import io.tagd.langx.time.Interval
import io.tagd.langx.time.Millis
import io.tagd.langx.time.UnixEpochInMillis

interface Scopable {

    val profilingScope: ProfilingScope
}

sealed class ProfilingScope(
    open val rank: Rank,
    open val parent: ProfilingScope? = null,
    open val watcher: ProfilingScopeLifecycleWatcher? = null,
    profileableReference: WeakReference<Profileable?>
) : Nameable, Comparable<ProfilingScope> {

    enum class Rank {
        /**
         * Right from the system boot to system shutdown in the context of app. The app must
         * persist system boots to monitor them effectively
         */
        System,

        /**
         * Right from the process creation to process kill, indicates one process scope. The app
         * must persist process launches to monitor them effectively
         */
        Process,

        /**
         * Right from the process creation to process kill, indicates one launcher scope
         */
        Launcher,

        /**
         * Right from the process creation to process kill, indicates one application scope
         */
        Application,

        /**
         * Right from the process creation to process kill, indicates the business journey of the
         * user
         */
        Journey,

        /**
         * Multiple visible periods defined as per business
         */
        Session,

        /**
         * The visible period of the app
         */
        Usage,

        /**
         * The page or worker, the main activity component of the app
         */
        Hero,

        /**
         * An enclosed granular component within a hero component
         */
        Component,

        /**
         * The typical content life cycle scope
         */
        Content
    }

    val uuid: String

    var sequence: Int = 0
        private set

    var visits: Int = 0
        private set

    lateinit var visitedAt: Millis
        private set

    lateinit var leftAt: Millis
        private set

    private var lazyWeakProfileable: WeakReference<Profileable?>? = null

    var weakProfilable: WeakReference<Profileable?>? = lazyWeakProfileable
        set(value) {
            if (lazyWeakProfileable == null) {
                lazyWeakProfileable = value
            } else {
                throw IllegalAccessException("already set")
            }
            field = value
        }

    init {
        weakProfilable = profileableReference
        uuid = UUID().value
        initialize()
    }

    open fun initialize() {
        visit()
    }

    private fun visit() {
        watcher?.onVisit(this)?.let { stats ->
            visitedAt = Millis(System.millis())
            sequence = stats.sequence
            visits = stats.visits
        }
    }

    fun leave() {
        watcher?.onLeave(this)?.let {
            leftAt = Millis(System.millis())
        }
    }

    override fun compareTo(other: ProfilingScope): Int {
        return this.rank.ordinal - other.rank.ordinal
    }

    data class SystemScope(
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.System,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        init {
            initialize()
        }

        override val name: String
            get() = "system-scope"
    }

    sealed class ProcessScope(
        parent: ProfilingScope,
        watcher: ProfilingScopeLifecycleWatcher? = null,
        open var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Process,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        data class Main(
            override val parent: SystemScope,
            override val watcher: ProfilingScopeLifecycleWatcher? = null,
            override var profileableReference: WeakReference<Profileable?>
        ) : ProcessScope(
            parent = parent,
            watcher = watcher,
            profileableReference = profileableReference
        ) {

            init {
                initialize()
            }

            override val name: String
                get() = "main-process-scope"
        }

        data class Forked(
            override val parent: Main,
            override val watcher: ProfilingScopeLifecycleWatcher? = null,
            override var profileableReference: WeakReference<Profileable?>
        ) : ProcessScope(
            parent = parent,
            watcher = watcher,
            profileableReference = profileableReference
        ) {

            init {
                initialize()
            }

            override val name: String
                get() = "forked-process-scope"
        }
    }

    data class LauncherScope(
        override val parent: ProcessScope,
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        val triggerType: TriggerType,
        var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Launcher,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        init {
            initialize()
        }

        sealed class TriggerType {

            object AppIcon : TriggerType()

            object Recents : TriggerType()

            data class Notification(val notificationJson: String) : TriggerType()

            data class DeepLink(val deepLink: String) : TriggerType()

            data class DeferredDeepLink(val deferredDeepLink: String) : TriggerType()

            data class OtherApp(val appPackage: String) : TriggerType()

            data class Job(val job: String) : TriggerType()

            data class Event(val event: String) : TriggerType()

            object System : TriggerType()
        }

        fun isWorker(): Boolean {
            return triggerType is TriggerType.Event || triggerType is TriggerType.Job
        }

        override val name: String
            get() = "launcher-scope"
    }

    sealed class ApplicationScope(
        parent: LauncherScope,
        watcher: ProfilingScopeLifecycleWatcher? = null,
        profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Application,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        data class Background2Foreground(
            override val parent: LauncherScope,
            override val watcher: ProfilingScopeLifecycleWatcher? = null,
            var profileableReference: WeakReference<Profileable?>
        ) : ApplicationScope(
            parent = parent,
            watcher = watcher,
            profileableReference = profileableReference
        ) {

            init {
                initialize()
            }

            override val name: String
                get() = "app-bg2fg-scope"
        }

        data class Background2Worker(
            override val parent: LauncherScope,
            override val watcher: ProfilingScopeLifecycleWatcher? = null,
            var profileableReference: WeakReference<Profileable?>
        ) : ApplicationScope(
            parent = parent,
            watcher = watcher,
            profileableReference = profileableReference
        ) {

            init {
                initialize()
            }

            override val name: String
                get() = "app-bg2worker-scope"
        }
    }

    data class JourneyScope(
        override val parent: ApplicationScope,
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Journey,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        init {
            initialize()
        }

        override val name: String
            get() = "journey-scope"
    }

    data class SessionScope(
        override val parent: JourneyScope,
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Session,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        init {
            initialize()
        }

        override val name: String
            get() = "session-scope"
    }

    data class UsageScope(
        override val parent: SessionScope,
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Usage,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        init {
            initialize()
        }

        override val name: String
            get() = "usage-session-scope"
    }

    sealed class HeroScope(
        override val parent: UsageScope,
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Hero,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        data class Page(
            override val parent: UsageScope,
            override val watcher: ProfilingScopeLifecycleWatcher? = null,
            var profileableReference: WeakReference<Profileable?>
        ) : HeroScope(
            parent = parent,
            watcher = watcher,
            profileableReference = profileableReference
        ) {

            lateinit var visiblePeriod: Interval

            init {
                initialize()
            }

            fun startVisible() {
                val now = UnixEpochInMillis(Millis(System.millis()))
                visiblePeriod = Interval(start = now, end = now)
            }

            fun stopVisible() {
                visiblePeriod.end = UnixEpochInMillis(Millis(System.millis()))
            }

            override val name: String
                get() = "hero-page-scope"
        }

        data class Worker(
            override val parent: UsageScope,
            override val watcher: ProfilingScopeLifecycleWatcher? = null,
            var profileableReference: WeakReference<Profileable?>
        ) : HeroScope(
            parent = parent,
            watcher = watcher,
            profileableReference = profileableReference
        ) {

            init {
                initialize()
            }

            override val name: String
                get() = "hero-worker-scope"
        }
    }

    data class ComponentScope(
        override val parent: HeroScope,
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Component,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        lateinit var visiblePeriod: Interval

        init {
            initialize()
        }

        fun startVisible() {
            val now = UnixEpochInMillis(Millis(System.millis()))
            visiblePeriod = Interval(start = now, end = now)
        }

        fun stopVisible() {
            visiblePeriod.end = UnixEpochInMillis(Millis(System.millis()))
        }

        override val name: String
            get() = "hero-component-scope"
    }

    data class ContentScope(
        override val parent: ComponentScope,
        override val watcher: ProfilingScopeLifecycleWatcher? = null,
        var profileableReference: WeakReference<Profileable?>
    ) : ProfilingScope(
        rank = Rank.Content,
        parent = parent,
        watcher = watcher,
        profileableReference = profileableReference
    ) {

        init {
            initialize()
        }

        override val name: String
            get() = "component-content-scope"
    }
}