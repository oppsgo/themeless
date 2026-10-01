package io.github.oppsgo.themeless.demo

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.binding.ViewResourceBinding
import io.github.oppsgo.android.theme.resource.ColorRef
import io.github.oppsgo.themeless.R

/**
 * AbsListView 条目：布局资源引用 + 复用挂上时由 AbsListView Binding 补刷。
 */
internal class ThemeAbsListAdapter(
    private val context: Context,
    private val indexes: MutableList<Int>,
    private val onItemClick: (() -> Unit)? = null,
) : BaseAdapter() {

    override fun getCount(): Int = indexes.size

    override fun getItem(position: Int): Int = indexes[position]

    override fun getItemId(position: Int): Long = indexes[position].toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflateRow(parent)
        val title = view.findViewById<TextView>(R.id.itemTitle)
        val subtitle = view.findViewById<TextView>(R.id.itemSubtitle)
        title.text = context.getString(R.string.theme_demo_list_title_format, indexes[position])
        subtitle.setText(R.string.theme_demo_list_subtitle)
        view.setOnClickListener { onItemClick?.invoke() }
        ThemeManager.get().refresh(view)
        val resolver = ThemeManager.get().getResolver(view.context)
        if (resolver != null) {
            val card = resolver.getColor(R.color.skin_card_bg)
            view.background = ColorDrawable(card)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.backgroundTintList = null
            }
        }
        return view
    }

    fun appendMore(count: Int = 5) {
        val start = indexes.size
        indexes.addAll((start until start + count).toList())
        notifyDataSetChanged()
    }

    private fun inflateRow(parent: ViewGroup): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_theme_row, parent, false)
        val binding = ThemeManager.get().ensureAttach(view)
        if (binding is ViewResourceBinding) {
            binding.setBackground(ColorRef.of(R.color.skin_card_bg))
        }
        view.findViewById<View>(R.id.itemAccent)?.let { accent ->
            val accentBinding = ThemeManager.get().ensureAttach(accent)
            if (accentBinding is ViewResourceBinding) {
                accentBinding.setBackground(ColorRef.of(R.color.skin_accent))
            }
        }
        return view
    }
}
