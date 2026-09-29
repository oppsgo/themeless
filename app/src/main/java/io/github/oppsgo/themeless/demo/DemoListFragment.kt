package io.github.oppsgo.themeless.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import io.github.oppsgo.themeless.R

class DemoListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_demo_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.bindBg(R.id.demoListRoot, R.color.skin_panel_bg)
        view.bindBg(R.id.demoListSubTabs, R.color.skin_panel_bg)

        val subBar = view.findViewById<ViewGroup>(R.id.demoListSubTabs)
        val subTabs = listOf(
            view.findViewById<TextView>(R.id.demoListSubRv),
            view.findViewById<TextView>(R.id.demoListSubLv),
            view.findViewById<TextView>(R.id.demoListSubGv),
        )
        val pager = view.findViewById<ViewPager2>(R.id.demoListPager)
        pager.offscreenPageLimit = 2
        pager.adapter = DemoListPagesAdapter()

        fun selectSub(index: Int) {
            subBar.tag = index
            requireActivity().refreshDemoTabColors()
        }
        subTabs.forEachIndexed { index, tab ->
            tab.setOnClickListener { pager.setCurrentItem(index, true) }
        }
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                selectSub(position)
            }
        })
        selectSub(pager.currentItem)
    }
}

private class DemoListPagesAdapter : RecyclerView.Adapter<DemoListPagesAdapter.PageHolder>() {

    override fun getItemCount(): Int = 3

    override fun getItemViewType(position: Int): Int = position

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageHolder {
        val inflater = LayoutInflater.from(parent.context)
        val page = when (viewType) {
            0 -> inflater.inflate(R.layout.page_demo_list_rv, parent, false).also { root ->
                root.bindBg(R.id.themeRecycler, R.color.skin_panel_bg)
                val recycler = root as RecyclerView
                lateinit var adapter: ThemeRowAdapter
                adapter = ThemeRowAdapter((0..11).toMutableList()) { adapter.appendMore() }
                recycler.layoutManager = LinearLayoutManager(parent.context)
                recycler.adapter = adapter
            }
            1 -> inflater.inflate(R.layout.page_demo_list_lv, parent, false).also { root ->
                root.bindBg(R.id.themeListView, R.color.skin_panel_bg)
                val listView = root as ListView
                lateinit var adapter: ThemeAbsListAdapter
                adapter = ThemeAbsListAdapter(parent.context, (0..11).toMutableList()) {
                    adapter.appendMore()
                }
                listView.adapter = adapter
            }
            else -> inflater.inflate(R.layout.page_demo_list_gv, parent, false).also { root ->
                root.bindBg(R.id.themeGridView, R.color.skin_panel_bg)
                val gridView = root as GridView
                lateinit var adapter: ThemeAbsListAdapter
                adapter = ThemeAbsListAdapter(parent.context, (0..11).toMutableList()) {
                    adapter.appendMore()
                }
                gridView.adapter = adapter
            }
        }
        page.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        return PageHolder(page)
    }

    override fun onBindViewHolder(holder: PageHolder, position: Int) = Unit

    class PageHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
