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
 * 沉浸式：标题栏垫 statusBars；底部内容区垫 navigationBars。
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

    val root = findViewById<View>(R.id.mainRoot)
        ?: findViewById(R.id.settingsPageRoot)
        ?: findViewById(R.id.themeRoot)
        ?: findViewById(R.id.vp2StressRoot)
        ?: findViewById(R.id.vpStressRoot)
        ?: return
    val titleBar = findViewById<View>(R.id.mainTitleBar)
        ?: findViewById(R.id.settingsTitleBar)
        ?: findViewById(R.id.themeTitleBar)
        ?: findViewById(R.id.vp2StressTitleBar)
        ?: findViewById(R.id.vpStressTitleBar)
        ?: return
    val content = findViewById<View>(R.id.homeRoot)
        ?: findViewById(R.id.settingsRoot)
        ?: findViewById(R.id.demoPager)
        ?: findViewById(R.id.vp2StressBody)
        ?: findViewById(R.id.vpStressBody)
        ?: return

    // 记录初始 padding，避免多次 insets 叠加
    val titlePad = intArrayOf(
        titleBar.paddingLeft, titleBar.paddingTop,
        titleBar.paddingRight, titleBar.paddingBottom,
    )
    val contentPad = intArrayOf(
        content.paddingLeft, content.paddingTop,
        content.paddingRight, content.paddingBottom,
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
        // ScrollView：底/左右垫导航栏；顶部由标题栏吃 statusBars
        content.setPadding(
            contentPad[0] + nav.left,
            contentPad[1],
            contentPad[2] + nav.right,
            contentPad[3] + nav.bottom,
        )
        insets
    }
    ViewCompat.requestApplyInsets(root)
    syncStatusBarIconAppearance()
}

internal fun Activity.syncStatusBarIconAppearance() {
    val resolver = ThemeManager.get().getResolver(this) ?: return
    val bg = resolver.getColor(R.color.skin_panel_bg)
    val lightBackground = ColorUtils.calculateLuminance(bg) > 0.5
    WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = lightBackground
}
