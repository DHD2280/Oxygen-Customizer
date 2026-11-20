package it.dhd.oxygencustomizer.xmorph.domain.model

data class FrameworkInfo(
    val name: String,
    val version: String,
    val apiVersion: Int,
    val scope: List<String>
)
