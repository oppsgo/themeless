package io.github.oppsgo.themeless.demo

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.ktx.installTheme
import io.github.oppsgo.themeless.R

/** ViewPager2 多tab：上方多 Tab + 下方 Banner peek。 */
class ViewPager2StressActivity : FragmentActivity() {

    init {
        ThemeDemoPage.init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager2_stress)
        setupImmersiveTitleBar()
        ThemeManager.get().setRefreshOnInflate(this, true)
        bindBg(R.id.vp2StressRoot, R.color.skin_page_bg)
        bindBg(R.id.vp2StressTitleBar, R.color.skin_panel_bg)
        bindBg(R.id.vp2StressTabScroll, R.color.skin_panel_bg)
        bindBg(R.id.vp2StressBody, R.color.skin_page_bg)
        bindBg(R.id.vp2StressPager, R.color.skin_page_bg)

        val pager = findViewById<ViewPager2>(R.id.vp2StressPager)
        val banner = findViewById<ViewPager2>(R.id.vp2StressBanner)
        val tabBar = findViewById<LinearLayout>(R.id.vp2StressTabBar)
        pager.adapter = ViewPager2StressAdapter(this)

        banner.setupBannerPeek()
        banner.adapter = ViewPager2BannerAdapter()
        banner.setCurrentItem(1, false)

        val density = resources.displayMetrics.density
        val tabs = (0 until TAB_COUNT).map { index ->
            TextView(this).apply {
                text = getString(R.string.vp2_stress_tab_format, index + 1)
                textSize = 14f
                gravity = Gravity.CENTER
                val pad = (16 * density).toInt()
                setPadding(pad, pad, pad, pad)
                setOnClickListener { pager.setCurrentItem(index, true) }
                tabBar.addView(
                    this,
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    ),
                )
            }
        }

        fun paintTabs(selected: Int) {
            val resolver = ThemeManager.get().getResolver(this) ?: return
            val accent = resolver.getColor(R.color.skin_accent)
            val secondary = resolver.getColor(R.color.skin_text_secondary)
            tabs.forEachIndexed { index, tab ->
                tab.setTextColor(if (index == selected) accent else secondary)
            }
        }

        findViewById<Button>(R.id.btnVp2Light).setOnClickListener {
            applyThemeNight(dark = false)
            paintTabs(pager.currentItem)
        }
        findViewById<Button>(R.id.btnVp2Dark).setOnClickListener {
            applyThemeNight(dark = true)
            paintTabs(pager.currentItem)
        }
        findViewById<Button>(R.id.btnVp2Custom).setOnClickListener {
            applyCustomTheme()
            paintTabs(pager.currentItem)
        }

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                paintTabs(position)
            }
        })
        paintTabs(pager.currentItem)
        restoreDemoTheme()
        paintTabs(pager.currentItem)
    }

    companion object {
        const val TAB_COUNT = 20
        const val BANNER_COUNT = 8
    }
}

private class ViewPager2StressAdapter(
    activity: FragmentActivity,
) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = ViewPager2StressActivity.TAB_COUNT

    override fun createFragment(position: Int): Fragment =
        ViewPager2StressPageFragment.newInstance(position)
}

private class ViewPager2BannerAdapter : RecyclerView.Adapter<ViewPager2BannerAdapter.Holder>() {

    override fun getItemCount(): Int = ViewPager2StressActivity.BANNER_COUNT

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_viewpager_banner_page, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(position)
    }

    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val label: TextView = itemView.findViewById(R.id.vpBannerPageLabel)

        fun bind(position: Int) {
            itemView.bindBg(R.id.vpBannerPageRoot, R.color.skin_panel_bg)
            label.text = itemView.context.getString(R.string.vp_banner_page_format, position + 1)
        }
    }
}

class ViewPager2StressPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_viewpager2_stress_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.bindBg(R.id.vp2StressPageRoot, R.color.skin_page_bg)
        val index = requireArguments().getInt(ARG_INDEX)
        view.findViewById<TextView>(R.id.vp2StressPageLabel).text =
            getString(R.string.vp2_stress_page_format, index + 1)
    }

    companion object {
        private const val ARG_INDEX = "index"

        fun newInstance(index: Int): ViewPager2StressPageFragment =
            ViewPager2StressPageFragment().apply {
                arguments = Bundle().apply { putInt(ARG_INDEX, index) }
            }
    }
}
