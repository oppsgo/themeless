package io.github.oppsgo.themeless.demo

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.androidx.binding.AppCompatImageViewResourceBinding
import io.github.oppsgo.android.theme.androidx.binding.AppCompatTextViewResourceBinding
import io.github.oppsgo.android.theme.androidx.binding.RecyclerViewResourceBinding
import io.github.oppsgo.android.theme.androidx.resolver.AppCompatDayNightResourceResolver
import io.github.oppsgo.android.theme.binding.BaseViewResourceBinding
import io.github.oppsgo.android.theme.binding.ImageViewResourceBinding
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding
import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver
import io.github.oppsgo.android.theme.resource.DrawableRef
import io.github.oppsgo.themeless.R

private const val DEMO_TAG = "ThemelessDemo"

/**
 * Demo 页共享逻辑。
 *
 * 手动亮/暗/自定义存在进程级状态里，避免 AppCompat `localNightMode` recreate 后丢失。
 */
object ThemeDemoPage {
    init {
        // inflate 前显式注册；否则 RecyclerView 只会挂上默认 ViewGroupBinding
        RecyclerViewResourceBinding.register()
        AppCompatImageViewResourceBinding.register()
        AppCompatTextViewResourceBinding.register()
    }

    /** 首页「全局日夜」；进 AppCompat Demo 时作为 FOLLOW 的 localNightMode。 */
    var dayNightMode: Int = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        private set

    /** 当前 Demo 页手动主题；recreate 后 restore。 */
    var demoThemeMode: DemoThemeMode = DemoThemeMode.FOLLOW_SYSTEM
        private set

    fun init() {
        // 触发 object init（Binding 注册）
    }

    fun setDefaultNightMode(mode: Int) {
        dayNightMode = mode
    }

    fun getDefaultNightMode() = dayNightMode

    fun setDemoThemeMode(mode: DemoThemeMode) {
        demoThemeMode = mode
    }

    /**
     * AppCompat Demo `attachBaseContext` 用。
     *
     * 亮色/自定义也锁 [MODE_NIGHT_YES]：系统夜间下若 Activity 是 Light 主题，
     * Force Dark 会把已画好的浅色像素反相。肤色只由 ThemeManager Resolver 提供。
     */
    fun resolveLocalNightMode(): Int = when (demoThemeMode) {
        DemoThemeMode.LIGHT, DemoThemeMode.CUSTOM, DemoThemeMode.DARK ->
            AppCompatDelegate.MODE_NIGHT_YES
        DemoThemeMode.FOLLOW_SYSTEM -> dayNightMode
    }

    /**
     * 「跟随」语义下当前该用暗色皮肤吗。
     * 首页 [dayNightMode] 优先；只有 FOLLOW_SYSTEM 才读真正的系统 Application uiMode。
     */
    fun resolveFollowNight(context: android.content.Context): Boolean = when (dayNightMode) {
        AppCompatDelegate.MODE_NIGHT_YES -> true
        AppCompatDelegate.MODE_NIGHT_NO -> false
        else -> DayNightResourceResolver.isSystemNight(context)
    }
}

/** Demo 当前手动模式；跟随系统时才响应「系统」uiMode 变化。 */
enum class DemoThemeMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
    CUSTOM,
}

internal fun Activity.showThemeDemo(@StringRes subtitle: Int) {
    setContentView(R.layout.activity_theme_demo)
    setupImmersiveTitleBar()
    ThemeManager.get().setRefreshOnInflate(this, true)
    ensureDemoBackgroundBindings()
    findViewById<TextView>(R.id.themeSubtitle).setText(subtitle)
    bindSizedIcon()
    bindManualText()
    bindThemeList()

    findViewById<Button>(R.id.btnLight).setOnClickListener { applyThemeNight(dark = false) }
    findViewById<Button>(R.id.btnDark).setOnClickListener { applyThemeNight(dark = true) }
    findViewById<Button>(R.id.btnCustom).setOnClickListener { applyCustomTheme() }

    restoreDemoTheme()
}

