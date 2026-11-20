package it.dhd.oxygencustomizer.xmorph.domain.model

data class ColumnNode(
    val children: List<FaceNode>,
    val gap: Float = 0f,
    val align: FaceAlign = FaceAlign.CENTER,
    override val placement: FacePlacement = FacePlacement()
) : FaceNode
