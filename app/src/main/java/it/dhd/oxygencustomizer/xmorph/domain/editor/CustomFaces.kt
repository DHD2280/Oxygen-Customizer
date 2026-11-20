package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ElementSpec
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceChangeStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceImage
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceSleepStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode

object CustomFaces {

    const val ELEMENT_CATEGORY = "element"
    const val OWNED_CATEGORY = "owned"
    const val GROUP_CATEGORY = "group"

    fun isPrivate(face: ClockFace): Boolean =
        face.category == ELEMENT_CATEGORY || face.category == OWNED_CATEGORY || face.category == GROUP_CATEGORY

    fun element(name: String, specs: List<ElementSpec>): ClockFace? {
        if (specs.isEmpty()) return null
        val layers = specs.mapIndexed { index, spec -> spec.layer.copy(id = ELEMENT_ID + index) }
        fun tree(scale: Float, placement: FacePlacement): FaceNode {
            val nodes =
                specs.mapIndexed { index, spec -> LayerNode(ELEMENT_ID + index, spec.size * scale) }
            return if (nodes.size == 1) nodes.first().copy(placement = placement) else StackNode(
                children = nodes,
                placement = placement
            )
        }
        return ClockFace(
            name = name,
            layers = layers,
            large = tree(1f, FacePlacement(CENTER_X, 0f, FaceAlign.CENTER)),
            small = tree(SMALL_SCALE, FacePlacement(EDGE_X, 0f, FaceAlign.START)),
            category = ELEMENT_CATEGORY
        )
    }

    fun text(name: String, text: String): ClockFace {
        val layer = FaceLayer(
            id = TEXT_ID,
            type = FaceLayerType.TEXT,
            format = text,
            font = TEXT_FONT,
            weight = TEXT_WEIGHT,
            width = TEXT_WIDTH,
            height = TEXT_HEIGHT,
            color = WHITE,
            change = FaceChangeStyle.FADE,
            sleepStyle = FaceSleepStyle.THIN
        )
        return ClockFace(
            name = name,
            layers = listOf(layer),
            large = LayerNode(
                TEXT_ID,
                TEXT_LARGE,
                placement = FacePlacement(CENTER_X, 0f, FaceAlign.CENTER)
            ),
            small = LayerNode(
                TEXT_ID,
                TEXT_SMALL,
                placement = FacePlacement(EDGE_X, 0f, FaceAlign.START)
            ),
            span = TEXT_SPAN,
            category = ELEMENT_CATEGORY
        )
    }

    fun image(name: String, image: FaceImage): ClockFace {
        val layer = FaceLayer(
            id = IMAGE_ID,
            type = FaceLayerType.IMAGE,
            format = image.id,
            aspect = image.aspect,
            radius = IMAGE_RADIUS,
            change = FaceChangeStyle.NONE,
            sleepStyle = FaceSleepStyle.NONE,
            sleepOpacity = 0f
        )
        val span = if (image.aspect > WIDE_ASPECT) WIDE_SPAN else IMAGE_SPAN
        return ClockFace(
            name = name,
            layers = listOf(layer),
            large = LayerNode(
                IMAGE_ID,
                IMAGE_LARGE * span / IMAGE_SPAN,
                placement = FacePlacement(CENTER_X, 0f, FaceAlign.CENTER)
            ),
            small = LayerNode(
                IMAGE_ID,
                IMAGE_SMALL * span / IMAGE_SPAN,
                placement = FacePlacement(EDGE_X, 0f, FaceAlign.START)
            ),
            span = span,
            category = ELEMENT_CATEGORY
        )
    }

    private const val TEXT_ID = "text"
    private const val ELEMENT_ID = "e"
    private const val SMALL_SCALE = 0.6f
    private const val IMAGE_ID = "image"
    private const val TEXT_FONT = "res:google_sans_flex?opsz=144"
    private const val TEXT_WEIGHT = 500
    private const val TEXT_WIDTH = 100
    private const val TEXT_HEIGHT = 700
    private const val WHITE = "#FFFFFF"
    private const val TEXT_LARGE = 0.06f
    private const val TEXT_SMALL = 0.036f
    private const val TEXT_SPAN = 4
    private const val IMAGE_LARGE = 0.34f
    private const val IMAGE_SMALL = 0.17f
    private const val IMAGE_RADIUS = 0.12f
    private const val IMAGE_SPAN = 2
    private const val WIDE_SPAN = 4
    private const val WIDE_ASPECT = 1.6f
    private const val CENTER_X = 0.5f
    private const val EDGE_X = 0.0225f
}