/** 确保页面容器 / 列表相关 background 一定在 Binding 里，换肤才能改到底色。 */
private fun Activity.ensureDemoBackgroundBindings() {
    fun bindBg(id: Int, color: Int) {
        val v = findViewById<View>(id) ?: return
        val binding = ThemeManager.get().obtainBinding(v)
        if (binding is BaseViewResourceBinding<*>) {
            binding.setBackground(color)
        }
    }
    bindBg(R.id.themeRoot, R.color.skin_page_bg)
    bindBg(R.id.themeBody, R.color.skin_page_bg)
    bindBg(R.id.themeControlsPane, R.color.skin_page_bg)
    bindBg(R.id.themeTitleBar, R.color.skin_panel_bg)
    bindBg(R.id.themeListPane, R.color.skin_panel_bg)
    bindBg(R.id.themeRecycler, R.color.skin_panel_bg)
}

/** 按 [ThemeDemoPage.demoThemeMode] 重新 apply；进页 / config 变化后调用。 */
private var restoringDemoTheme = false

internal fun Activity.restoreDemoTheme() {
    if (restoringDemoTheme) return
    restoringDemoTheme = true
    try {
        when (ThemeDemoPage.demoThemeMode) {
            DemoThemeMode.FOLLOW_SYSTEM -> applyThemeResources()
            DemoThemeMode.LIGHT -> applyThemeNight(dark = false)
            DemoThemeMode.DARK -> applyThemeNight(dark = true)
            DemoThemeMode.CUSTOM -> applyCustomTheme()
        }
    } finally {
        restoringDemoTheme = false
    }
}

/** 自定义主题：固定亮色板 + id 重映射。底座强制走亮色 Configuration。 */
internal fun Activity.applyCustomTheme() {
    ThemeDemoPage.setDemoThemeMode(DemoThemeMode.CUSTOM)
    if (syncActivityNightMode(dark = false)) return
    val base = DayNightResourceResolver.light(this)
    val mapped = MappedResourceResolver.skyBlue(base)
    val page = mapped.getColor(R.color.skin_page_bg)
    val panel = mapped.getColor(R.color.skin_panel_bg)
    val card = mapped.getColor(R.color.skin_card_bg)
    Log.i(
        DEMO_TAG,
        "apply custom page=0x${page.hex()} panel=0x${panel.hex()} card=0x${card.hex()} " +
            "raw=0x${base.getColor(R.color.skin_page_bg).hex()} " +
            "blue=0x${base.getColor(R.color.skin_page_bg_blue).hex()} " +
            "appNight=${DayNightResourceResolver.isSystemNight(this)} actNight=${isActivityNight()}",
    )
    ThemeManager.get().apply(this, mapped)
    paintDemoSurfaces("custom", page, panel, card)
}

internal fun Activity.applyThemeNight(dark: Boolean) {
    ThemeDemoPage.setDemoThemeMode(if (dark) DemoThemeMode.DARK else DemoThemeMode.LIGHT)
    if (syncActivityNightMode(dark = dark)) return
    val resolver = if (this is AppCompatActivity) {
        AppCompatDayNightResourceResolver.of(this, dark)
    } else {
        DayNightResourceResolver.of(this, dark)
    }
    val page = resolver.getColor(R.color.skin_page_bg)
    val panel = resolver.getColor(R.color.skin_panel_bg)
    val card = resolver.getColor(R.color.skin_card_bg)
    Log.i(
        DEMO_TAG,
        "apply ${if (dark) "dark" else "light"} page=0x${page.hex()} panel=0x${panel.hex()} " +
            "card=0x${card.hex()} actNight=${isActivityNight()}",
    )
    ThemeManager.get().apply(this, resolver)
    paintDemoSurfaces(if (dark) "dark" else "light", page, panel, card)
}

