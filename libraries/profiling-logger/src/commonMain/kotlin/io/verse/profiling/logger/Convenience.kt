package io.verse.profiling.logger

import io.tagd.arch.access.library
import io.tagd.di.Global
import io.tagd.di.Scope

fun logging(): Logging? {
    return Global.logging()
}

fun Scope.logging(): Logging? {
    return this.library<Logging>()
}

fun logger(): Logger<DefaultLoggable>? {
    return Global.logger()
}

fun Scope.logger(): Logger<DefaultLoggable>? {
    return this.library<Logging>()?.logger
}

fun logE(method: String, message: String?) {
    logger()?.e(method, message)
}

fun logeF(method: String, message: String?, vararg args: Any?) {
    logger()?.ef(method, message, args)
}

fun logV(method: String, message: String?) {
    logger()?.v(method, message)
}

fun logVF(method: String, message: String?, vararg args: Any?) {
    logger()?.vf(method, message, args)
}

fun logI(method: String, message: String?) {
    logger()?.i(method, message)
}

fun logIF(method: String, message: String?, vararg args: Any?) {
    logger()?.`if`(method, message, args)
}

fun logD(method: String, message: String?) {
    logger()?.d(method, message)
}

fun logDF(method: String, message: String?, vararg args: Any?) {
    logger()?.df(method, message, args)
}

fun Scope.logE(method: String, message: String?) {
    this.logger()?.e(method, message)
}

fun Scope.logeF(method: String, message: String?, vararg args: Any?) {
    this.logger()?.ef(method, message, args)
}

fun Scope.logV(method: String, message: String?) {
    this.logger()?.v(method, message)
}

fun Scope.logVF(method: String, message: String?, vararg args: Any?) {
    this.logger()?.vf(method, message, args)
}

fun Scope.logI(method: String, message: String?) {
    this.logger()?.i(method, message)
}

fun Scope.logIF(method: String, message: String?, vararg args: Any?) {
    this.logger()?.`if`(method, message, args)
}

fun Scope.logD(method: String, message: String?) {
    this.logger()?.d(method, message)
}

fun Scope.logDF(method: String, message: String?, vararg args: Any?) {
    this.logger()?.df(method, message, args)
}