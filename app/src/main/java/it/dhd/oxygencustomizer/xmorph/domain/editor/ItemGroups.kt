package it.dhd.oxygencustomizer.xmorph.domain.editor

object ItemGroups {

    private const val GROUP = "group:"
    private const val CLOCK = "clock"

    fun keyOf(layerId: String): String = GROUP + (FaceComposer.itemOf(layerId)?.toString() ?: CLOCK)

    fun keyOfItem(id: Int, clock: Boolean): String = GROUP + if (clock) CLOCK else id.toString()

    fun isGroup(id: String?): Boolean = id?.startsWith(GROUP) == true

    fun itemId(key: String): Int? = key.removePrefix(GROUP).toIntOrNull()

    fun belongs(key: String, layerId: String): Boolean = keyOf(layerId) == key
}
