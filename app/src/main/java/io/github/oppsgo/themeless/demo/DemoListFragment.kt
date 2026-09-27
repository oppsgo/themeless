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
import io.github.oppsgo.themeless.R

class DemoListFragment : Fragment() {

    private var selectedSub = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_demo_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.bindBg(R.id.demoListRoot, R.color.skin_panel_bg)
        view.bindBg(R.id.demoListSubTabs, R.color.skin_panel_bg)
        view.bindBg(R.id.themeRecycler, R.color.skin_panel_bg)
        view.bindBg(R.id.themeListView, R.color.skin_panel_bg)
        view.bindBg(R.id.themeGridView, R.color.skin_panel_bg)

        val recycler = view.findViewById<RecyclerView>(R.id.themeRecycler)
        val listView = view.findViewById<ListView>(R.id.themeListView)
        val gridView = view.findViewById<GridView>(R.id.themeGridView)
        val panels = listOf(recycler, listView, gridView)

        lateinit var rvAdapter: ThemeRowAdapter
        rvAdapter = ThemeRowAdapter((0..11).toMutableList()) {
            rvAdapter.appendMore()
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = rvAdapter

        lateinit var lvAdapter: ThemeAbsListAdapter
        lvAdapter = ThemeAbsListAdapter(requireContext(), (0..11).toMutableList()) {
            lvAdapter.appendMore()
        }
        listView.adapter = lvAdapter

        lateinit var gvAdapter: ThemeAbsListAdapter
        gvAdapter = ThemeAbsListAdapter(requireContext(), (0..11).toMutableList()) {
            gvAdapter.appendMore()
        }
        gridView.adapter = gvAdapter

        val subTabs = listOf(
            view.findViewById<TextView>(R.id.demoListSubRv),
            view.findViewById<TextView>(R.id.demoListSubLv),
            view.findViewById<TextView>(R.id.demoListSubGv),
        )
        val subBar = view.findViewById<ViewGroup>(R.id.demoListSubTabs)
        fun showSub(index: Int) {
            selectedSub = index
            subBar.tag = index
            panels.forEachIndexed { i, panel ->
                panel.visibility = if (i == index) View.VISIBLE else View.GONE
            }
            requireActivity().refreshDemoTabColors()
        }
        subTabs.forEachIndexed { index, tab ->
            tab.setOnClickListener { showSub(index) }
        }
        showSub(0)
    }
}
