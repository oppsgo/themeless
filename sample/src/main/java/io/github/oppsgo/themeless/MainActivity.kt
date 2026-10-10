package io.github.oppsgo.themeless

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.demo.DemoThemeMode
import io.github.oppsgo.themeless.demo.ThemeAppCompatDemoActivity
import io.github.oppsgo.themeless.demo.ThemeDemoActivity
import io.github.oppsgo.themeless.demo.ThemeDemoPage
import io.github.oppsgo.themeless.demo.ViewPager2StressActivity
import io.github.oppsgo.themeless.demo.ViewPagerStressActivity
import io.github.oppsgo.themeless.demo.restoreDemoTheme
import io.github.oppsgo.themeless.demo.setupImmersiveTitleBar

/**
 * 首页：二级演示入口；右上角设置图标。
 * 皮肤经 SharedPreferences 持久化，冷启动由 [ThemelessApp] 恢复。
 * 设置页改肤后通过 Activity Result 通知本页补刷（apply 按 Activity 隔离）。
 */
class MainActivity : AppCompatActivity() {

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            restoreDemoTheme()
        }
    }

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
        setContentView(R.layout.activity_main)
        setupImmersiveTitleBar()
        ThemeManager.get().setRefreshOnInflate(this, true)

        findViewById<ImageButton>(R.id.btnOpenSettings).setOnClickListener {
            settingsLauncher.launch(Intent(this, SettingsActivity::class.java))
        }
        findViewById<Button>(R.id.btnOpenFragmentDemo).setOnClickListener {
            startActivity(Intent(this, ThemeDemoActivity::class.java))
        }
        findViewById<Button>(R.id.btnOpenAppCompatDemo).setOnClickListener {
            startActivity(Intent(this, ThemeAppCompatDemoActivity::class.java))
        }
        findViewById<Button>(R.id.btnOpenVpStress).setOnClickListener {
            startActivity(Intent(this, ViewPagerStressActivity::class.java))
        }
        findViewById<Button>(R.id.btnOpenVp2Stress).setOnClickListener {
            startActivity(Intent(this, ViewPager2StressActivity::class.java))
        }

        restoreDemoTheme()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (ThemeDemoPage.demoThemeMode == DemoThemeMode.FOLLOW_SYSTEM) {
            restoreDemoTheme()
        }
    }
}
