package it.dhd.oxygencustomizer.xmorph.domain.model

data class PresetImportResult(
    val imported: Int = 0,
    val error: PresetFileError? = null
)
