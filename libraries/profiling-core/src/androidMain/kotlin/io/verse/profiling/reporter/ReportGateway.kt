@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package io.verse.profiling.reporter

import io.tagd.arch.access.library
import io.tagd.arch.domain.crosscutting.async.networkIO
import io.tagd.langx.Callback
import io.tagd.core.BidirectionalDependentOn
import io.tagd.langx.IllegalAccessException
import io.tagd.langx.reflection.nativeTypeOf
import io.verse.latch.core.ExecutionException
import io.verse.latch.core.InterceptorGateway
import io.verse.latch.core.Latch
import io.verse.latch.core.Request
import io.verse.latch.core.RequestPayloadBody
import io.verse.latch.core.ResultContext
import io.verse.latch.core.converter.ConversionSequence
import io.verse.latch.core.converter.JsonConverterFactory
import io.verse.latch.core.newHttpsPostRequestBuilder
import io.verse.latch.core.newRequestPayloadBody
import io.verse.profiling.core.ProfilingLibrary

actual class ReportGateway<
    REPORT_PAYLOAD : Any?,
    REPORT : Report<REPORT_PAYLOAD>
> actual constructor() : InterceptorGateway<List<REPORT>, String, Unit>(),
    IReportGateway<REPORT_PAYLOAD, REPORT>, BidirectionalDependentOn<ProfilingLibrary> {

    private var library: ProfilingLibrary? = null

    override fun injectBidirectionalDependent(other: ProfilingLibrary) {
        library = other
    }

    override fun postReports(
        reports: List<REPORT>,
        success: Callback<Unit>,
        failure: Callback<Throwable>,
    ) {

        networkIO {
            val request = newHttpsPostRequest(reports)
            request?.let {
                fire(request, success, failure)
            } ?: failure.invoke(IllegalAccessException("failed to create request"))
        }
    }

    private fun newHttpsPostRequest(reports: List<REPORT>): Request<List<REPORT>, String>? {

        // look for dependsOn dependencies at the same parent scope
        return library?.let { profilingLibrary ->
            profilingLibrary.thisScope.library<Latch>()
                ?.newHttpsPostRequestBuilder(
                    url = profilingLibrary.config.reportingUrl,
                    body = newRequestPayloadBody(
                        content = reports,
                        type = nativeTypeOf<List<REPORT>>()
                    ),
                    interceptor = this
                )
                ?.build()
        }
    }

    override fun success(context: ResultContext<List<REPORT>, String>, result: String) {
        val requestContext = requestContext(context.identifier)
        requestContext?.success?.invoke(Unit)
    }

    override fun failure(exception: ExecutionException) {
        val requestContext = requestContext(exception.identifier)
        requestContext?.failure?.invoke(exception)
    }

    override fun release() {
        library = null
    }
}