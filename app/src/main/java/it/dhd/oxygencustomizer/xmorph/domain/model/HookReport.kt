package it.dhd.oxygencustomizer.xmorph.domain.model

data class HookReport(
    val timestamp: Long,
    val framework: String,
    val systemUiVersion: String,
    val buildId: String,
    val adapter: String?,
    val signals: List<HookSignal>,
    val clockFace: String?,
    val errors: List<String>,
    val largeFrame: ClockFrame? = null,
    val smallFrame: ClockFrame? = null,
    val screenAspect: Float? = null,
    val moduleUpdatedAt: Long = 0L,
    val moduleLoadedAt: Long = 0L,
    val uptimeMs: Long = 0L,
    val probes: List<String> = emptyList(),
    val counters: Map<String, Int> = emptyMap(),
    val state: Map<String, String> = emptyMap(),
    val logs: List<String> = emptyList()
) {
    val isHealthy: Boolean
        get() = adapter != null && signals.all { it.source != null } && errors.isEmpty()

    fun isStale(installedAt: Long): Boolean =
        moduleUpdatedAt in 1 until installedAt - STALE_SLACK_MS

    fun attachState(installedAt: Long): ClockAttachState = when {
        state[STATE_ENABLED] == false.toString() -> ClockAttachState.OFF
        isStale(installedAt) -> ClockAttachState.STALE
        clockFace != null -> ClockAttachState.ATTACHED
        adapter == null -> ClockAttachState.UNSUPPORTED
        (counters[LOCKSCREEN_SHOWN] ?: 0) == 0 -> ClockAttachState.WAITING
        else -> ClockAttachState.FAILED
    }

    companion object {
        const val LOCKSCREEN_SHOWN = "lockscreenShown"
        private const val STATE_ENABLED = "enabled"
        private const val STALE_SLACK_MS = 2_000L
    }
}
