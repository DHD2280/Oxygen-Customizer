package it.dhd.oxygencustomizer.xmorph.domain.model

data class WeatherSnapshot(
    val temperature: Int,
    val high: Int,
    val low: Int,
    val conditionCode: Int,
    val condition: String,
    val isDay: Boolean,
    val humidity: Int,
    val wind: Int,
    val windUnit: String,
    val location: String,
    val metric: Boolean,
    val timestamp: Long,
    val sunrises: List<Long> = emptyList(),
    val sunsets: List<Long> = emptyList(),
    val uvIndex: Float? = null,
    val hourly: List<HourlyPoint> = emptyList()
)
