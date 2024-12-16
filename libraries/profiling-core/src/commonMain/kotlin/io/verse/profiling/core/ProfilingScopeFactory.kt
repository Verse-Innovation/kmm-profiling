package io.verse.profiling.core

import io.tagd.arch.access.library
import kotlin.reflect.KClass

object ProfilingScopeFactory {

    @Suppress("UNCHECKED_CAST")
    fun <P : ProfilingScope> newScope(
        kClass: KClass<P>,
        profileable: Profileable,
        triggerType: ProfilingScope.LauncherScope.TriggerType? = null,
        parentScope: ProfilingScope? = null
    ): P {

        val scope = when(kClass) {
            ProfilingScope.SystemScope::class -> newSystemScope(profileable)
            ProfilingScope.ProcessScope.Main::class -> newMainProcessScope(profileable)
            ProfilingScope.ProcessScope.Forked::class -> newForkedProcessScope(
                profileable
            )
            ProfilingScope.LauncherScope::class ->
                newLauncherScope(
                    parentScope as ProfilingScope.ProcessScope,
                    triggerType!!,
                    profileable
                )
            
            ProfilingScope.ApplicationScope.Background2Foreground::class ->
                newApplicationScope(profileable)
            
            ProfilingScope.ApplicationScope.Background2Worker::class ->
                newApplicationScope(profileable)
            
            ProfilingScope.JourneyScope::class -> newJourneyScope(profileable)
            ProfilingScope.SessionScope::class -> newSessionScope(profileable)
            ProfilingScope.UsageScope::class -> newUsageScope(profileable)
            ProfilingScope.HeroScope.Page::class -> newPageScope(profileable)
            ProfilingScope.HeroScope.Worker::class -> newWorkerScope(profileable)
            ProfilingScope.ComponentScope::class -> newComponentScope(profileable)
            ProfilingScope.ContentScope::class ->
                newContentScope(parentScope as ProfilingScope.ComponentScope, profileable)
            else -> throw UnsupportedOperationException()
        }
        return scope as P
    }
}

fun newSystemScope(profileable: Profileable): ProfilingScope.SystemScope {
    return library<SandBoxing>()!!.newSystemScope(profileable)
}

fun newMainProcessScope(profileable: Profileable): ProfilingScope.ProcessScope.Main {
    return library<SandBoxing>()!!.newMainProcessScope(profileable)
}

fun newForkedProcessScope(profileable: Profileable): ProfilingScope.ProcessScope.Forked {
    return library<SandBoxing>()!!.newForkedProcessScope(profileable)
}

fun newLauncherScope(
    parentScope: ProfilingScope.ProcessScope,
    triggerType: ProfilingScope.LauncherScope.TriggerType,
    profileable: Profileable
): ProfilingScope.LauncherScope {

    return library<SandBoxing>()!!.newLauncherScope(parentScope, triggerType, profileable)
}

fun newApplicationScope(profileable: Profileable): ProfilingScope.ApplicationScope? {
    return library<SandBoxing>()!!.newApplicationScope(profileable)
}

fun newJourneyScope(profileable: Profileable): ProfilingScope.JourneyScope {
    return library<SandBoxing>()!!.newJourneyScope(profileable)
}

fun newSessionScope(profileable: Profileable): ProfilingScope.SessionScope {
    return library<SandBoxing>()!!.newSessionScope(profileable)
}

fun newUsageScope(profileable: Profileable): ProfilingScope.UsageScope {
    return library<SandBoxing>()!!.newUsageScope(profileable)
}

fun newPageScope(profileable: Profileable): ProfilingScope.HeroScope.Page {
    return library<SandBoxing>()!!.newPageScope(profileable)
}

fun newWorkerScope(profileable: Profileable): ProfilingScope.HeroScope.Worker {
    return library<SandBoxing>()!!.newWorkerScope(profileable)
}

fun newComponentScope(profileable: Profileable): ProfilingScope.ComponentScope {
    return library<SandBoxing>()!!.newComponentScope(profileable)
}

fun newContentScope(
    parentScope: ProfilingScope.ComponentScope,
    profileable: Profileable
): ProfilingScope.ContentScope {
    
    return library<SandBoxing>()!!.newContentScope(parentScope, profileable)
}