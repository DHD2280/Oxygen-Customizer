package it.dhd.oxygencustomizer.xmorph.domain.model

data class ClockSettings(
    val clockEnabled: Boolean,
    val faceStyle: Int,
    val burnInProtection: Boolean,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val composition: Composition = Composition()
)
