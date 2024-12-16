package io.verse.profiling.core

import io.tagd.arch.datatype.DataObject
import io.tagd.core.Identifiable
import io.tagd.core.Mappable
import io.tagd.core.State
import io.tagd.langx.time.Millis
import io.tagd.langx.System
import io.tagd.langx.datatype.UUID
import io.tagd.langx.time.UnixEpochInMillis

class Session(
    override val identifier: UUID = UUID(),
    var config: Config,
    val journey: Journey
) : DataObject(), Mappable, Identifiable<UUID> {

    data class Config(
        val ignorableInactiveTimeMs: Millis
    )

    class SessionExpiredException(message: String? = null) : Exception(message)

    data class UsagePeriod(
        val usageScope: ProfilingScope.UsageScope,
        val startTime: UnixEpochInMillis,
        var endTime: UnixEpochInMillis
    ) {

        fun duration() = endTime.millisSince1970.millis - startTime.millisSince1970.millis
    }

    val sessionScope = ProfilingScope.SessionScope(
        journey.scope,
        journey.watcher,
        journey.scope.profileableReference
    )

    val createdAt = UnixEpochInMillis(Millis(System.millis()))

    private var inactiveAt: UnixEpochInMillis? = null

    private val intervals = arrayListOf<UsagePeriod>()

    var ongoingInterval: UsagePeriod? = null
        private set

    var ongoingUsageScope: ProfilingScope.UsageScope? = null
        private set

    val progressiveState: State = State()

    init {
        createUsagePeriod()
    }

    private fun createUsagePeriod() {
        ongoingInterval = UsagePeriod(
            usageScope = ProfilingScope.UsageScope(
                sessionScope,
                journey.watcher,
                sessionScope.profileableReference
            ),
            startTime = UnixEpochInMillis(Millis(System.millis())),
            endTime = UnixEpochInMillis(Millis(System.millis()))
        ).also {
            ongoingUsageScope = it.usageScope
            intervals.add(it)
        }
    }

    fun update(config: Config) {
        this.config = config
    }

    fun inactive() {
        ongoingInterval?.endTime = UnixEpochInMillis(Millis(System.millis()))
        ongoingUsageScope?.leave()
        ongoingInterval = null

        inactiveAt = UnixEpochInMillis(Millis(System.millis()))
    }

    fun active() {
        inactiveAt?.let {
            val now = System.millis()
            if (now - it.millisSince1970.millis >= config.ignorableInactiveTimeMs.millis) {
                sessionScope.leave()
                throw SessionExpiredException()
            }
            inactiveAt = null
            createUsagePeriod()
        }
    }

    fun duration(): Long {
        var duration: Long = 0L
        intervals.forEach {
            duration += it.duration()
        }
        return duration
    }

    fun plusState(property: String, value: Any) {
        progressiveState.put(property, value)
    }

    override fun map(): HashMap<String, Any> {
        return hashMapOf()
    }
}