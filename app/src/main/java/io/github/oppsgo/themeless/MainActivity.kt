package io.github.oppsgo.themeless

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver
import io.github.oppsgo.themeless.demo.ThemeAppCompatDemoActivity
import io.github.oppsgo.themeless.demo.ThemeDemoActivity

class MainActivity : AppCompatActivity() {

    private data class EntryItem(
        val title: String,
        val subtitle: String,
        val onClick: () -> Unit,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnNightFollow).setOnClickListener {
            setGlobalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        findViewById<Button>(R.id.btnNightLight).setOnClickListener {
            setGlobalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        findViewById<Button>(R.id.btnNightDark).setOnClickListener {
            setGlobalNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        refreshNightModeHint()

        val list = findViewById<LinearLayout>(R.id.entryList)
        listOf(
            EntryItem(
                title = getString(R.string.main_entry_theme_title),
                subtitle = getString(R.string.main_entry_theme_subtitle),
                onClick = {
                    startActivity(Intent(this, ThemeDemoActivity::class.java))
                },
            ),
            EntryItem(
                title = getString(R.string.main_entry_theme_appcompat_title),
                subtitle = getString(R.string.main_entry_theme_appcompat_subtitle),
                onClick = {
                    startActivity(Intent(this, ThemeAppCompatDemoActivity::class.java))
                },
            ),
        ).forEach { list.addView(createEntryRow(it)) }
    }

    override fun onResume() {
        super.onResume()
        refreshNightModeHint()
    }

    private fun setGlobalNightMode(mode: Int) {
        if (AppCompatDelegate.getDefaultNightMode() == mode) return
        AppCompatDelegate.setDefaultNightMode(mode)
        // setDefaultNightMode 可能 recreate；若没重建也刷新说明。
        refreshNightModeHint()
    }

    private fun refreshNightModeHint() {
        val hint = findViewById<TextView>(R.id.mainNightModeHint)
        val mode = AppCompatDelegate.getDefaultNightMode()
        val systemNight = DayNightResourceResolver.isSystemNight(this)
        val activityNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        val base = when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> getString(R.string.main_night_hint_light)
            AppCompatDelegate.MODE_NIGHT_YES -> getString(R.string.main_night_hint_dark)
            else -> getString(R.string.main_night_hint_follow)
        }
        hint.text = buildString {
            append(base)
            append("\n\n")
            append("此刻：系统(Application)=")
            append(if (systemNight) "暗" else "亮")
            append("，本页 Activity=")
            append(if (activityNight) "暗" else "亮")
            append("，DefaultNightMode=")
            append(
                when (mode) {
                    AppCompatDelegate.MODE_NIGHT_NO -> "NO(亮)"
                    AppCompatDelegate.MODE_NIGHT_YES -> "YES(暗)"
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM -> "FOLLOW"
                    else -> mode.toString()
                },
            )
        }
    }

    private fun createEntryRow(item: EntryItem): TextView {
        val padH = (20 * resources.displayMetrics.density).toInt()
        val padV = (16 * resources.displayMetrics.density).toInt()
        return TextView(this).apply {
            text = "${item.title}\n${item.subtitle}"
            textSize = 16f
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
            setBackgroundColor(0xFF243044.toInt())
            setPadding(padH, padV, padH, padV)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            lp.bottomMargin = (12 * resources.displayMetrics.density).toInt()
            layoutParams = lp
            setOnClickListener { item.onClick() }
        }
    }
}
