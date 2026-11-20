package it.dhd.oxygencustomizer.xmorph.domain.model

data class ItemBounds(val left: Float, val top: Float, val right: Float, val bottom: Float) {
    val width: Float
        get() = right - left

    val height: Float
        get() = bottom - top

    fun overlaps(other: ItemBounds): Boolean =
        left < other.right && other.left < right && top < other.bottom && other.top < bottom

    companion object {
        fun of(placement: FacePlacement, width: Float, height: Float): ItemBounds {
            val left = when (placement.anchor) {
                FaceAlign.START -> placement.x
                FaceAlign.CENTER -> placement.x - width / 2f
                FaceAlign.END -> placement.x - width
            }
            return ItemBounds(left, placement.y, left + width, placement.y + height)
        }
    }
}
