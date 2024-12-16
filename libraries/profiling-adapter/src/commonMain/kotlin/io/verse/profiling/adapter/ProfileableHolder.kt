package io.verse.profiling.adapter

import io.tagd.arch.control.IApplication
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.core.ProfilingScopeFactory
import io.verse.profiling.tracer.Branch
import kotlin.reflect.KClass

class ProfileableHolder<P : ProfilingScope>(
    override val name: String,
    scopeType: KClass<P>,
    triggerType: ProfilingScope.LauncherScope.TriggerType? = null,
    parentScope: ProfilingScope? = null,
    private val callee: Branch? = null
) : ProfileableSubject {

    override val profilingScope: P =
        ProfilingScopeFactory.newScope(scopeType, this, triggerType, parentScope)

    var profiler: ProfilingDelegate<ProfileableHolder<P>>? = null
        private set

    override fun application(): IApplication {
        return io.tagd.arch.control.application()!!
    }

    init {
        profiler = newProfiler()
    }

    private fun newProfiler(): ProfilingDelegate<ProfileableHolder<P>> {
        return ProfilingDelegate(
            subject = this,
            name = name,
            profilingScope = profilingScope,
            callee = callee
        )
    }
}

/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  ProfilingDelegate HOFs  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

internal typealias SystemProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.SystemScope>>

internal typealias MainProcessProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.ProcessScope.Main>>

internal typealias ForkedProcessProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.ProcessScope.Forked>>

internal typealias LauncherProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.LauncherScope>>

internal typealias ApplicationBg2FgProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.ApplicationScope.Background2Foreground>>

internal typealias ApplicationBg2WkProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.ApplicationScope.Background2Worker>>

internal typealias JourneyProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.JourneyScope>>

internal typealias SessionProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.SessionScope>>

internal typealias UsageSessionProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.UsageScope>>

typealias PageProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.HeroScope.Page>>

typealias WorkerProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.HeroScope.Worker>>

typealias ComponentProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.ComponentScope>>

typealias ContentProfilingDelegate =
        ProfilingDelegate<ProfileableHolder<ProfilingScope.ContentScope>>

internal fun newSystemProfileDelegate(name: String): SystemProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.SystemScope::class).profiler!!
}

internal fun newMainProcessProfileDelegate(name: String): MainProcessProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.ProcessScope.Main::class).profiler!!
}

internal fun newForkedProcessProfileDelegate(name: String): ForkedProcessProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.ProcessScope.Forked::class).profiler!!
}

internal fun newLaunchProfileDelegate(
    name: String,
    triggerType: ProfilingScope.LauncherScope.TriggerType,
    processScope: ProfilingScope.ProcessScope,
): LauncherProfilingDelegate {

    return ProfileableHolder(
        name,
        ProfilingScope.LauncherScope::class,
        triggerType,
        processScope
    ).profiler!!
}

internal fun newApplicationBg2FgProfileDelegate(name: String): ApplicationBg2FgProfilingDelegate {
    return ProfileableHolder(
        name,
        ProfilingScope.ApplicationScope.Background2Foreground::class
    ).profiler!!
}

internal fun newApplicationBg2WkProfileDelegate(name: String): ApplicationBg2WkProfilingDelegate {
    return ProfileableHolder(
        name,
        ProfilingScope.ApplicationScope.Background2Worker::class
    ).profiler!!
}

internal fun newJourneyProfileDelegate(name: String): JourneyProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.JourneyScope::class).profiler!!
}

internal fun newSessionProfileDelegate(name: String): SessionProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.SessionScope::class).profiler!!
}

internal fun newUsageSessionProfileDelegate(name: String): UsageSessionProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.UsageScope::class).profiler!!
}

fun newPageProfileDelegate(name: String): PageProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.HeroScope.Page::class).profiler!!
}

fun newWorkerProfileDelegate(name: String): WorkerProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.HeroScope.Worker::class).profiler!!
}

fun newComponentProfileDelegate(name: String, callee: Branch?): ComponentProfilingDelegate {
    return ProfileableHolder(name, ProfilingScope.ComponentScope::class, callee = callee).profiler!!
}

