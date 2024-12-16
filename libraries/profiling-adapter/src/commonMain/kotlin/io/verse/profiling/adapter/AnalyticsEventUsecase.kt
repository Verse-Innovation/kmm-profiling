package io.verse.profiling.adapter

import io.tagd.arch.access.library
import io.tagd.arch.access.module
import io.tagd.arch.domain.crosscutting.async.AsyncContext
import io.tagd.arch.domain.crosscutting.async.compute
import io.tagd.arch.domain.usecase.Args
import io.tagd.arch.domain.usecase.CallableUseCase
import io.tagd.arch.domain.usecase.argsOf
import io.tagd.arch.scopable.library.Library
import io.tagd.arch.scopable.library.usecase
import io.tagd.arch.scopable.module.Module
import io.tagd.arch.scopable.module.usecase
import io.tagd.langx.IllegalValueException

abstract class AnalyticsEventUsecase(
    protected open val eventName: String,
) : CallableUseCase<Unit>(), AsyncContext {

    override fun trigger(args: Args) {
        compute {
            trackWith(args)
        }
    }

    private fun trackWith(args: Args) {
        val delegate = args.get<ProfilingDelegate<*>>(ARG_PROFILING_DELEGATE)
        delegate?.let {
            trackWith(delegate, args)
        } ?: kotlin.run {
            setError(
                args = args,
                throwable = newPopulateEventException(
                    "$ARG_PROFILING_DELEGATE must be ${ProfilingDelegate::class.simpleName}"
                )
            )
        }
    }

    protected open fun trackWith(delegate: ProfilingDelegate<*>, args: Args) {
        try {
            val event = newEventWithContext(args)
            populate(event = event, args = args)

            val eventExtras = args.get<Map<String, Any>>(ARG_EVENT_EXTRAS) ?: emptyMap()
            event.putAll(eventExtras)

            delegate.track(eventName, event)
            setValue(args, Unit)
        } catch (e: PopulateEventException) {
            setError(args, e)
        }
    }

    abstract fun newEventWithContext(args: Args): HashMap<String, Any>

    @Throws(PopulateEventException::class)
    protected abstract fun populate(event: HashMap<String, Any>, args: Args)

    protected open fun newPopulateEventException(message: String?): PopulateEventException {
        val cause = IllegalValueException(message)
        return PopulateEventException(message, cause)
    }

    companion object {
        const val ARG_PROFILING_DELEGATE = "profiling_delegate"
        const val ARG_EVENT_EXTRAS = "event_extras"
    }
}

open class AnalyticsException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

open class PopulateEventException(
    message: String? = null,
    cause: Throwable? = null,
) : AnalyticsException(message, cause)


inline fun <reified M : Module, reified T : AnalyticsEventUsecase> trackModuleEvent(
    profiler: ProfilingDelegate<*>,
    args: Args = argsOf(),
    extras: Map<String, Any> = emptyMap()
) {

    module<M>()
        ?.usecase<T>()
        ?.execute(
            args = argsOf(args).apply {
                put(AnalyticsEventUsecase.ARG_PROFILING_DELEGATE, profiler)
                put(AnalyticsEventUsecase.ARG_EVENT_EXTRAS, extras)
            },
            success = {
                //no-op
            },
            failure = {
                it.printStackTrace()
            }
        )
}

inline fun <reified L : Library, reified T : AnalyticsEventUsecase> trackLibraryEvent(
    profiler: ProfilingDelegate<*>,
    args: Args = argsOf(),
    extras: Map<String, Any> = emptyMap()
) {

    library<L>()
        ?.usecase<T>()
        ?.execute(
            args = argsOf(args).apply {
                put(AnalyticsEventUsecase.ARG_PROFILING_DELEGATE, profiler)
                put(AnalyticsEventUsecase.ARG_EVENT_EXTRAS, extras)
            },
            success = {
                //no-op
            },
            failure = {
                it.printStackTrace()
            }
        )
}

inline fun <reified T : AnalyticsEventUsecase> Module.trackModuleEvent(
    profiler: ProfilingDelegate<*>,
    args: Args = argsOf(),
    extras: Map<String, Any> = emptyMap()
) {

    this.usecase<T>()
        ?.execute(
            args = argsOf(args).apply {
                put(AnalyticsEventUsecase.ARG_PROFILING_DELEGATE, profiler)
                put(AnalyticsEventUsecase.ARG_EVENT_EXTRAS, extras)
            },
            success = {
                //no-op
            },
            failure = {
                it.printStackTrace()
            }
        )
}

inline fun <reified T : AnalyticsEventUsecase> Library.trackLibraryEvent(
    profiler: ProfilingDelegate<*>,
    args: Args = argsOf(),
    extras: Map<String, Any> = emptyMap()
) {

    this.usecase<T>()
        ?.execute(
            args = argsOf(args).apply {
                put(AnalyticsEventUsecase.ARG_PROFILING_DELEGATE, profiler)
                put(AnalyticsEventUsecase.ARG_EVENT_EXTRAS, extras)
            },
            success = {
                //no-op
            },
            failure = {
                it.printStackTrace()
            }
        )
}