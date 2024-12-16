package io.verse.profiling.app

import androidx.compose.runtime.Composable
import io.tagd.arch.access.library
import io.tagd.arch.control.IApplication
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.adapter.ProfilingDelegate
import io.tagd.core.Releasable
import io.verse.profiling.adapter.ProfileableSubject
import io.verse.profiling.core.SandBoxing
import io.verse.profiling.tracer.Branch
import java.lang.ref.WeakReference

abstract class ProfileableComposable(
    private val parentScope: ProfilingScope.HeroScope,
    private val callee: Branch?
) : ProfileableSubject, Releasable {

    final override val profilingScope: ProfilingScope = ProfilingScope.ComponentScope(
            parentScope,
            library<SandBoxing>()?.watcher,
            WeakReference(this)
        )

    protected lateinit var profiler: ProfilingDelegate<ProfileableComposable>
        private set

    override fun application(): IApplication {
        return parentScope.weakProfilable?.get()?.application()!!
    }

    //todo handle content scopes

    init {
        onCreate()
    }

    protected open fun onCreate() {
        profiler = onCreateProfiler()
        profiler.visit("onCreate")
        profiler.onInitialize("onViewCreate")
        profiler.leave("onCreate")
    }

    protected open fun onCreateProfiler(): ProfilingDelegate<ProfileableComposable> {
        return ProfilingDelegate(
            subject = this,
            name = name,
            profilingScope = profilingScope,
            callee = callee
        )
    }

    @Composable
    fun Render() {
        profiler.visit("Render") // method entry
        profiler.onProcessing(alias = "onViewRender") // life cycle event
        profiler.i("Render", "setContent starts") //assorted logs
        CreateAndBind()
        profiler.i("Render", "setContent finish") //assorted logs
        profiler.leave("Render") // method exit
    }

    @Composable
    abstract fun CreateAndBind()

    override fun release() {
        profiler.visit("release") // method entry
        profiler.onRelease(alias = "onViewRelease")
        profiler.profilingScope.leave()
        profiler.leave("release") // method exit
    }
}

