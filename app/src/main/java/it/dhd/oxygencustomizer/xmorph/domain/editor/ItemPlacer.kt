package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemBounds

object ItemPlacer {

    private const val EDGE = 0.0225f
    private const val GAP = 0.03f
    private const val STEP = 0.01f

    fun place(
        occupied: List<ItemBounds>,
        width: Float,
        height: Float,
        startY: Float,
        maxY: Float
    ): FacePlacement {
        val limit = (maxY - height).coerceAtLeast(0f)
        val top = startY.coerceIn(0f, limit)
        val columns = columns(width)
        free(occupied, width, height, top, limit, columns)?.let { return it }
        free(occupied, width, height, 0f, top, columns)?.let { return it }
        return FacePlacement(columns.first(), top, FaceAlign.START)
    }

    private fun free(
        occupied: List<ItemBounds>,
        width: Float,
        height: Float,
        from: Float,
        to: Float,
        columns: List<Float>
    ): FacePlacement? {
        var y = from
        while (y <= to) {
            columns.forEach { x ->
                val candidate = ItemBounds(x, y, x + width, y + height)
                if (occupied.none { it.overlaps(candidate) }) return FacePlacement(
                    x,
                    y,
                    FaceAlign.START
                )
            }
            y += STEP
        }
        return null
    }

    private fun columns(width: Float): List<Float> {
        val span = 1f - EDGE * 2f
        if (width >= span) return listOf(((1f - width) / 2f).coerceAtLeast(0f))
        val count = ((span + GAP) / (width + GAP)).toInt().coerceAtLeast(1)
        val used = count * width + (count - 1) * GAP
        val start = (1f - used) / 2f
        return List(count) { index -> start + index * (width + GAP) }
    }
}
