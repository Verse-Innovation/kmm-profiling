package io.verse.profiling.android

import io.verse.profiling.anomaly.logException
import io.verse.profiling.logger.logV
import io.verse.profiling.logger.logger

class SomeArbitraryClassWithLogging {

    fun doSomeStuff() {
        logger()?.e("myMethod", "error log")
        logV("myMethod", "verbose log")
        logException(NullPointerException("fake null"))
    }
}