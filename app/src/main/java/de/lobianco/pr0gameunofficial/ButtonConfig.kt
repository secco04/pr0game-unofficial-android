package de.lobianco.pr0gameunofficial

import android.graphics.drawable.Drawable

data class ButtonConfig(
    val id: String,
    val nameResId: Int,
    val iconRes: Int,
    val tintColor: Int? = null,
    val hasBadge: Boolean = false,
    val onClick: () -> Unit
)
