package io.verse.profiling.android

import io.tagd.android.app.TagdApplication
import io.tagd.core.Dependencies
import io.tagd.core.Initializer
import io.tagd.core.dependencies
import io.tagd.langx.Context
import io.tagd.langx.time.UnixEpochInMillis
import io.verse.app.AppBundle
import io.verse.app.AppBundleLibraryInitializer
import io.verse.app.InstallIdentifierLoader
import io.verse.app.ThemeResolver
import java.lang.ref.WeakReference

class AppBundleInitializer(application: TagdApplication) : Initializer<AppBundle> {

    private var weakApplication: WeakReference<TagdApplication>? = WeakReference(application)

    private val app: TagdApplication?
        get() = weakApplication?.get()

    override fun new(dependencies: Dependencies): AppBundle {
        val context = app!!
        var appBundle: AppBundle? = null

        AppBundleLibraryInitializer(context).new(
            dependencies = dependencies(
                AppBundleLibraryInitializer.ARG_CONTEXT to context
            )
        ).also { library ->
            library.appBundle?.let {
                appBundle = it
            } ?: run {
                appBundle = newAppBundle(context).apply {
                    library.update(this)
                }
            }
        }

        return appBundle!!
    }

    private fun newAppBundle(context: Context): AppBundle {
        return AppBundle(
            versionName = BuildConfig.VERSION_NAME,
            currentVersionCode = BuildConfig.VERSION_CODE,
            previousVersionCode = BuildConfig.VERSION_CODE,
            flavour = BuildConfig.FLAVOR,
            flavorDimension = BuildConfig.FLAVOUR_DIMENSION,
            buildType = BuildConfig.BUILD_TYPE,
            namespace = context.packageName,
            profilable = canProfile(),
            appLocale = null,
            localityLocale = null,
            installTime = UnixEpochInMillis(),
            themeLabel = ThemeResolver().themeLabel(context),
            systemLocale = io.tagd.langx.Locale.default(),
            publishingIdentifier = BuildConfig.APPLICATION_ID,
            installIdentifier = InstallIdentifierLoader.newInstallIdentifier(context),
        )
    }

    private fun canProfile(): Boolean {
        return BuildConfig.DEBUG
    }

    override fun release() {
        weakApplication?.clear()
        weakApplication = null
    }

    companion object {

        fun new(app: TagdApplication): AppBundle {
            return AppBundleInitializer(app).new(dependencies())
        }
    }
}