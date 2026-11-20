package it.dhd.oxygencustomizer.xmorph.domain.model

data class UserFont(
    val id: String,
    val name: String
) {
    val family: String
        get() = SCHEME + id

    companion object {
        const val SCHEME = "user:"

        fun idOf(font: String): String? =
            font.takeIf { it.startsWith(SCHEME) }?.removePrefix(SCHEME)?.substringBefore(AXES)
                ?.takeIf { it.isNotEmpty() }

        private const val AXES = '?'
    }
}
