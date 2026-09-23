package io.github.oppsgo.themeless.demo

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.R

/**
 * AppCompat 只多传一个 Factory2：AppCompatDelegate 的实现类本身就是 Factory2。
 *
 * 手动亮/自定义锁夜间宿主，避免 Light 主题触发 Force Dark。肤色只来自 ResourceResolver。
 */
class ThemeAppCompatDemoActivity : AppCompatActivity() {
    init {
        ThemeDemoPage.init()
    }

    override fun attachBaseContext(newBase: Context) {
        delegate.localNightMode = ThemeDemoPage.resolveLocalNightMode()
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.get().install(this, delegate as? LayoutInflater.Factory2)
        super.onCreate(savedInstanceState)
        showThemeDemo(R.string.theme_demo_appcompat_subtitle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (ThemeDemoPage.demoThemeMode == DemoThemeMode.FOLLOW_SYSTEM) {
            restoreDemoTheme()
        }
    }
}
