package it.dhd.oxygencustomizer.xmorph.engine.render

private val DIGIT_RUN = Regex("\\d+")
private val ROMAN_VALUES = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)
private val ROMAN_SYMBOLS =
    arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")

internal fun String.withRomanNumerals(): String = DIGIT_RUN.replace(this) { match ->
    val value = match.value.toIntOrNull() ?: return@replace match.value
    if (value <= 0 || value >= 4000) return@replace match.value
    var rest = value
    buildString {
        ROMAN_VALUES.forEachIndexed { index, step ->
            while (rest >= step) {
                append(ROMAN_SYMBOLS[index])
                rest -= step
            }
        }
    }
}
