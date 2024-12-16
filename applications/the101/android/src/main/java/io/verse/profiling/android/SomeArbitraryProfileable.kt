package io.verse.profiling.android

import io.tagd.core.Releasable
import io.verse.profiling.adapter.ProfileableHolder
import io.verse.profiling.adapter.ProfilingDelegateFactory
import io.verse.profiling.adapter.WorkerProfilingDelegate
import io.verse.profiling.adapter.newComponentProfileDelegate
import io.verse.profiling.adapter.newContentProfileDelegate
import io.verse.profiling.adapter.newPageProfileDelegate
import io.verse.profiling.adapter.newWorkerProfileDelegate
import io.verse.profiling.adapter.workProfiler
import io.verse.profiling.core.ProfilingScope

class SomeArbitraryProfileable {

    init {
        variousProfileableViewHolders()
        variousProfileableDelegates()
        ProfileableWorkerWithDelegationThroughNewFunction().someWork()
        ProfileableWorkerWithDelegationThroughFactory().someWork()
        ProfileableWorkerWithDelegationThroughConciseHOF().someWork()
    }

    fun variousProfileableViewHolders() {
/*        val systemProfileableHolder = ProfileableHolder(
            name = "SomeArbitraryProfileableSystem",
            scopeType = ProfilingScope.SystemScope::class,
        )
        val processProfileableHolder = ProfileableHolder(
            name = "SomeArbitraryProfileableProcess",
            scopeType = ProfilingScope.ProcessScope.Main::class,
        )
        val launchProfileableHolder = ProfileableHolder(
            name = "SomeArbitraryProfileableProcess",
            scopeType = ProfilingScope.ProcessScope.Main::class,
            triggerType = ProfilingScope.LauncherScope.TriggerType.System,
            parentScope = processProfileableHolder.profilingScope
        )*/
        val pageProfileableHolder = ProfileableHolder(
            name = "SomeArbitraryProfileablePage",
            scopeType = ProfilingScope.HeroScope.Page::class,
        )
        val workProfileableHolder = ProfileableHolder(
            name = "SomeArbitraryProfileableWork",
            scopeType = ProfilingScope.HeroScope.Worker::class,
        )
        val componentProfileableHolder = ProfileableHolder(
            name = "SomeArbitraryProfileableComponent",
            scopeType = ProfilingScope.ComponentScope::class,
        )
        val contentProfileableHolder = ProfileableHolder(
            name = "SomeArbitraryProfileableComponent",
            scopeType = ProfilingScope.ContentScope::class,
            parentScope = componentProfileableHolder.profilingScope
        )
    }

    fun variousProfileableDelegates() {
/*        val systemProfileDelegate = newSystemProfileDelegate(
            "SomeArbitraryProfileableSystem"
        )
        val processProfileDelegate = newMainProcessProfileDelegate(
            "SomeArbitraryProfileableProcess"
        )
        val launchProfileDelegate = newLaunchProfileDelegate(
            name = "SomeArbitraryProfileableProcess",
            triggerType = ProfilingScope.LauncherScope.TriggerType.System,
            processScope = processProfileDelegate.profilingScope as ProfilingScope.ProcessScope
        )*/
        val pageProfileDelegate = newPageProfileDelegate(
            name = "SomeArbitraryProfileablePage"
        )
        val workProfileableHolder = newWorkerProfileDelegate(
            name = "SomeArbitraryProfileableWork"
        )
        val componentProfileableHolder = newComponentProfileDelegate(
            name = "SomeArbitraryProfileableComponent",
            null
        )
        val contentProfileableHolder = newContentProfileDelegate(
            name = "SomeArbitraryProfileableComponent",
            parentScope = componentProfileableHolder.profilingScope as ProfilingScope.ComponentScope
        )
    }

    class ProfileableWorkerWithDelegationThroughNewFunction() : Releasable {

        private var profiler: WorkerProfilingDelegate? = null

        init {
            profiler = newWorkerProfileDelegate(
                "ProfileableWorkerWithDelegationThroughNewFunction"
            )
        }

        fun someWork() {
            profiler?.signal("hey I'm using the simplified profiling :) at" +
                    " ProfileableWorkerWithDelegationThroughNewFunction")
        }

        override fun release() {
            profiler?.release()
            profiler = null
        }
    }

    class ProfileableWorkerWithDelegationThroughFactory() : Releasable {

        private var profiler: WorkerProfilingDelegate? = null

        init {
            profiler = ProfilingDelegateFactory.forWork(
                "ProfileableWorkerWithDelegationThroughFactory"
            )
        }

        fun someWork() {
            profiler?.signal("hey I'm using the simplified profiling :) at " +
                    "ProfileableWorkerWithDelegationThroughFactory"
            )
        }

        override fun release() {
            profiler?.release()
            profiler = null
        }
    }

    class ProfileableWorkerWithDelegationThroughConciseHOF() : Releasable {

        private var profiler: WorkerProfilingDelegate? = null

        init {
            profiler = workProfiler(
                "ProfileableWorkerWithDelegationThroughConciseHOF"
            )
        }

        fun someWork() {
            profiler?.signal("hey I'm using the simplified profiling :) at " +
                    "ProfileableWorkerWithDelegationThroughConciseHOF"
            )
        }

        override fun release() {
            profiler?.release()
            profiler = null
        }
    }
}