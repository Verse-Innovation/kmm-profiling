package io.verse.profiling.android.access

import io.tagd.di.Scope
import io.tagd.langx.Context
import io.verse.latch.core.Latch
import io.verse.latch.core.LatchInitializer
import io.verse.latch.core.converter.PlainTextFieldConverterFactory
import io.verse.latch.core.converter.gson.JsonCodecContentConverterFactory
import io.verse.latch.core.okhttp.OkHttpProtocolGateway
import io.verse.profiling.android.MyApplication

class MyLatchInitializer(within: MyApplication) :
    LatchInitializer<MyApplication>(within, within, "latch") {

    override fun initLatch(context: Context, outerScope: Scope, name: String?): Latch {
        return Latch.Builder()
            .name(name)
            .scope(outerScope)
            .context(context)
            .addBaseUrl("http", "http://demo2921399.mockable.io")
            .addBaseUrl("https", "https://demo2921399.mockable.io")
            .register("http", OkHttpProtocolGateway())
            .register("https", OkHttpProtocolGateway())
            .addPayloadConverterFactory(JsonCodecContentConverterFactory.new())
            .addPayloadConverterFactory(PlainTextFieldConverterFactory())
            .build()
    }

    override fun release() {
        // no-op
    }
}