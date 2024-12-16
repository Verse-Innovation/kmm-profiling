package io.verse.profiling.app

import io.tagd.android.app.DeepLinkLauncher
import io.tagd.android.app.EventLauncher
import io.tagd.android.app.JobLauncher
import io.tagd.android.app.Launcher
import io.tagd.android.app.NotificationLauncher
import io.tagd.android.app.PageLauncher
import io.tagd.android.app.SystemLauncher
import io.verse.profiling.core.Profileable
import io.verse.profiling.core.ProfilingScope

fun Launcher<*>.toTriggerType(): ProfilingScope.LauncherScope.TriggerType {
    return when (this) {
        is PageLauncher -> {
            ProfilingScope.LauncherScope.TriggerType.AppIcon
        }

        is DeepLinkLauncher -> {
            ProfilingScope.LauncherScope.TriggerType.DeepLink(cause.toString())
        }

        is NotificationLauncher -> {
            ProfilingScope.LauncherScope.TriggerType.Notification(cause)
        }

        is JobLauncher -> {
            ProfilingScope.LauncherScope.TriggerType.Job(cause)
        }

        is EventLauncher -> {
            ProfilingScope.LauncherScope.TriggerType.Event(cause)
        }

        is SystemLauncher -> {
            ProfilingScope.LauncherScope.TriggerType.System
        }

        else -> {
            ProfilingScope.LauncherScope.TriggerType.AppIcon
        }
    }
}

fun Launcher<*>.profileable(application: ProfileableApplication): Profileable? {
    var profileable: Profileable? = null
    if (this is PageLauncher) {
        if (this.activity.get() is Profileable) {
            profileable = this.activity.get() as Profileable
        }
    } else if (this is EventLauncher) {
        if (this.receiver.get() is Profileable) {
            profileable = this.receiver.get() as Profileable
        }
    } else if (this is JobLauncher) {
        if (this.service.get() is Profileable) {
            profileable = this.service.get() as Profileable
        }
    } else if (this is DeepLinkLauncher) {
        profileable = application
    }
    return profileable
}