internal fun Activity.applyThemeResources() {
    ThemeDemoPage.setDemoThemeMode(DemoThemeMode.FOLLOW_SYSTEM)
    if (syncActivityNightMode(dark = null)) return
    // 跟随首页 DefaultNightMode，而不是只读系统 Application（否则「强制暗色」皮肤不生效）
    val night = ThemeDemoPage.resolveFollowNight(this)
    val resolver = if (this is AppCompatActivity) {
        AppCompatDayNightResourceResolver.of(this, night)
    } else {
        DayNightResourceResolver.of(this, night)
    }
    val page = resolver.getColor(R.color.skin_page_bg)
    val panel = resolver.getColor(R.color.skin_panel_bg)
    val card = resolver.getColor(R.color.skin_card_bg)
    Log.i(
        DEMO_TAG,
        "apply follow night=$night page=0x${page.hex()} panel=0x${panel.hex()} card=0x${card.hex()} " +
            "dayNightMode=${ThemeDemoPage.getDefaultNightMode()} " +
            "systemNight=${DayNightResourceResolver.isSystemNight(this)} actNight=${isActivityNight()}",
    )
    ThemeManager.get().apply(this, resolver)
    paintDemoSurfaces("follow", page, panel, card)
}

/**
 * @return true 表示正在切 localNightMode / recreate，调用方应立刻 return，等新实例 restoreDemoTheme。
 *
 * 仅 AppCompatActivity 需要同步宿主日夜（躲开 Force Dark、对齐首页 DefaultNightMode）。
 * FragmentActivity 肤色全靠隔离 [DayNightResourceResolver]；旧逻辑在 uiMode 不一致时
 * 只 [Activity.recreate] 却不改配置，会无限闪屏。
 */
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
    // 手动亮/暗/自定义：若当前是 Light 宿主则切到 YES 躲开 Force Dark
    val mode = delegate.localNightMode
    if (mode == AppCompatDelegate.MODE_NIGHT_YES || mode != AppCompatDelegate.MODE_NIGHT_NO) {
        return false
    }
    delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_YES
    return true
}

private fun Activity.isActivityNight(): Boolean {
    val mask = android.content.res.Configuration.UI_MODE_NIGHT_MASK
    return (resources.configuration.uiMode and mask) ==
        android.content.res.Configuration.UI_MODE_NIGHT_YES
}

/**
 * 按当前 Resolver 强制上色。左侧底色画在 themeControlsPane（LinearLayout），
 * ScrollView 保持透明。
 */
private fun Activity.paintDemoSurfaces(label: String, page: Int, panel: Int, card: Int) {
    fun bg(id: Int, color: Int) {
        val v = findViewById<View>(id) ?: return
        v.setBackgroundColor(color)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            v.backgroundTintList = null
        }
    }
    window.decorView.setBackgroundColor(page)
    bg(R.id.themeRoot, page)
    bg(R.id.themeBody, page)
    bg(R.id.themeControlsPane, page)
    findViewById<View>(R.id.themeControlsScroll)?.setBackgroundColor(Color.TRANSPARENT)
    findViewById<View>(R.id.themeControlsContent)?.setBackgroundColor(Color.TRANSPARENT)
    bg(R.id.themeTitleBar, panel)
    bg(R.id.themeListPane, panel)
    findViewById<RecyclerView>(R.id.themeRecycler)?.let { rv ->
        rv.setBackgroundColor(panel)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            rv.backgroundTintList = null
        }
        for (i in 0 until rv.childCount) {
            rv.getChildAt(i)?.setBackgroundColor(card)
        }
        rv.adapter?.notifyDataSetChanged()
    }
    syncStatusBarIconAppearance()
    findViewById<TextView>(R.id.themeTitle)?.text = "换肤 $label"

    val pane = findViewById<View>(R.id.themeControlsPane) ?: return
    pane.post {
        sampleDisplayedPixel(pane) { panePx ->
            sampleDisplayedPixel(findViewById(R.id.themeTitleBar)) { titlePx ->
                Log.i(
                    DEMO_TAG,
                    "pixel $label drawable=0x${page.hex()} panePx=0x${panePx.hex()} " +
                        "titlePx=0x${titlePx.hex()} match=${panePx == page}",
                )
            }
        }
    }
}

