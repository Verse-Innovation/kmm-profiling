package io.verse.profiling.reporter

import io.tagd.core.Identifiable
import io.tagd.core.Immutable
import io.tagd.core.Nameable
import io.tagd.langx.time.Millis
import io.tagd.langx.System
import io.tagd.langx.datatype.UUID
import io.tagd.langx.time.UnixEpochInMillis

open class Report<P>(
    override val identifier: UUID = UUID(),
    override val name: String,
    val payload: P,
) : Observation(), Nameable, Immutable, Identifiable<UUID> {

    val createdAt = UnixEpochInMillis(Millis(millis = System.millis()))

    open fun copy(name: String = this.name, payload: P = this.payload) : Report<P> {
        return Report(
            identifier = this.identifier,
            name = name,
            payload = payload
        )
    }

    companion object {
        val NULL = Report(name = "", payload = null)
    }
}