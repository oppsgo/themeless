package io.github.oppsgo.themeless.demo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import io.github.oppsgo.themeless.R

/**
 * 列表颜色靠布局里的资源引用。
 * 换主题后：已贴上的条目由整页 refresh；复用回来的条目由 RecyclerViewResourceBinding 补刷。
 */
internal class ThemeRowAdapter(
    private val indexes: MutableList<Int>,
    private val onItemClick: (() -> Unit)? = null,
) : RecyclerView.Adapter<ThemeRowAdapter.Holder>() {

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.itemTitle)
        val subtitle: TextView = itemView.findViewById(R.id.itemSubtitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_theme_row, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.title.text = holder.itemView.context.getString(
            R.string.theme_demo_list_title_format,
            indexes[position],
        )
        holder.subtitle.setText(R.string.theme_demo_list_subtitle)
        holder.itemView.setOnClickListener { onItemClick?.invoke() }
    }

    override fun getItemCount(): Int = indexes.size

    fun appendMore(count: Int = 5) {
        val start = indexes.size
        val next = (start until start + count).toList()
        indexes.addAll(next)
        notifyItemRangeInserted(start, next.size)
    }
}
