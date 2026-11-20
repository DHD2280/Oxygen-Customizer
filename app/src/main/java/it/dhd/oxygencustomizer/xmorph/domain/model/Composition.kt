package it.dhd.oxygencustomizer.xmorph.domain.model

data class Composition(
    val items: List<CompositionItem> = listOf(CompositionItem(id = 0, clock = true)),
    val nextId: Int = 1
) {
    val clockItem: CompositionItem?
        get() = items.firstOrNull { it.clock }

    val widgetStyles: Set<Int>
        get() = items.filterNot { it.clock }.mapTo(LinkedHashSet()) { it.style }

    fun item(id: Int): CompositionItem? = items.firstOrNull { it.id == id }

    fun withClock(): Composition = if (clockItem != null) this else copy(
        items = items + CompositionItem(
            id = nextId,
            clock = true
        ), nextId = nextId + 1
    )

    fun add(style: Int, large: FacePlacement?, small: FacePlacement?): Composition =
        copy(
            items = items + CompositionItem(
                id = nextId,
                style = style,
                large = large,
                small = small
            ), nextId = nextId + 1
        )

    fun remove(id: Int): Composition = copy(items = items.filterNot { it.id == id })

    fun place(id: Int, state: ItemState, placement: FacePlacement): Composition =
        copy(items = items.map { if (it.id == id) it.with(state, placement) else it })

    fun move(id: Int, steps: Int): Composition {
        val from = items.indexOfFirst { it.id == id }
        if (from < 0) return this
        val to = (from + steps).coerceIn(0, items.lastIndex)
        if (to == from) return this
        return copy(items = items.toMutableList().apply { add(to, removeAt(from)) })
    }

    fun group(ids: List<Int>, style: Int): Composition {
        val index = items.indexOfFirst { it.id in ids }
        if (index < 0) return this
        val kept = items.filterNot { it.id in ids }.toMutableList()
        kept.add(index.coerceAtMost(kept.size), CompositionItem(id = nextId, style = style))
        return copy(items = kept, nextId = nextId + 1)
    }

    fun ungroup(id: Int, members: List<UngroupedItem>): Composition {
        val index = items.indexOfFirst { it.id == id }
        if (index < 0) return this
        val created = members.mapIndexed { offset, member ->
            CompositionItem(
                id = nextId + offset,
                style = member.style,
                large = member.large,
                small = member.small,
                aodLarge = member.aodLarge?.takeIf { it != member.large },
                aodSmall = member.aodSmall?.takeIf { it != member.small },
                hidden = member.hidden
            )
        }
        val next = items.toMutableList().apply {
            removeAt(index)
            addAll(index, created)
        }
        return copy(items = next, nextId = nextId + members.size)
    }

    fun retain(available: Set<Int>): Composition =
        copy(items = items.filter { it.clock || it.style in available })
}
