package it.dhd.oxygencustomizer.xmorph.domain.model

data class LayerNode(
    val id: String,
    val size: Float,
    val alpha: Float = 1f,
    val rotation: Float = 0f,
    val tiltX: Float = 0f,
    val tiltY: Float = 0f,
    val width: Int? = null,
    val height: Int? = null,
    val tracking: Float? = null,
    val weight: Int? = null,
    val color: String? = null,
    val lineHeight: Float? = null,
    val stretch: Float? = null,
    val stretchX: Float? = null,
    val effect: FaceTextEffect? = null,
    val align: FaceAlign? = null,
    override val placement: FacePlacement = FacePlacement()
) : FaceNode