/** 读窗口合成后的真实像素，判断是否被 Force Dark / 厂商压暗。 */
private fun Activity.sampleDisplayedPixel(view: View?, onResult: (Int) -> Unit) {
    if (view == null || android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
        onResult(0)
        return
    }
    view.post {
        if (view.width <= 0 || view.height <= 0) {
            onResult(0)
            return@post
        }
        val loc = IntArray(2)
        view.getLocationInWindow(loc)
        val x = (loc[0] + view.width / 2).coerceAtLeast(0)
        val y = (loc[1] + view.height / 2).coerceAtLeast(0)
        val bitmap = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)
        val src = android.graphics.Rect(x, y, x + 1, y + 1)
        try {
            android.view.PixelCopy.request(
                window,
                src,
                bitmap,
                { result ->
                    onResult(
                        if (result == android.view.PixelCopy.SUCCESS) bitmap.getPixel(0, 0) else 0,
                    )
                },
                android.os.Handler(android.os.Looper.getMainLooper()),
            )
        } catch (_: Throwable) {
            onResult(0)
        }
    }
}

private fun Int.hex(): String = Integer.toHexString(this)

private fun Activity.bindSizedIcon() {
    val icon = findViewById<ImageView>(R.id.themeSizedIcon)
    val binding = ThemeManager.get().obtainBinding(icon) as ImageViewResourceBinding
    val size = (72 * resources.displayMetrics.density).toInt()
    binding.setImage(
        DrawableRef.of(R.drawable.mail_star_fill) { resolver, resourceId ->
            val drawable = resolver.getDrawable(resourceId)?.mutate() ?: return@of null
            drawable.setBounds(0, 0, size, size)
            drawable
        },
    )
}

private fun Activity.bindManualText() {
    val managed = findViewById<TextView>(R.id.themeManagedLabel)
    var useAccent = false
    findViewById<Button>(R.id.btnThemeDynamic).setOnClickListener {
        useAccent = !useAccent
        val color = if (useAccent) R.color.skin_accent else R.color.skin_text_primary
        TextViewResourceBinding.of(managed).setTextColor(color).refresh()
    }

    val rows = findViewById<LinearLayout>(R.id.themeExtraRows)
    findViewById<Button>(R.id.btnThemeUnmanaged).setOnClickListener {
        val created = TextView(this).apply {
            text = getString(R.string.theme_demo_unmanaged_line)
            textSize = 16f
            val pad = (8 * resources.displayMetrics.density).toInt()
            setPadding(0, pad, 0, 0)
        }
        rows.addView(created)
        TextViewResourceBinding.of(created)
            .setTextColor(R.color.skin_accent)
            .refresh()
    }

    val host = this as? FragmentActivity
    findViewById<Button>(R.id.btnThemeDialog).setOnClickListener {
        if (host == null) return@setOnClickListener
        ThemeLayerDialogFragment.show(
            host.supportFragmentManager,
            R.string.theme_demo_dialog_message
        )
    }
    val popupAnchor = findViewById<Button>(R.id.btnThemePopup)
    var popup: PopupWindow? = null
    popupAnchor.setOnClickListener {
        popup?.dismiss()
        val content = layoutInflater.inflate(R.layout.popup_theme_layer, null)
        val window = PopupWindow(
            content,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        )
        window.isOutsideTouchable = true
        window.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        popup = window
        window.setOnDismissListener { if (popup === window) popup = null }
        window.showAsDropDown(popupAnchor, 0, 0, Gravity.START)
    }
}

/** 单个 RecyclerView；复用条目由 RecyclerViewResourceBinding 补刷。 */
private fun Activity.bindThemeList() {
    val recycler = findViewById<RecyclerView>(R.id.themeRecycler)
    lateinit var adapter: ThemeRowAdapter
    adapter = ThemeRowAdapter((0..11).toMutableList()) {
        adapter.appendMore()
    }
    recycler.layoutManager = LinearLayoutManager(this)
    recycler.adapter = adapter
}

/** 从页面或从对话框按钮里再弹出。换主题时不用自己 refresh。 */
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
