package io.github.oppsgo.themeless.demo

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.androidx.ThemeAndroidX
import io.github.oppsgo.android.theme.androidx.resolver.AppCompatDayNightResourceResolver
import io.github.oppsgo.android.theme.binding.ViewResourceBinding
import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver
import io.github.oppsgo.android.theme.resource.ColorRef
import io.github.oppsgo.themeless.R

private const val DEMO_TAG = "ThemelessDemo"

/**
 * Demo 共享状态与换肤 apply。
 *
 * [demoThemeMode] / [dayNightMode] 仅表示「设置页已保存」的值（内存镜像 prefs）。
 * 演示页临时切肤不得改写它们，只对本 Activity 做 [ThemeManager.apply]。
 */
object ThemeDemoPage {

    /** 已保存的宿主 DefaultNightMode。 */
    var dayNightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        private set

    /** 已保存的皮肤；仅设置页 persist 时更新。 */
    var demoThemeMode: DemoThemeMode = DemoThemeMode.FOLLOW_SYSTEM
        private set

    fun init() {
        ThemeAndroidX.registerAvailable()
    }

    fun restorePersisted(context: Context) {
        dayNightMode = ThemePrefs.loadHostNightMode(context)
        demoThemeMode = ThemePrefs.loadThemeMode(context)
    }

    fun persist(context: Context) {
        ThemePrefs.save(context, demoThemeMode, dayNightMode)
    }

    fun setDefaultNightMode(mode: Int, context: Context? = null) {
        dayNightMode = mode
        context?.let { persist(it) }
    }

    fun getDefaultNightMode() = dayNightMode

    /** 仅在设置页保存皮肤时调用。 */
    fun setDemoThemeMode(mode: DemoThemeMode, context: Context) {
        demoThemeMode = mode
        persist(context)
    }

    fun resolveLocalNightMode(): Int = when (demoThemeMode) {
        DemoThemeMode.LIGHT, DemoThemeMode.CUSTOM, DemoThemeMode.DARK ->
            AppCompatDelegate.MODE_NIGHT_YES

        DemoThemeMode.FOLLOW_SYSTEM -> dayNightMode
    }

    fun resolveFollowNight(context: Context): Boolean = when (dayNightMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> DayNightResourceResolver.isSystemNight(context)
    }

    fun themeModeLabel(context: Context): String = when (demoThemeMode) {
        DemoThemeMode.FOLLOW_SYSTEM -> context.getString(R.string.theme_mode_follow)
        DemoThemeMode.LIGHT -> context.getString(R.string.theme_mode_light)
        DemoThemeMode.DARK -> context.getString(R.string.theme_mode_dark)
        DemoThemeMode.CUSTOM -> context.getString(R.string.theme_mode_custom)
    }
}

enum class DemoThemeMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
    CUSTOM,
}

internal fun Activity.bindBg(id: Int, color: Int) {
    val v = findViewById<View>(id) ?: return
    val ref = ColorRef.of(color)
    when (val binding = ThemeManager.get().ensureAttach(v)) {
        is ViewResourceBinding -> binding.setBackground(ref)
        else -> binding.bind(android.R.attr.background, ref)
    }
}

internal fun View.bindBg(id: Int, color: Int) {
    val v = findViewById<View>(id) ?: return
    val ref = ColorRef.of(color)
    when (val binding = ThemeManager.get().ensureAttach(v)) {
        is ViewResourceBinding -> binding.setBackground(ref)
        else -> binding.bind(android.R.attr.background, ref)
    }
}

internal fun Activity.ensureHomeBackgroundBindings() {
    bindBg(R.id.mainRoot, R.color.skin_page_bg)
    bindBg(R.id.mainTitleBar, R.color.skin_panel_bg)
    bindBg(R.id.homeRoot, R.color.skin_page_bg)
}

internal fun Activity.ensureSettingsBackgroundBindings() {
    bindBg(R.id.settingsPageRoot, R.color.skin_page_bg)
    bindBg(R.id.settingsTitleBar, R.color.skin_panel_bg)
    bindBg(R.id.settingsRoot, R.color.skin_page_bg)
}

internal fun Activity.ensureDemoShellBackgroundBindings() {
    bindBg(R.id.themeRoot, R.color.skin_page_bg)
    bindBg(R.id.themeTitleBar, R.color.skin_panel_bg)
    bindBg(R.id.demoPager, R.color.skin_page_bg)
}

private var restoringDemoTheme = false

internal fun Activity.restoreDemoTheme() {
    if (restoringDemoTheme) return
    restoringDemoTheme = true
    try {
        // 始终按已保存皮肤恢复，不沿用其它页的临时预览
        ThemeDemoPage.restorePersisted(this)
        when (ThemeDemoPage.demoThemeMode) {
            DemoThemeMode.FOLLOW_SYSTEM -> applyThemeResources(persist = false)
            DemoThemeMode.LIGHT -> applyThemeNight(dark = false, persist = false)
            DemoThemeMode.DARK -> applyThemeNight(dark = true, persist = false)
            DemoThemeMode.CUSTOM -> applyCustomTheme(persist = false)
        }
    } finally {
        restoringDemoTheme = false
    }
}

