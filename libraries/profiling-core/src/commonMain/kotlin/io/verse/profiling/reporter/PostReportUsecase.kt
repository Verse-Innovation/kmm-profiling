package io.verse.profiling.reporter

import io.tagd.arch.domain.usecase.Args
import io.tagd.arch.domain.usecase.CallableUseCase
import io.tagd.arch.scopable.library.Library
import io.tagd.arch.scopable.library.repository
import io.tagd.core.BidirectionalDependentOn

class PostReportUsecase<P : Any?, T : Report<P>> :
    CallableUseCase<Unit>(),
    BidirectionalDependentOn<Library> {

    private var repository: IReportRepository<P, T>? = null

    override fun injectBidirectionalDependent(other: Library) {
        repository = other.repository()
    }

    override fun trigger(args: Args) {
        args.get<T>(ARG_REPORT)?.let { report ->
            repository?.post(report, success = {
                setValue(args, Unit)
            }, failure = {
                setError(args, it)
            })
        }
    }

    override fun release() {
        repository = null
        super.release()
    }

    companion object {
        const val ARG_REPORT = "com.verse.profiling.core.report"
    }
}