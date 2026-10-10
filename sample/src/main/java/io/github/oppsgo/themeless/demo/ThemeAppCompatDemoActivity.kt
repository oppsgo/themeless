package io.github.oppsgo.themeless.demo

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.R

/**
 * AppCompat：install 时传入 Delegate。二级页按功能拆成 Tab。
 */
class ThemeAppCompatDemoActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        delegate.localNightMode = ThemeDemoPage.resolveLocalNightMode()
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.get().install(this, delegate as? LayoutInflater.Factory2)
        super.onCreate(savedInstanceState)
        setupThemeDemoHost(R.string.theme_demo_appcompat_subtitle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (ThemeDemoPage.demoThemeMode == DemoThemeMode.FOLLOW_SYSTEM) {
            restoreDemoTheme()
        }
    }
}
