package io.verse.profiling.android

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.tagd.core.MappableReference
import io.verse.profiling.adapter.ProfilingDelegate
import io.verse.profiling.android.MainComposeView.Companion.METHOD_ON_VIEW_BINDING
import io.verse.profiling.android.MainComposeView.Companion.METHOD_ON_VIEW_BINDING_FINISH
import io.verse.profiling.app.ProfileableComposable
import io.verse.profiling.core.ProfilingScope
import io.verse.profiling.tracer.Branch

class MainComposeView(
    parentScope: ProfilingScope.HeroScope,
    callee: Branch?,
) : ProfileableComposable(parentScope, callee) {

    override val name: String
        get() = NAME

    private var bindCounter: Int = 0

    @Composable
    override fun CreateAndBind() {
        MyApplicationTheme {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colors.background
            ) {
                GreetingView("bounded content iteration ${++bindCounter}")
            }
        }
    }

    companion object {
        const val NAME = "main-page-view"
        const val METHOD_ON_VIEW_BINDING = "onViewBinding"
        const val METHOD_ON_VIEW_BINDING_FINISH = "onViewBindingFinish"
    }
}

@Preview
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        GreetingView("Hello, Android!")
    }
}

@Composable
fun GreetingView(text: String, delegate: ProfilingDelegate<*>? = null) {
    val element = MappableReference(text)
    delegate?.onBinding(alias = METHOD_ON_VIEW_BINDING, element = element) // binding starts

    Text(text = text)

    delegate?.onBindFinish(alias = METHOD_ON_VIEW_BINDING_FINISH, element = element) // binding starts
}

