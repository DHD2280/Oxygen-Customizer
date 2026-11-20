package it.dhd.oxygencustomizer.xmorph.domain.model

enum class ReportLog(val fileName: String) {
    SYSTEMUI("systemui.log"),
    CRASH("crash.log"),
    FRAMEWORK("xposed.log")
}
