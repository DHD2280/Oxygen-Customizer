package it.dhd.oxygencustomizer.xmorph.engine.render

import android.content.res.Resources
import it.dhd.oxygencustomizer.R

internal data class DataPhrases(
    val charging: String,
    val charged: String,
    val chargingLeft: String,
    val hoursMinutes: String,
    val minutes: String,
    val noAlarm: String,
    val notCharging: String,
    val sunrise: String,
    val sunset: String,
    val uv: String,
    val noMedia: String,
    val noEvents: String,
    val allDay: String,
    val happeningNow: String,
    val inMinutes: String,
    val tomorrow: String
) {
    companion object {
        fun from(resources: Resources): DataPhrases = DataPhrases(
            charging = resources.getString(R.string.data_charging),
            charged = resources.getString(R.string.data_charged),
            chargingLeft = resources.getString(R.string.data_charging_left),
            hoursMinutes = resources.getString(R.string.data_hours_minutes),
            minutes = resources.getString(R.string.data_minutes),
            noAlarm = resources.getString(R.string.data_no_alarm),
            notCharging = resources.getString(R.string.data_not_charging),
            sunrise = resources.getString(R.string.data_sunrise),
            sunset = resources.getString(R.string.data_sunset),
            uv = resources.getString(R.string.data_uv),
            noMedia = resources.getString(R.string.data_no_media),
            noEvents = resources.getString(R.string.data_no_events),
            allDay = resources.getString(R.string.data_all_day),
            happeningNow = resources.getString(R.string.data_happening_now),
            inMinutes = resources.getString(R.string.data_in_minutes),
            tomorrow = resources.getString(R.string.data_tomorrow)
        )
    }
}
