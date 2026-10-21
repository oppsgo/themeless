package io.github.oppsgo.themeless.demo

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.androidx.resolver.AppCompatDayNightResourceResolver
import io.github.oppsgo.android.theme.webview.WebViewDayNightCompat
import io.github.oppsgo.themeless.R

/**
 * WebView 暗黑演示：分段按钮亮色 / 暗色 / 跟随系统。
 * <p>
 * 切换不 reload：改 {@code localNightMode}（主题驱动下决定 {@code isLightTheme} /
 * {@code prefers-color-scheme}），再 {@link WebViewDayNightCompat#apply}
 * （非主题驱动写 {@code setForceDark}；主题驱动只配算法加深，不是明暗开关）。
 */
class WebViewDayNightActivity : AppCompatActivity() {

    private lateinit var webContainer: FrameLayout
    private lateinit var webView: WebView
    private lateinit var toggleGroup: MaterialButtonToggleGroup

    /** 本页临时预览模式，不写 prefs。 */
    private var previewMode = Mode.FOLLOW
    private var bindingUi = false

    /** `<input type="file">` 的回调；取消或换起新选择器时必须 onReceiveValue(null)。 */
    private var filePathCallback: ValueCallback<Array<Uri>>? = null

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(uris)
        filePathCallback = null
    }

    private enum class Mode { LIGHT, DARK, FOLLOW }

    override fun attachBaseContext(newBase: Context) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        super.attachBaseContext(newBase)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.get().install(this, delegate as? LayoutInflater.Factory2)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview_day_night)
        setupImmersiveTitleBar()
        ThemeManager.get().setRefreshOnInflate(this, true)

        webContainer = findViewById(R.id.webDayNightBody)
        toggleGroup = findViewById(R.id.webDayNightToggle)

        previewMode = savedInstanceState?.getString(STATE_MODE)
            ?.let { runCatching { Mode.valueOf(it) }.getOrNull() }
            ?: modeFromPersistedSkin()

        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (bindingUi || !isChecked) return@addOnButtonCheckedListener
            val mode = when (checkedId) {
                R.id.webDayNightLight -> Mode.LIGHT
                R.id.webDayNightDark -> Mode.DARK
                else -> Mode.FOLLOW
            }
            if (mode == previewMode) return@addOnButtonCheckedListener
            applyPreview(mode)
        }

        applyPreview(previewMode)
        installWebView(savedInstanceState?.getBundle(STATE_WEB_VIEW))

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (::webView.isInitialized && webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            },
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_MODE, previewMode.name)
        if (::webView.isInitialized) {
            val webState = Bundle()
            webView.saveState(webState)
            outState.putBundle(STATE_WEB_VIEW, webState)
        }
    }

    override fun onDestroy() {
        clearFilePathCallback()
        if (::webView.isInitialized) {
            webContainer.removeAllViews()
            webView.destroy()
        }
        super.onDestroy()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (previewMode == Mode.FOLLOW) {
            applyPreview(Mode.FOLLOW)
        }
    }

    private fun modeFromPersistedSkin(): Mode = when (ThemeDemoPage.demoThemeMode) {
        DemoThemeMode.LIGHT, DemoThemeMode.CUSTOM -> Mode.LIGHT
        DemoThemeMode.DARK -> Mode.DARK
        DemoThemeMode.FOLLOW_SYSTEM -> Mode.FOLLOW
    }

    private fun resolveWebDark(): Boolean = when (previewMode) {
        Mode.LIGHT -> false
        Mode.DARK -> true
        Mode.FOLLOW -> ThemeDemoPage.resolveFollowNight(this)
    }

    private fun syncToggle(mode: Mode) {
        bindingUi = true
        toggleGroup.check(
            when (mode) {
                Mode.LIGHT -> R.id.webDayNightLight
                Mode.DARK -> R.id.webDayNightDark
                Mode.FOLLOW -> R.id.webDayNightFollow
            },
        )
        bindingUi = false
    }

    /** 改 Activity 日夜 + 刷原生皮肤 + apply WebSettings；不 reload。 */
    private fun applyPreview(mode: Mode) {
        previewMode = mode
        syncToggle(mode)

        delegate.localNightMode = when (mode) {
            Mode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            Mode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            Mode.FOLLOW -> ThemeDemoPage.getDefaultNightMode()
        }

        val dark = resolveWebDark()
        ThemeManager.get().apply(this, AppCompatDayNightResourceResolver.of(this, dark))
        paintChrome()

        if (::webView.isInitialized) {
            WebViewDayNightCompat.apply(webView, dark)
            val page = ThemeManager.get().getResolver(this)?.getColor(R.color.skin_page_bg)
                ?: ContextCompat.getColor(this, R.color.skin_page_bg)
            webView.setBackgroundColor(page)
        }
    }

    private fun paintChrome() {
        val resolver = ThemeManager.get().getResolver(this) ?: return
        val page = resolver.getColor(R.color.skin_page_bg)
        val panel = resolver.getColor(R.color.skin_panel_bg)
        val primary = resolver.getColor(R.color.skin_text_primary)
        val secondary = resolver.getColor(R.color.skin_text_secondary)
        val accent = resolver.getColor(R.color.skin_accent)
        val onAccent = resolver.getColor(R.color.skin_button_text)

        findViewById<ViewGroup>(R.id.webDayNightRoot).setBackgroundColor(page)
        findViewById<ViewGroup>(R.id.webDayNightTitleBar).setBackgroundColor(panel)
        webContainer.setBackgroundColor(page)
        findViewById<TextView>(R.id.webDayNightTitle).setTextColor(primary)
        findViewById<TextView>(R.id.webDayNightSubtitle).setTextColor(secondary)

        val textColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
            intArrayOf(onAccent, primary),
        )
        val bgColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
            intArrayOf(accent, Color.TRANSPARENT),
        )
        val strokeColors = ColorStateList(
            arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked)),
            intArrayOf(accent, accent),
        )
        listOf(R.id.webDayNightLight, R.id.webDayNightDark, R.id.webDayNightFollow).forEach { id ->
            findViewById<MaterialButton>(id).apply {
                backgroundTintList = bgColors
                setTextColor(textColors)
                strokeColor = strokeColors
                strokeWidth = (1.5f * resources.displayMetrics.density).toInt().coerceAtLeast(2)
            }
        }
        syncStatusBarIconAppearance()
    }



    private fun installWebView(restore: Bundle?) {
        // 用 Activity Context 创建，scheme 才能跟 localNightMode 热更新；不要锁死亮/暗 ThemeWrapper。
        webView = createConfiguredWebView(this)
        val page = ThemeManager.get().getResolver(this)?.getColor(R.color.skin_page_bg)
            ?: ContextCompat.getColor(this, R.color.skin_page_bg)
        webView.setBackgroundColor(page)
        webContainer.addView(
            webView,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            ),
        )
        WebViewDayNightCompat.apply(webView, resolveWebDark())

        val restored = restore != null && webView.restoreState(restore) != null
        if (!restored) {
            webView.loadUrl(ASSET_PAGE_A)
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun createConfiguredWebView(context: Context): WebView {
        return WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webChromeClient = object : WebChromeClient() {
                override fun onShowFileChooser(
                    webView: WebView?,
                    filePathCallback: ValueCallback<Array<Uri>>?,
                    fileChooserParams: FileChooserParams?,
                ): Boolean {
                    // 上一次选择未结束就再点：先取消旧回调，否则 WebView 会卡住。
                    clearFilePathCallback()
                    this@WebViewDayNightActivity.filePathCallback = filePathCallback
                    val intent = fileChooserParams?.createIntent()
                        ?: return cancelFileChooser()
                    return try {
                        fileChooserLauncher.launch(intent)
                        true
                    } catch (_: ActivityNotFoundException) {
                        Toast.makeText(
                            this@WebViewDayNightActivity,
                            R.string.webview_file_chooser_unavailable,
                            Toast.LENGTH_SHORT,
                        ).show()
                        cancelFileChooser()
                    }
                }
            }
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView,
                    request: WebResourceRequest,
                ): Boolean = false
            }
        }
    }

    private fun clearFilePathCallback() {
        filePathCallback?.onReceiveValue(null)
        filePathCallback = null
    }

    private fun cancelFileChooser(): Boolean {
        clearFilePathCallback()
        return false
    }

    companion object {
        private const val ASSET_PAGE_A = "file:///android_asset/webview/page_a.html"
        private const val STATE_MODE = "preview_mode"
        private const val STATE_WEB_VIEW = "web_view"
    }
}
