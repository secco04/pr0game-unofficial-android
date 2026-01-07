package de.lobianco.pr0gameunofficial

data class ButtonIcon(
    val id: String,
    val name: String,
    val iconRes: Int,
    val tintColor: Int?,
    var isVisible: Boolean = true,
    var order: Int = 0
)
