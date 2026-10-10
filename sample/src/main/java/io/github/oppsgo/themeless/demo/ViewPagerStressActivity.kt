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
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.R

/** ViewPager 多tab：上方多 Tab + 下方 Banner peek。 */
class ViewPagerStressActivity : FragmentActivity() {

    init {
        ThemeDemoPage.init()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.get().install(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_viewpager_stress)
        setupImmersiveTitleBar()
        ThemeManager.get().setRefreshOnInflate(this, true)
        bindBg(R.id.vpStressRoot, R.color.skin_page_bg)
        bindBg(R.id.vpStressTitleBar, R.color.skin_panel_bg)
        bindBg(R.id.vpStressTabScroll, R.color.skin_panel_bg)
        bindBg(R.id.vpStressBody, R.color.skin_page_bg)
        bindBg(R.id.vpStressPager, R.color.skin_page_bg)

        val pager = findViewById<ViewPager>(R.id.vpStressPager)
        val banner = findViewById<ViewPager>(R.id.vpStressBanner)
        val tabBar = findViewById<LinearLayout>(R.id.vpStressTabBar)
        @Suppress("DEPRECATION")
        pager.adapter = ViewPagerStressAdapter(this)

        banner.setupBannerPeek()
        banner.adapter = ViewPagerBannerAdapter()
        banner.setCurrentItem(1, false)

        val density = resources.displayMetrics.density
        val tabs = (0 until TAB_COUNT).map { index ->
            TextView(this).apply {
                text = getString(R.string.vp_stress_tab_format, index + 1)
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

        findViewById<Button>(R.id.btnVpLight).setOnClickListener {
            applyThemeNight(dark = false)
            paintTabs(pager.currentItem)
        }
        findViewById<Button>(R.id.btnVpDark).setOnClickListener {
            applyThemeNight(dark = true)
            paintTabs(pager.currentItem)
        }
        findViewById<Button>(R.id.btnVpCustom).setOnClickListener {
            applyCustomTheme()
            paintTabs(pager.currentItem)
        }

        pager.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
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

@Suppress("DEPRECATION")
private class ViewPagerStressAdapter(
    activity: FragmentActivity,
) : FragmentStatePagerAdapter(activity.supportFragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount(): Int = ViewPagerStressActivity.TAB_COUNT

    override fun getItem(position: Int): Fragment =
        ViewPagerStressPageFragment.newInstance(position)
}

private class ViewPagerBannerAdapter : PagerAdapter() {
    override fun getCount(): Int = ViewPagerStressActivity.BANNER_COUNT

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val page = LayoutInflater.from(container.context)
            .inflate(R.layout.item_viewpager_banner_page, container, false)
        page.bindBg(R.id.vpBannerPageRoot, R.color.skin_panel_bg)
        page.findViewById<TextView>(R.id.vpBannerPageLabel).text =
            container.context.getString(R.string.vp_banner_page_format, position + 1)
        container.addView(page)
        return page
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        container.removeView(obj as View)
    }
}

class ViewPagerStressPageFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = inflater.inflate(R.layout.fragment_viewpager_stress_page, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.bindBg(R.id.vpStressPageRoot, R.color.skin_page_bg)
        val index = requireArguments().getInt(ARG_INDEX)
        view.findViewById<TextView>(R.id.vpStressPageLabel).text =
            getString(R.string.vp_stress_page_format, index + 1)
    }

    companion object {
        private const val ARG_INDEX = "index"

        fun newInstance(index: Int): ViewPagerStressPageFragment =
            ViewPagerStressPageFragment().apply {
                arguments = Bundle().apply { putInt(ARG_INDEX, index) }
            }
    }
}
