package io.github.oppsgo.themeless

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatRadioButton
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.ktx.installTheme
import io.github.oppsgo.themeless.demo.DemoThemeMode
import io.github.oppsgo.themeless.demo.ThemeDemoPage
import io.github.oppsgo.themeless.demo.applyCustomTheme
import io.github.oppsgo.themeless.demo.applyHostNightMode
import io.github.oppsgo.themeless.demo.applyThemeNight
import io.github.oppsgo.themeless.demo.applyThemeResources
import io.github.oppsgo.themeless.demo.ensureSettingsBackgroundBindings
import io.github.oppsgo.themeless.demo.refreshChromeTextColors
import io.github.oppsgo.themeless.demo.restoreDemoTheme
import io.github.oppsgo.themeless.demo.setupImmersiveTitleBar

/** 皮肤与宿主日夜设置；变更会写入 SharedPreferences。 */
class SettingsActivity : AppCompatActivity() {

    private var bindingUi = false

    override fun attachBaseContext(newBase: Context) {
        delegate.localNightMode = ThemeDemoPage.resolveLocalNightMode()
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installTheme(delegate as? LayoutInflater.Factory2)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setupImmersiveTitleBar()
        ThemeManager.get().setRefreshOnInflate(this, true)
        ensureSettingsBackgroundBindings()

        restoreDemoTheme()
        syncFromState()

        findViewById<RadioGroup>(R.id.settingsSkinGroup)
            .setOnCheckedChangeListener { _, checkedId ->
                if (bindingUi) return@setOnCheckedChangeListener
                when (checkedId) {
                    R.id.settingsSkinFollow -> applyThemeResources(persist = true)
                    R.id.settingsSkinLight -> applyThemeNight(dark = false, persist = true)
                    R.id.settingsSkinDark -> applyThemeNight(dark = true, persist = true)
                    R.id.settingsSkinCustom -> applyCustomTheme(persist = true)
                }
            }

        findViewById<RadioGroup>(R.id.settingsHostGroup)
            .setOnCheckedChangeListener { _, checkedId ->
                if (bindingUi) return@setOnCheckedChangeListener
                val mode = when (checkedId) {
                    R.id.settingsHostLight -> AppCompatDelegate.MODE_NIGHT_NO
                    R.id.settingsHostDark -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                applyHostNightMode(mode)
                syncFromState()
            }
    }

    override fun onResume() {
        super.onResume()
        syncFromState()
        refreshChromeTextColors()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (ThemeDemoPage.demoThemeMode == DemoThemeMode.FOLLOW_SYSTEM) {
            restoreDemoTheme()
        }
    }

    private fun syncFromState() {
        bindingUi = true
        try {
            val skinId = when (ThemeDemoPage.demoThemeMode) {
                DemoThemeMode.FOLLOW_SYSTEM -> R.id.settingsSkinFollow
                DemoThemeMode.LIGHT -> R.id.settingsSkinLight
                DemoThemeMode.DARK -> R.id.settingsSkinDark
                DemoThemeMode.CUSTOM -> R.id.settingsSkinCustom
            }
            findViewById<AppCompatRadioButton>(skinId).isChecked = true

            val hostId = when (ThemeDemoPage.getDefaultNightMode()) {
                AppCompatDelegate.MODE_NIGHT_NO -> R.id.settingsHostLight
                AppCompatDelegate.MODE_NIGHT_YES -> R.id.settingsHostDark
                else -> R.id.settingsHostFollow
            }
            findViewById<AppCompatRadioButton>(hostId).isChecked = true
        } finally {
            bindingUi = false
        }
    }
}
