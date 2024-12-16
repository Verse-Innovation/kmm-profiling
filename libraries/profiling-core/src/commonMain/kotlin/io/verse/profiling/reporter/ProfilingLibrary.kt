package io.verse.profiling.reporter

import io.tagd.arch.datatype.DataObject

open class ReportingConfig(
    open val reportingUrl: String,
    open val batchSize: Int,
    open val reporterConfig: HashMap<String, HashMap<String, Any>> = hashMapOf(),
) : DataObject() {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ReportingConfig) return false

        if (reportingUrl != other.reportingUrl) return false
        if (batchSize != other.batchSize) return false
        if (reporterConfig != other.reporterConfig) return false

        return true
    }

    override fun hashCode(): Int {
        var result = reportingUrl.hashCode()
        result = 31 * result + batchSize
        result = 31 * result + reporterConfig.hashCode()
        return result
    }

    override fun toString(): String {
        return "[${stringify()}]"
    }

    protected open fun stringify(): String {
        return "reportingUrl='$reportingUrl', batchSize=$batchSize, reporterConfig='$reporterConfig'"
    }

}

data class ReportingState(val canStore: Boolean, val canSync: Boolean) :
    DataObject() {

    val off: Boolean
        get() = !canStore && !canSync
}