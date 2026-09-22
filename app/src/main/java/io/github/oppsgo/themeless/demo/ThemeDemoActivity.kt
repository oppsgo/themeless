package io.github.oppsgo.themeless.demo

import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.R

/**
 * 非 AppCompat：不传 Factory2，View 走默认创建。
 */
class ThemeDemoActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.get().install(this)
        super.onCreate(savedInstanceState)
        showThemeDemo(R.string.theme_demo_subtitle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        reapplyDemoThemeIfFollowingSystem()
    }
}
