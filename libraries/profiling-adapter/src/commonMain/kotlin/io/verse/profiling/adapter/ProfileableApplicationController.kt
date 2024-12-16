package io.verse.profiling.adapter

import io.tagd.arch.access.library
import io.tagd.arch.control.LifeCycleAwareApplicationController
import io.verse.profiling.core.Journey
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.core.SandBoxing

open class ProfileableApplicationController<A : IProfileableApplication>(application: A) :
    LifeCycleAwareApplicationController<A>(application), ProfileableSubject {

    override val name: String
        get() = app!!.name + "-controller"

    override val profilingScope: ProfilingScope
        get() = app!!.profilingScope

    protected lateinit var profiler: ProfilingDelegate<ProfileableApplicationController<A>>

    protected lateinit var journey: Journey
        private set

    override fun application(): IProfileableApplication {
        return app as IProfileableApplication
    }

    override fun onCreate() {
        onCreateProfiler()
        super.onCreate()
        profiler.leave("onCreate")
    }

    protected open fun onCreateProfiler() {
        profiler = ProfilingDelegate(
            subject = this,
            name = name,
            profilingScope = profilingScope
        )

        profiler.visit("onCreate")
        profiler.onInitialize("onCreate")
    }

    override fun onLaunch() {
        profiler.visit("onLaunch")
        profiler.onReport("onLaunch")
        setupJourney()
        super.onLaunch()
        profiler.leave("onLaunch")
    }

    override fun onLoading() {
        profiler.visit("onLoading")
        profiler.onAwaiting("onLoading")
        super.onLoading()
        profiler.leave("onLoading")
    }

    override fun onReady() {
        profiler.visit("onReady")
        profiler.onReady("onReady")
        profiler.mark("application is ready for business processing")
        super.onReady()
        profiler.leave("onReady")
    }

    private fun setupJourney() {
        val sandBoxing = application().thisScope.library<SandBoxing>()!!
        journey = sandBoxing.newJourney()
        journey.startSession()
    }

    override fun onForeground() {
        profiler.visit("onForeground")
        profiler.onProcessing("onForeground")
        super.onForeground()
        journey.active()
        profiler.leave("onForeground")
    }

    override fun onBackground() {
        profiler.visit("onBackground")
        profiler.onInterrupt("onBackground")
        super.onBackground()
        journey.inactive()
        profiler.leave("onBackground")
    }

    override fun onDestroy() {
        profiler.visit("onDestroy")
        profiler.onRelease("onDestroy")
        super.onDestroy()
        profiler.leave("onDestroy")
    }
}