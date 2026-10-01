package io.github.oppsgo.themeless.demo

import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.R

/**
 * 二级演示页：标题栏切肤 + TabLayout（控件 / 列表 / 其它 / 浮层）+ ViewPager2。
 */
internal fun FragmentActivity.setupThemeDemoHost(@StringRes subtitle: Int) {
    setContentView(R.layout.activity_theme_demo)
    setupImmersiveTitleBar()
    ThemeManager.get().setRefreshOnInflate(this, true)
    findViewById<TextView>(R.id.themeSubtitle).setText(subtitle)

    findViewById<Button>(R.id.btnLight).setOnClickListener {
        applyThemeNight(dark = false)
    }
    findViewById<Button>(R.id.btnDark).setOnClickListener {
        applyThemeNight(dark = true)
    }
    findViewById<Button>(R.id.btnCustom).setOnClickListener {
        applyCustomTheme()
    }

    val pager = findViewById<ViewPager2>(R.id.demoPager)
    pager.adapter = ThemeDemoPagerAdapter(this)
    pager.offscreenPageLimit = 3

    val tabLayout = findViewById<TabLayout>(R.id.demoTabBar)
    val titles = intArrayOf(
        R.string.demo_tab_widgets,
        R.string.demo_tab_list,
        R.string.demo_tab_extras,
        R.string.demo_tab_overlays,
    )
    TabLayoutMediator(tabLayout, pager) { tab, position ->
        tab.setText(titles[position])
    }.attach()

    restoreDemoTheme()
}

private class ThemeDemoPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> DemoWidgetsFragment()
        1 -> DemoListFragment()
        2 -> DemoExtrasFragment()
        else -> DemoOverlaysFragment()
    }
}
