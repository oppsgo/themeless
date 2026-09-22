package io.github.oppsgo.themeless.demo

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.themeless.R

/**
 * 沉浸式标题：内容顶到状态栏下，[themeTitleBar] 自己垫 statusBars；
 * 左右内容区 [themeBody] 垫 navigationBars。
 * 状态栏图标深浅按标题栏背景亮度切换（换肤后也要再调一次）。
 */
internal fun Activity.setupImmersiveTitleBar() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        @Suppress("DEPRECATION")
        window.statusBarColor = Color.TRANSPARENT
        @Suppress("DEPRECATION")
        window.navigationBarColor = Color.TRANSPARENT
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    val root = findViewById<View>(R.id.themeRoot)
    val titleBar = findViewById<View>(R.id.themeTitleBar)
    val body = findViewById<View>(R.id.themeBody)

    val titlePad = intArrayOf(
        titleBar.paddingLeft, titleBar.paddingTop,
        titleBar.paddingRight, titleBar.paddingBottom,
    )
    val bodyPad = intArrayOf(
        body.paddingLeft, body.paddingTop,
        body.paddingRight, body.paddingBottom,
    )

    ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
        val status = insets.getInsets(WindowInsetsCompat.Type.statusBars())
        val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        titleBar.setPadding(
            titlePad[0],
            titlePad[1] + status.top,
            titlePad[2],
            titlePad[3],
        )
        body.setPadding(
            bodyPad[0],
            bodyPad[1],
            bodyPad[2],
            bodyPad[3] + nav.bottom,
        )
        insets
    }
    ViewCompat.requestApplyInsets(root)
    syncStatusBarIconAppearance()
}

/** 按当前主题下标题栏背景色，决定状态栏图标用深色还是浅色。 */
internal fun Activity.syncStatusBarIconAppearance() {
    val resolver = ThemeManager.get().getResolver(this) ?: return
    val bg = resolver.getColor(R.color.skin_panel_bg)
    val lightBackground = ColorUtils.calculateLuminance(bg) > 0.5
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = lightBackground
}
