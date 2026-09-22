package io.github.oppsgo.themeless.demo

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.androidx.binding.AppCompatImageViewResourceBinding
import io.github.oppsgo.android.theme.androidx.binding.AppCompatTextViewResourceBinding
import io.github.oppsgo.themeless.R

/**
 * AppCompat 只多传一个 Factory2：AppCompatDelegate 的实现类本身就是 Factory2。
 */
class ThemeAppCompatDemoActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val viewFactory = delegate as? LayoutInflater.Factory2
        ThemeManager.get().install(this, viewFactory)
        AppCompatImageViewResourceBinding.register()
        AppCompatTextViewResourceBinding.register()
        super.onCreate(savedInstanceState)
        showThemeDemo(R.string.theme_demo_appcompat_subtitle)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // 仅「跟随系统」时重刷；手动亮/暗/自定义不要被系统 uiMode 冲掉。
        // localNightMode 在 apply* 里赋值，这里空读没用。
        reapplyDemoThemeIfFollowingSystem()
    }
}
