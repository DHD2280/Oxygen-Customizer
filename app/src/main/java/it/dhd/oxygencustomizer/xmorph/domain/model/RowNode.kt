package it.dhd.oxygencustomizer.xmorph.domain.model

data class RowNode(
    val children: List<FaceNode>,
    val gap: Float = 0f,
    val align: FaceAlign = FaceAlign.END,
    override val placement: FacePlacement = FacePlacement()
) : FaceNode
