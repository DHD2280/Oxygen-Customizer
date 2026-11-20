package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemBounds
import it.dhd.oxygencustomizer.xmorph.domain.model.SnapResult
import kotlin.math.abs

object ItemSnapper {

    private const val THRESHOLD = 0.018f
    private const val EDGE = 0.0225f
    private const val HALF = 0.5f

    fun snap(moving: ItemBounds, others: List<ItemBounds>): SnapResult {
        val targetsX =
            others.flatMap { listOf(it.left, (it.left + it.right) / 2f, it.right) } + listOf(
                EDGE,
                HALF,
                1f - EDGE
            )
        val targetsY = others.flatMap { listOf(it.top, (it.top + it.bottom) / 2f, it.bottom) }
        val x = best(listOf(moving.left, (moving.left + moving.right) / 2f, moving.right), targetsX)
        val y = best(listOf(moving.top, (moving.top + moving.bottom) / 2f, moving.bottom), targetsY)
        return SnapResult(
            x?.delta ?: 0f,
            y?.delta ?: 0f,
            x?.target,
            y?.target,
            x?.let { ANCHORS[it.feature] })
    }

    private fun best(features: List<Float>, targets: List<Float>): Match? =
        features.flatMapIndexed { index, feature ->
            targets.map { target ->
                Match(
                    target - feature,
                    target,
                    index
                )
            }
        }
            .filter { abs(it.delta) < THRESHOLD }
            .minByOrNull { abs(it.delta) }

    private val ANCHORS = listOf(FaceAlign.START, FaceAlign.CENTER, FaceAlign.END)
}
