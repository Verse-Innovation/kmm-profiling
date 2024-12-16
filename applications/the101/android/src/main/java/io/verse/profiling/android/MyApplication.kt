package io.verse.profiling.android

import io.tagd.android.app.TagdApplication
import io.tagd.arch.control.ApplicationInjector
import io.tagd.arch.control.IApplication

class MyApplication : io.verse.profiling.app.ProfileableApplication() {

    override val name: String
        get() = NAME

    override fun application(): IApplication {
        return this
    }

    override fun newInjector(): ApplicationInjector<out TagdApplication> {
        return MyInjector(this)
    }

    companion object {
        const val NAME = "my-profileable-app"
    }
}