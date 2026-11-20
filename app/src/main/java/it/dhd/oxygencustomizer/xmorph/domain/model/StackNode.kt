package it.dhd.oxygencustomizer.xmorph.domain.model

data class StackNode(
    val children: List<FaceNode>,
    val width: Float? = null,
    val height: Float? = null,
    override val placement: FacePlacement = FacePlacement()
) : FaceNode
