package it.dhd.oxygencustomizer.xmorph.domain.model

data class ClockFace(
    val name: String,
    val layers: List<FaceLayer>,
    val large: FaceNode,
    val small: FaceNode,
    val category: String = "",
    val span: Int = 1,
    val id: Int = -1,
    val aodLarge: FaceNode? = null,
    val aodSmall: FaceNode? = null,
    val aboveSubject: Set<String> = emptySet()
)
