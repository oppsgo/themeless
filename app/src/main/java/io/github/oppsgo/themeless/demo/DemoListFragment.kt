package io.github.oppsgo.themeless.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        view.bindBg(R.id.themeRecycler, R.color.skin_panel_bg)

        val recycler = view.findViewById<RecyclerView>(R.id.themeRecycler)
        lateinit var adapter: ThemeRowAdapter
        adapter = ThemeRowAdapter((0..11).toMutableList()) {
            adapter.appendMore()
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter
    }
}
