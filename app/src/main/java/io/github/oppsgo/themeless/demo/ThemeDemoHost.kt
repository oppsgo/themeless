package io.github.oppsgo.themeless.demo

import android.app.Activity
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.R

/**
 * 二级演示页：标题栏切肤 + 按功能划分的 Tab（控件 / 列表 / 其它 / 浮层）。
 */
internal fun FragmentActivity.setupThemeDemoHost(@StringRes subtitle: Int) {
    setContentView(R.layout.activity_theme_demo)
    setupImmersiveTitleBar()
    ThemeManager.get().setRefreshOnInflate(this, true)
    ensureDemoShellBackgroundBindings()
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

    val tabBar = findViewById<ViewGroup>(R.id.demoTabBar)
    val tabs = listOf(
        findViewById<TextView>(R.id.demoTabWidgets),
        findViewById<TextView>(R.id.demoTabList),
        findViewById<TextView>(R.id.demoTabExtras),
        findViewById<TextView>(R.id.demoTabOverlays),
    )
    tabs.forEachIndexed { index, tab ->
        tab.setOnClickListener { pager.setCurrentItem(index, true) }
    }
    pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            tabBar.tag = position
            refreshDemoTabColors()
        }
    })
    tabBar.tag = pager.currentItem
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

/** 兼容旧 Lab Activity 入口名。 */
internal fun Activity.showThemeDemo(@StringRes subtitle: Int) {
    (this as FragmentActivity).setupThemeDemoHost(subtitle)
}