/** @param persist true 仅设置页：写入 prefs；false 只影响当前 Activity。 */
internal fun Activity.applyCustomTheme(persist: Boolean = false) {
    if (persist) {
        ThemeDemoPage.setDemoThemeMode(DemoThemeMode.CUSTOM, this)
    }
    if (syncActivityNightMode(dark = false)) return
    val base = DayNightResourceResolver.light(this)
    val mapped = MappedResourceResolver.skyBlue(base)
    val page = mapped.getColor(R.color.skin_page_bg)
    val panel = mapped.getColor(R.color.skin_panel_bg)
    val card = mapped.getColor(R.color.skin_card_bg)
    Log.i(DEMO_TAG, "apply custom page=0x${page.hex()} panel=0x${panel.hex()} persist=$persist")
    ThemeManager.get().apply(this, mapped)
    paintDemoSurfaces("custom", page, panel, card)
}

internal fun Activity.applyThemeNight(dark: Boolean, persist: Boolean = false) {
    if (persist) {
        ThemeDemoPage.setDemoThemeMode(
            if (dark) DemoThemeMode.DARK else DemoThemeMode.LIGHT,
            this,
        )
    }
    if (syncActivityNightMode(dark = dark)) return
    val resolver = if (this is AppCompatActivity) {
        AppCompatDayNightResourceResolver.of(this, dark)
    } else {
        DayNightResourceResolver.of(this, dark)
    }
    val page = resolver.getColor(R.color.skin_page_bg)
    val panel = resolver.getColor(R.color.skin_panel_bg)
    val card = resolver.getColor(R.color.skin_card_bg)
    Log.i(DEMO_TAG, "apply ${if (dark) "dark" else "light"} page=0x${page.hex()} persist=$persist")
    ThemeManager.get().apply(this, resolver)
    paintDemoSurfaces(if (dark) "dark" else "light", page, panel, card)
}

internal fun Activity.applyThemeResources(persist: Boolean = false) {
    if (persist) {
        ThemeDemoPage.setDemoThemeMode(DemoThemeMode.FOLLOW_SYSTEM, this)
    }
    if (syncActivityNightMode(dark = null)) return
    val night = ThemeDemoPage.resolveFollowNight(this)
    val resolver = if (this is AppCompatActivity) {
        AppCompatDayNightResourceResolver.of(this, night)
    } else {
        DayNightResourceResolver.of(this, night)
    }
    val page = resolver.getColor(R.color.skin_page_bg)
    val panel = resolver.getColor(R.color.skin_panel_bg)
    val card = resolver.getColor(R.color.skin_card_bg)
    Log.i(DEMO_TAG, "apply follow night=$night page=0x${page.hex()} persist=$persist")
    ThemeManager.get().apply(this, resolver)
    paintDemoSurfaces("follow", page, panel, card)
}

internal fun Activity.applyHostNightMode(mode: Int) {
    ThemeDemoPage.setDefaultNightMode(mode, this)
    if (AppCompatDelegate.getDefaultNightMode() != mode) {
        AppCompatDelegate.setDefaultNightMode(mode)
    }
    if (ThemeDemoPage.demoThemeMode == DemoThemeMode.FOLLOW_SYSTEM) {
        restoreDemoTheme()
    } else if (this is AppCompatActivity) {
        syncActivityNightMode(
            dark = when (ThemeDemoPage.demoThemeMode) {
                DemoThemeMode.DARK -> true
                DemoThemeMode.LIGHT, DemoThemeMode.CUSTOM -> false
                DemoThemeMode.FOLLOW_SYSTEM -> null
            },
        )
    }
}

private fun Activity.syncActivityNightMode(dark: Boolean?): Boolean {
    if (this !is AppCompatActivity) return false
    if (dark == null) {
        val mode = ThemeDemoPage.getDefaultNightMode()
        if (delegate.localNightMode != mode) {
            delegate.localNightMode = mode
            return true
        }
        return false
    }
    val mode = delegate.localNightMode
    if (mode == AppCompatDelegate.MODE_NIGHT_YES || mode != AppCompatDelegate.MODE_NIGHT_NO) {
        return false
    }
    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
    return true
}

private fun Activity.paintDemoSurfaces(label: String, page: Int, panel: Int, card: Int) {
    syncStatusBarIconAppearance()
    findViewById<TextView>(R.id.mainSubtitle)?.text =
        getString(R.string.main_shell_subtitle_format, ThemeDemoPage.themeModeLabel(this))
    findViewById<TextView>(R.id.themeTitle)?.text =
        getString(R.string.theme_demo_title_format, label)
}

internal fun Activity.refreshChromeTextColors() {
}

private fun Int.hex(): String = Integer.toHexString(this)

class ThemeLayerDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = requireArguments().getInt(ARG_MESSAGE)
        val content = requireActivity().layoutInflater.inflate(R.layout.dialog_theme_layer, null)
        content.findViewById<TextView>(R.id.themeDialogMessage).setText(message)
        content.findViewById<Button>(R.id.btnThemeDialogAgain).setOnClickListener {
            show(parentFragmentManager, R.string.theme_demo_dialog_nested_message)
        }
        return AlertDialog.Builder(requireContext())
            .setView(content)
            .create()
    }

    companion object {
        private const val ARG_MESSAGE = "message"

        fun show(manager: FragmentManager, @StringRes message: Int) {
            ThemeLayerDialogFragment().apply {
                arguments = Bundle().apply { putInt(ARG_MESSAGE, message) }
            }.show(manager, "theme-layer-${System.nanoTime()}")
        }
    }
}
