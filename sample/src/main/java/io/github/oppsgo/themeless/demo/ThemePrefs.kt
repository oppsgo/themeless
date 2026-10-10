package io.github.oppsgo.themeless.demo

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

/**
 * Demo 皮肤与宿主日夜的本地持久化（SharedPreferences）。
 */
object ThemePrefs {
    private const val PREFS = "themeless_demo"
    private const val KEY_THEME = "demo_theme_mode"
    private const val KEY_HOST_NIGHT = "host_night_mode"

    fun loadThemeMode(context: Context): DemoThemeMode {
        val raw = prefs(context).getString(KEY_THEME, DemoThemeMode.FOLLOW_SYSTEM.name)
        return runCatching { DemoThemeMode.valueOf(raw!!) }.getOrDefault(DemoThemeMode.FOLLOW_SYSTEM)
    }

    fun loadHostNightMode(context: Context): Int {
        return prefs(context).getInt(KEY_HOST_NIGHT, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun save(context: Context, themeMode: DemoThemeMode, hostNightMode: Int) {
        prefs(context).edit()
            .putString(KEY_THEME, themeMode.name)
            .putInt(KEY_HOST_NIGHT, hostNightMode)
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
