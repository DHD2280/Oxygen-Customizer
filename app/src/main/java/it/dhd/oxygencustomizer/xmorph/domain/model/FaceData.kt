package it.dhd.oxygencustomizer.xmorph.domain.model

data class FaceData(
    val weather: WeatherSnapshot? = null,
    val batteryLevel: Int? = null,
    val charging: Boolean = false,
    val batteryFull: Boolean = false,
    val chargeRemainingMs: Long? = null,
    val nextAlarm: Long? = null,
    val use24Hour: Boolean = false,
    val mediaTitle: String? = null,
    val mediaArtist: String? = null,
    val mediaPlaying: Boolean = false,
    val mediaArt: Int = 0,
    val notificationCount: Int? = null,
    val events: List<CalendarEvent> = emptyList(),
    val steps: Int? = null,
    val dataUsageBytes: Long? = null,
    val tiles: Map<FaceTile, Int> = emptyMap()
) {
    fun tileState(tile: FaceTile): Int = tiles[tile] ?: TILE_OFF

    companion object {
        const val TILE_OFF = 0
        const val TILE_ON = 1
        const val TILE_ALT = 2
    }
}