fun newContentProfileDelegate(
    name: String,
    parentScope: ProfilingScope.ComponentScope
): ContentProfilingDelegate {

    return ProfileableHolder(
        name,
        ProfilingScope.ContentScope::class,
        parentScope = parentScope
    ).profiler!!
}


/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  ProfilingDelegateFactory  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

object ProfilingDelegateFactory {

    internal fun forSystem(name: String): SystemProfilingDelegate {
        return newSystemProfileDelegate(name)
    }

    internal fun forMainProcess(name: String): MainProcessProfilingDelegate {
        return newMainProcessProfileDelegate(name)
    }

    internal fun forForkedProcess(name: String): ForkedProcessProfilingDelegate {
        return newForkedProcessProfileDelegate(name)
    }

    internal fun forLaunch(
        name: String,
        triggerType: ProfilingScope.LauncherScope.TriggerType,
        processScope: ProfilingScope.ProcessScope
    ): LauncherProfilingDelegate {

        return newLaunchProfileDelegate(name, triggerType, processScope)
    }

    internal fun forJourney(name: String): JourneyProfilingDelegate {
        return newJourneyProfileDelegate(name)
    }

    internal fun forSession(name: String): SessionProfilingDelegate {
        return newSessionProfileDelegate(name)
    }

    internal fun forUsageSession(name: String): UsageSessionProfilingDelegate {
        return newUsageSessionProfileDelegate(name)
    }

    fun forPage(name: String): PageProfilingDelegate {
        return newPageProfileDelegate(name)
    }

    fun forWork(name: String): WorkerProfilingDelegate {
        return newWorkerProfileDelegate(name)
    }

    fun forComponent(name: String, callee: Branch?): ComponentProfilingDelegate {
        return newComponentProfileDelegate(name, callee)
    }

    fun forContent(
        name: String,
        parentScope: ProfilingScope.ComponentScope
    ): ContentProfilingDelegate {

        return newContentProfileDelegate(name, parentScope)
    }
}


/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~  ProfilingDelegate Convenience HOFs  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/
/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~*/

internal fun systemProfiler(name: String) : SystemProfilingDelegate {
    return newSystemProfileDelegate(name)
}

internal fun mainProcessProfiler(name: String) : MainProcessProfilingDelegate {
    return newMainProcessProfileDelegate(name)
}

internal fun forkedProcessProfiler(name: String) : ForkedProcessProfilingDelegate {
    return newForkedProcessProfileDelegate(name)
}

internal fun applicationBg2FgProfiler(name: String) : ApplicationBg2FgProfilingDelegate {
    return newApplicationBg2FgProfileDelegate(name)
}

internal fun applicationBg2WkProfiler(name: String) : ApplicationBg2WkProfilingDelegate {
    return newApplicationBg2WkProfileDelegate(name)
}

internal fun launchProfiler(
    name: String,
    trigger: ProfilingScope.LauncherScope.TriggerType,
    parent: ProfilingScope.ProcessScope
): LauncherProfilingDelegate {

    return newLaunchProfileDelegate(name, trigger, parent)
}

internal fun journeyProfiler(name: String) : JourneyProfilingDelegate {
    return newJourneyProfileDelegate(name)
}

internal fun sessionProfiler(name: String) : SessionProfilingDelegate {
    return newSessionProfileDelegate(name)
}

internal fun sessionUsageProfiler(name: String) : UsageSessionProfilingDelegate {
    return newUsageSessionProfileDelegate(name)
}

fun pageProfiler(name: String) : PageProfilingDelegate {
    return newPageProfileDelegate(name)
}

fun workProfiler(name: String) : WorkerProfilingDelegate {
    return newWorkerProfileDelegate(name)
}

fun componentProfiler(name: String, callee: Branch?) : ComponentProfilingDelegate {
    return newComponentProfileDelegate(name, callee)
}

fun contentProfiler(name: String, parent: ProfilingScope.ComponentScope): ContentProfilingDelegate {
    return newContentProfileDelegate(name, parent)
}