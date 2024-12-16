package io.verse.profiling.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import io.verse.profiling.app.ProfileableActivity
import io.verse.profiling.core.ProfilingScope

class MainActivity : ProfileableActivity() {

    override val name: String
        get() = NAME

    private lateinit var layout: MainComposeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profiler.leave(METHOD_ON_CREATE) // method exit
    }

    override fun onCreateView(savedInstanceState: Bundle?) {
        val callee = profiler.visit(METHOD_ON_CREATE_VIEW) // method entry (optional profiling for protected methods)

        layout = MainComposeView(profiler.profilingScope as ProfilingScope.HeroScope, callee)
        renderWithPeriodicUpdates()

        profiler.d("onCreateView", "Sample logD message")
        profiler.escalate(Exception("Sample Anomaly exception"))

        profiler.leave(METHOD_ON_CREATE_VIEW) //method entry (optional profiling for protected methods)

        SomeArbitraryClassWithLogging().doSomeStuff()
        SomeArbitraryProfileable()
    }

    private fun renderWithPeriodicUpdates() {
        var delay = 0L
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(object : Runnable {
            override fun run() {
                delay = 20000L
                setContent {
                    layout.Render()
                }
                handler.postDelayed(this, delay)
            }
        }, delay)
    }

    override fun onStart() {
        profiler.visit(METHOD_ON_START)
        super.onStart()
        profiler.leave(METHOD_ON_START)
    }

    companion object {
        const val NAME = "main-page"
        const val METHOD_ON_CREATE = "onCreate"
        const val METHOD_ON_CREATE_VIEW = "onCreateView"
        const val METHOD_ON_START = "onStart"
    }
}