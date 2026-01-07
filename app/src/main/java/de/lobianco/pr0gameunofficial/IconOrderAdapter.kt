package de.lobianco.pr0gameunofficial

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.util.Collections

class IconOrderAdapter(
    private val icons: MutableList<ButtonIcon>,
    private val onOrderChanged: (List<ButtonIcon>) -> Unit,
    private val onStartDrag: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<IconOrderAdapter.IconViewHolder>() {

    inner class IconViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val dragHandle: ImageView = view.findViewById(R.id.dragHandle)
        val iconImage: ImageView = view.findViewById(R.id.iconImage)
        val iconName: TextView = view.findViewById(R.id.iconName)
        val iconToggle: SwitchCompat = view.findViewById(R.id.iconToggle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon_order, parent, false)
        return IconViewHolder(view)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        val icon = icons[position]

        // Set icon
        holder.iconImage.setImageResource(icon.iconRes)
        icon.tintColor?.let {
            holder.iconImage.setColorFilter(it)
        }

        // Set name
        holder.iconName.text = icon.name

        // Set toggle
        holder.iconToggle.isChecked = icon.isVisible
        holder.iconToggle.setOnCheckedChangeListener { _, isChecked ->
            android.util.Log.d("IconOrderAdapter", "Toggle changed for ${icon.name}: $isChecked")
            icon.isVisible = isChecked
            onOrderChanged(icons)
        }

        // Long press (3 seconds) to start drag - use local variables
        var longPressHandler: Handler? = Handler(Looper.getMainLooper())
        var longPressRunnable: Runnable? = null

        longPressRunnable = Runnable {
            // Start drag after 3 seconds
            onStartDrag(holder)
            holder.dragHandle.setColorFilter(Color.parseColor("#64b5f6"))
        }

        holder.dragHandle.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // Start 3 second timer
                    longPressRunnable?.let {
                        longPressHandler?.postDelayed(it, 3000)
                    }
                    holder.dragHandle.setColorFilter(Color.parseColor("#ffffff"))
                    true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    // Cancel timer
                    longPressRunnable?.let { longPressHandler?.removeCallbacks(it) }
                    holder.dragHandle.setColorFilter(Color.parseColor("#aaaaaa"))
                    v.performClick()
                    true
                }
                else -> false
            }
        }
    }

    override fun getItemCount() = icons.size

    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(icons, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(icons, i, i - 1)
            }
        }

        // Update order values
        icons.forEachIndexed { index, icon ->
            icon.order = index
        }

        notifyItemMoved(fromPosition, toPosition)
        onOrderChanged(icons)
        return true
    }
}