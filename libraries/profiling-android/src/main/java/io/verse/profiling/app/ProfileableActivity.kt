package io.verse.profiling.app

import android.os.Bundle
import io.tagd.android.app.AppCompatActivity
import io.tagd.arch.control.IApplication
import io.verse.profiling.adapter.ProfileableSubject
import io.verse.profiling.adapter.ProfilingDelegate
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.core.newPageScope

abstract class ProfileableActivity : AppCompatActivity(), ProfileableSubject {

    protected lateinit var profiler: ProfilingDelegate<ProfileableActivity>

    override lateinit var profilingScope: ProfilingScope

    override fun application(): IApplication {
        return application as IApplication
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //create view
        //profiler.signal("creating view", METHOD_ON_CREATE) // a key step - setting content, signal it
        //onCreateView(savedInstanceState) //logic
        //profiler.mark("view created", METHOD_ON_CREATE) // a key step - setting content, signal it
        profiler.leave(METHOD_ON_CREATE)
    }

    override fun onPreCreate(savedInstanceState: Bundle?) {
        super.onPreCreate(savedInstanceState)

        //create profiler
        onCreateProfiler()
        profiler.visit(METHOD_ON_CREATE) // method entry
    }

    override fun interceptOnCreate(savedInstanceState: Bundle?) {
        super.interceptOnCreate(savedInstanceState)

        profiler.onInitialize(METHOD_ON_CREATE) // life cycle event
        profiler.mark("profiler created", METHOD_ON_CREATE) // checkpoint
    }

    protected open fun onCreateProfiler() {
        profilingScope = newPageScope(this)
        profiler = ProfilingDelegate(
            subject = this,
            name = name,
            profilingScope = profilingScope
        )
    }

    override fun onStart() {
        profiler.visit(METHOD_ON_START)
        super.onStart()
        profiler.onReady(METHOD_ON_START)
        profiler.leave(METHOD_ON_START)
    }

    override fun onAwaiting() {
        profiler.visit(METHOD_ON_AWAITING)
        super.onAwaiting()
        profiler.onAwaiting(METHOD_ON_AWAITING)
        profiler.leave(METHOD_ON_AWAITING)
    }

    override fun onReady() {
        profiler.visit(METHOD_ON_READY)
        super.onReady()
        profiler.onReady(METHOD_ON_READY)
        profiler.leave(METHOD_ON_READY)
    }

    override fun onResume() {
        profiler.visit(METHOD_ON_RESUME)
        super.onResume()
        profiler.onProcessing(METHOD_ON_RESUME)
        profiler.leave(METHOD_ON_RESUME)
    }

    override fun onPause() {
        profiler.visit(METHOD_ON_PAUSE)
        profiler.onReport(METHOD_ON_PAUSE)
        super.onPause()
        profiler.leave(METHOD_ON_PAUSE)
    }

    override fun onStop() {
        profiler.visit(METHOD_ON_STOP)
        profiler.onInterrupt(METHOD_ON_STOP)
        super.onStop()
        profiler.leave(METHOD_ON_STOP)
    }

    override fun onDestroy() {
        profiler.visit(METHOD_ON_DESTROY)
        profiler.onRelease(METHOD_ON_DESTROY)
        profilingScope.leave()
        super.onDestroy()
        profiler.leave(METHOD_ON_DESTROY)
    }

    companion object {
        const val METHOD_ON_CREATE = "onCreate"
        const val METHOD_ON_START = "onStart"
        const val METHOD_ON_RESUME = "onResume"
        const val METHOD_ON_AWAITING = "onAwaiting"
        const val METHOD_ON_READY = "onReady"
        const val METHOD_ON_PAUSE = "onPause"
        const val METHOD_ON_STOP = "onStop"
        const val METHOD_ON_DESTROY = "onDestroy"
    }
}