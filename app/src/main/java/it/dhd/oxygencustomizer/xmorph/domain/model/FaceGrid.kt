package it.dhd.oxygencustomizer.xmorph.domain.model

object FaceGrid {
    const val ROW_CELLS = 4
    const val MAX_CELLS = 8
    const val CELL = 0.171f
    const val GAP = 0.0315f

    fun width(span: Int): Float = CELL * span + GAP * (span - 1)
}
