package it.dhd.oxygencustomizer.xmorph.engine.render

import android.icu.text.MessageFormat
import java.util.Locale

internal object WordTime {

    private val UNITS = listOf(
        "zero",
        "one",
        "two",
        "three",
        "four",
        "five",
        "six",
        "seven",
        "eight",
        "nine",
        "ten",
        "eleven",
        "twelve",
        "thirteen",
        "fourteen",
        "fifteen",
        "sixteen",
        "seventeen",
        "eighteen",
        "nineteen"
    )
    private val TENS = listOf("", "", "twenty", "thirty", "forty", "fifty")

    fun hour(hour: Int, locale: Locale = Locale.ENGLISH): String = number(twelve(hour), locale)

    fun hour24(hour: Int, locale: Locale = Locale.ENGLISH): String = number(hour % FULL_DAY, locale)

    fun minute(minute: Int, locale: Locale = Locale.ENGLISH): String {
        if (!locale.isEnglish()) return if (minute == 0) "" else spell(locale, minute)
        return if (minute == 0) O_CLOCK else englishMinute(minute)
    }

    fun time(hour: Int, minute: Int, locale: Locale = Locale.ENGLISH): String =
        join(hour(hour, locale), minute(minute, locale))

    fun time24(
        hour: Int,
        minute: Int,
        phrases: WordPhrases,
        locale: Locale = Locale.ENGLISH
    ): String {
        val numbers = phrases.numbers(locale)
        val tail = when {
            minute != 0 -> if (numbers.isEnglish()) englishMinute(minute) else spell(
                numbers,
                minute
            )

            numbers.isEnglish() -> phrases.hundred
            else -> ""
        }
        return join(hour24(hour, numbers), tail)
    }

    fun fuzzyLead(
        hour: Int,
        minute: Int,
        phrases: WordPhrases,
        locale: Locale = Locale.ENGLISH
    ): String {
        val numbers = phrases.numbers(locale)
        val rounded = rounded(minute)
        return when {
            rounded == 0 || rounded == HOUR -> ""
            rounded == QUARTER -> phrases.leadQuarterPast
            rounded == HALF -> phrases.leadHalfPast
            rounded == HOUR - QUARTER -> phrases.leadQuarterTo
            rounded < HALF -> phrases.leadPast.format(number(rounded, numbers))
            else -> phrases.leadTo.format(number(HOUR - rounded, numbers))
        }
    }

    fun fuzzyHour(
        hour: Int,
        minute: Int,
        phrases: WordPhrases,
        locale: Locale = Locale.ENGLISH
    ): String {
        val numbers = phrases.numbers(locale)
        val rounded = rounded(minute)
        return when {
            rounded == 0 -> phrases.oclock.format(hour(hour, numbers))
            rounded == HOUR -> phrases.oclock.format(hour(hour + 1, numbers))
            rounded > HALF -> hour(hour + 1, numbers)
            else -> hour(hour, numbers)
        }
    }

    fun fuzzy(
        hour: Int,
        minute: Int,
        phrases: WordPhrases,
        locale: Locale = Locale.ENGLISH
    ): String {
        val numbers = phrases.numbers(locale)
        val rounded = rounded(minute)
        val current = hour(hour, numbers)
        val next = hour(hour + 1, numbers)
        return when {
            rounded == 0 -> phrases.oclock.format(current)
            rounded == HOUR -> phrases.oclock.format(next)
            rounded == QUARTER -> phrases.quarterPast.format(current)
            rounded == HALF -> phrases.halfPast.format(current)
            rounded == HOUR - QUARTER -> phrases.quarterTo.format(next)
            rounded < HALF -> phrases.past.format(number(rounded, numbers), current)
            else -> phrases.to.format(number(HOUR - rounded, numbers), next)
        }
    }

    private fun twelve(hour: Int): Int = (hour % HALF_DAY).let { if (it == 0) HALF_DAY else it }

    private fun number(value: Int, locale: Locale): String =
        if (locale.isEnglish()) english(value) else spell(locale, value)

    private fun english(value: Int): String = when {
        value < 20 -> UNITS[value]
        value % 10 == 0 -> TENS[value / 10]
        else -> TENS[value / 10] + "-" + UNITS[value % 10]
    }

    private fun englishMinute(minute: Int): String =
        if (minute < 10) "oh " + UNITS[minute] else english(minute)

    private fun join(vararg parts: String): String =
        parts.filter { it.isNotEmpty() }.joinToString(" ")

    private fun Locale.isEnglish(): Boolean = language == Locale.ENGLISH.language

    private fun spell(locale: Locale, value: Int): String = runCatching {
        synchronized(formats) {
            formats.getOrPut(locale) {
                MessageFormat(
                    SPELLOUT,
                    locale
                )
            }
        }.format(arrayOf<Any>(value))
    }.getOrDefault(value.toString())

    private val formats = HashMap<Locale, MessageFormat>()

    private fun rounded(minute: Int): Int = (minute + 2) / STEP * STEP

    private const val O_CLOCK = "o'clock"
    private const val SPELLOUT = "{0,spellout}"
    private const val HALF_DAY = 12
    private const val FULL_DAY = 24
    private const val HOUR = 60
    private const val HALF = 30
    private const val QUARTER = 15
    private const val STEP = 5
}
