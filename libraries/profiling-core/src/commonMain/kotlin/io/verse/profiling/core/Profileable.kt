package io.verse.profiling.core

import io.tagd.arch.control.IApplication
import io.tagd.core.Nameable
import io.tagd.langx.ref.WeakReference

interface Profileable : Nameable, Scopable {

    fun application(): IApplication
}

fun Profileable?.weak(): WeakReference<Profileable?> {
    return WeakReference(this)
}