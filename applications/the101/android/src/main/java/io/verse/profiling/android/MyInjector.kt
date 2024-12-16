package io.verse.profiling.android

import io.tagd.android.app.TagdApplicationInjector
import io.tagd.arch.scopable.AbstractWithinScopableInitializer
import io.tagd.arch.scopable.WithinScopableInitializer
import io.tagd.core.dependencies
import io.verse.profiling.adapter.Profiling
import io.verse.profiling.android.access.MyLatchInitializer
import io.verse.profiling.android.access.MyProfilingInitializer
import io.verse.storage.core.StorageInitializer

class MyInjector(application: MyApplication) : TagdApplicationInjector<MyApplication>(application) {

    override fun setup() {
        super.setup()

        // todo if any dependent lib is loaded before time, then it is never in ready state
        // todo guide developer that profiling needs to be in setup and not in inject,
        //  otherwise it wont work properly
        setupAppBundle()
        setupProfiling()
    }

    private fun setupAppBundle() {
        within.let {
            val appBundle = AppBundleInitializer.new(it)
        }
    }

    private fun setupProfiling(): Profiling {
        return MyProfilingInitializer(within).new(dependencies(
            AbstractWithinScopableInitializer.ARG_OUTER_SCOPE to within.thisScope
        ))
    }

    override fun load(initializers: ArrayList<WithinScopableInitializer<MyApplication, *>>) {
        super.load(initializers)
        initializers.add(MyLatchInitializer(within))
        initializers.add(StorageInitializer(within))
    }
}
