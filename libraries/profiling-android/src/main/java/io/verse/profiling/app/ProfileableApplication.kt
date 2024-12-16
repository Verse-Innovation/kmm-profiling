package io.verse.profiling.app

import io.tagd.android.app.TagdApplication
import io.tagd.androidx.app.processName
import io.tagd.arch.access.library
import io.tagd.arch.control.ApplicationController
import io.verse.profiling.adapter.IProfileableApplication
import io.verse.profiling.adapter.ProfileableApplicationController
import io.verse.profiling.adapter.ProfilingDelegate
import io.verse.profiling.core.Profileable
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.core.SandBoxing
import io.verse.profiling.core.newForkedProcessScope
import io.verse.profiling.core.newMainProcessScope

abstract class ProfileableApplication : TagdApplication(), IProfileableApplication {

    override val profilingScope: ProfilingScope
        get() = processScope

    protected lateinit var profiler: ProfilingDelegate<ProfileableApplication>

    lateinit var processScope: ProfilingScope.ProcessScope
        private set

    override fun onCreate() {
        super.onCreate()
        profiler.leave("onCreate")
    }

    override fun setupSelf() {
        super.setupSelf()
        setupSystemScope()
        setupProcessScope()
        setupProfiler()
    }

    private fun setupSystemScope() {
        library<SandBoxing>()?.newSystemScope(this)
    }

    private fun setupProcessScope() {
        val processName = processName()
        processScope = if (processName?.contains(":") == true) {
            newForkedProcessScope(this)
        } else {
            newMainProcessScope(this)
        }
    }

    protected open fun setupProfiler() {
        profiler = ProfilingDelegate(
            subject = this,
            name = name,
            profilingScope = profilingScope,
        )

        profiler.visit("onCreate")
        profiler.onInitialize("onCreate")
    }

    override fun onCreateController(): ApplicationController<*> {
        return ProfileableApplicationController(this)
    }

    override fun onLaunch() {
        profiler.visit("onLaunch")
        profiler.onReport("onLaunch")
        super.onLaunch()

        setupLauncherScope()
        setupApplicationScope()
        setupJourneyScope()

        profiler.leave("onLaunch")
    }

    private fun setupLauncherScope() {
        library<SandBoxing>()?.newLauncherScope(
            processScope = processScope,
            triggerType = launcher.toTriggerType(),
            profileable = this
        )
    }

    private fun setupApplicationScope() {
        if (processScope is ProfilingScope.ProcessScope.Main) {
            library<SandBoxing>()?.newApplicationScope(
                launcher.profileable(this) as Profileable
            )
        }
    }

    private fun setupJourneyScope() {
        if (processScope is ProfilingScope.ProcessScope.Main) {
            library<SandBoxing>()?.newJourneyScope()
        }
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
        super.onReady()
        profiler.leave("onReady")
    }

    override fun onForeground() {
        profiler.visit("onForeground")
        profiler.onProcessing("onForeground")
        super.onForeground()
        profiler.leave("onForeground")
    }

    override fun onBackground() {
        profiler.visit("onBackground")
        profiler.onInterrupt("onBackground")
        super.onBackground()
        profiler.leave("onBackground")
    }

    override fun onExit() {
        profiler.visit("onExit")
        profiler.onRelease("onExit")
        super.onExit()
        profiler.leave("onExit")
    }
}