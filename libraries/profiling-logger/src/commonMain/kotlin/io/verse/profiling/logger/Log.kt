package io.verse.profiling.logger

import io.verse.profiling.reporter.Report

abstract class Log(name: String, val message: String?) :
    Report<String?>(name = name, payload = message) {

    override fun toString(): String {
        return "$name${if (message.isNullOrEmpty()) "" else ":$message"}"
    }
}

open class LogE(name: String, message: String?) : Log(name = name, message = message)

open class LogV(name: String, message: String?) : Log(name = name, message = message)

open class LogI(name: String, message: String?) : Log(name = name, message = message)

open class LogD(name: String, message: String?) : Log(name = name, message = message)

open class LogF(name: String, message: String?, vararg args: Any?) :
    Log(name = name, message = message) {

    var args: Array<out Any?>
        protected set

    init {
        this.args = args
    }

    override fun toString(): String {
        return name + message
    }
}

open class LogEF(name: String, message: String?, vararg args: Any?) :
    LogF(name = name, message = message, args = args)

open class LogVF(name: String, message: String?, vararg args: Any?) :
    LogF(name = name, message = message, args = args)

open class LogIF(name: String, message: String?, vararg args: Any?) :
    LogF(name = name, message = message, args = args)

open class LogDF(name: String, message: String?, vararg args: Any?) :
    LogF(name = name, message = message, args = args)