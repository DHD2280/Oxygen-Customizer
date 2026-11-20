package it.dhd.oxygencustomizer.xmorph.domain.model

data class CompositionItem(
    val id: Int,
    val clock: Boolean = false,
    val style: Int = -1,
    val large: FacePlacement? = null,
    val small: FacePlacement? = null,
    val aodLarge: FacePlacement? = null,
    val aodSmall: FacePlacement? = null,
    val hidden: Set<ItemState> = emptySet(),
    val largeScale: Float = 1f,
    val smallScale: Float = 1f,
    val aboveSubject: Boolean = false
) {
    val isDefault: Boolean
        get() = large == null && small == null && aodLarge == null && aodSmall == null && hidden.isEmpty() && largeScale == 1f && smallScale == 1f && !aboveSubject

    fun visible(state: ItemState): Boolean = state !in hidden

    fun scale(state: ItemState): Float =
        if (state == ItemState.LARGE || state == ItemState.AOD_LARGE) largeScale else smallScale

    val hasAod: Boolean
        get() = aodLarge != null || aodSmall != null || ItemState.AOD_LARGE in hidden || ItemState.AOD_SMALL in hidden

    fun placement(state: ItemState): FacePlacement? = when (state) {
        ItemState.LARGE -> large
        ItemState.SMALL -> small
        ItemState.AOD_LARGE -> aodLarge ?: large
        ItemState.AOD_SMALL -> aodSmall ?: small
    }

    fun with(state: ItemState, placement: FacePlacement): CompositionItem = when (state) {
        ItemState.LARGE -> copy(large = placement)
        ItemState.SMALL -> copy(small = placement)
        ItemState.AOD_LARGE -> copy(aodLarge = placement)
        ItemState.AOD_SMALL -> copy(aodSmall = placement)
    }
}
