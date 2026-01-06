package de.lobianco.pr0gameunofficial

import androidx.viewpager2.widget.ViewPager2

/**
 * Helper um ViewPager2 Swipen zu aktivieren/deaktivieren
 * Einfache Lösung ohne Touch-Intercepting
 */
object ViewPagerHelper {

    private var swipeEnabled = true
    private var cachedViewPager: ViewPager2? = null

    fun setupViewPager(viewPager: ViewPager2) {
        cachedViewPager = viewPager

        // Reduziere Sensitivity durch höheren Touch Slop
        try {
            val recyclerView = viewPager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
            recyclerView?.let {
                val field = androidx.recyclerview.widget.RecyclerView::class.java.getDeclaredField("mTouchSlop")
                field.isAccessible = true
                val touchSlop = field.getInt(it)
                field.setInt(it, touchSlop * 3) // 3x weniger sensitiv
            }
        } catch (e: Exception) {
            android.util.Log.e("ViewPagerHelper", "Could not adjust touch slop: ${e.message}")
        }

        viewPager.isUserInputEnabled = true

        android.util.Log.d("ViewPagerHelper", "ViewPager setup complete")
    }

    fun setSwipeEnabled(enabled: Boolean) {
        swipeEnabled = enabled
        cachedViewPager?.isUserInputEnabled = enabled

        android.util.Log.d("ViewPagerHelper", "Swipe enabled: $enabled")
    }

    fun isSwipeEnabled(): Boolean = swipeEnabled
}