package io.github.oppsgo.themeless.demo

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
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
import io.github.oppsgo.android.theme.androidx.binding.RecyclerViewResourceBinding
import io.github.oppsgo.android.theme.androidx.resolver.AppCompatContextResourceResolver
import io.github.oppsgo.android.theme.androidx.resolver.AppCompatDayNightResourceResolver
import io.github.oppsgo.android.theme.binding.ImageViewResourceBinding
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding
import io.github.oppsgo.android.theme.resolver.ContextResourceResolver
import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver
import io.github.oppsgo.android.theme.resource.DrawableRef
import io.github.oppsgo.themeless.R

import java.util.WeakHashMap

/** Demo 当前手动模式；跟随系统时才响应 uiMode 变化。 */
private enum class DemoThemeMode {
    FOLLOW_SYSTEM,
    LIGHT,
    DARK,
    CUSTOM,
}

private val demoThemeModes = WeakHashMap<Activity, DemoThemeMode>()

private var Activity.demoThemeMode: DemoThemeMode
    get() = demoThemeModes[this] ?: DemoThemeMode.FOLLOW_SYSTEM
    set(value) {
        demoThemeModes[this] = value
    }

internal fun Activity.showThemeDemo(@StringRes subtitle: Int) {
    // 必须在 setContentView 之前注册，否则 inflate 时 RecyclerView 只会挂上 ViewGroupBinding
    RecyclerViewResourceBinding.register()
    setContentView(R.layout.activity_theme_demo)
    setupImmersiveTitleBar()
    // 运行中换主题后，Dialog / PopupWindow 新 inflate 的内容也立刻按当前 resolver 刷一遍。
    ThemeManager.get().setRefreshOnInflate(this, true)
    findViewById<TextView>(R.id.themeSubtitle).setText(subtitle)
    applyThemeResources()
    bindSizedIcon()
    bindManualText()
    bindThemeList()

    findViewById<Button>(R.id.btnLight).setOnClickListener {
        applyThemeNight(dark = false)
    }
    findViewById<Button>(R.id.btnDark).setOnClickListener {
        applyThemeNight(dark = true)
    }
    findViewById<Button>(R.id.btnCustom).setOnClickListener {
        applyCustomTheme()
    }
}

internal fun Activity.applyThemeResources() {
    demoThemeMode = DemoThemeMode.FOLLOW_SYSTEM
    // skin_* 走隔离 Resolver；AppCompat 页面再同步 localNightMode，让未托管的主题属性也跟系统。
    syncAppCompatLocalNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    ThemeManager.get().apply(
        this,
        if (this is AppCompatActivity) {
            AppCompatDayNightResourceResolver.followSystem(this)
        } else {
            DayNightResourceResolver.followSystem(this)
        },
    )
    syncWindowAndStatusBar()
}

/** 自定义主题：一套固定色，只做 id 重映射，不走 DayNight。 */
internal fun Activity.applyCustomTheme() {
    demoThemeMode = DemoThemeMode.CUSTOM
    // 固定亮色板，AppCompat chrome 也锁到亮色，避免系统暗色把未托管属性盖暗。
    syncAppCompatLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    val base = if (this is AppCompatActivity) {
        AppCompatContextResourceResolver(this)
    } else {
        ContextResourceResolver(this)
    }
    ThemeManager.get().apply(this, MappedResourceResolver.skyBlue(base))
    syncWindowAndStatusBar()
}

/** 系统 uiMode 变化时：仅跟随系统才重刷。 */
internal fun Activity.reapplyDemoThemeIfFollowingSystem() {
    if (demoThemeMode != DemoThemeMode.FOLLOW_SYSTEM) return
    applyThemeResources()
}

/** AppCompatActivity 才有意义；与 ThemeManager 的亮/暗/跟随对齐。 */
private fun Activity.syncAppCompatLocalNightMode(mode: Int) {
    val host = this as? AppCompatActivity ?: return
    if (host.delegate.localNightMode != mode) {
        host.delegate.localNightMode = mode
    }
}

/** 窗口底 + 状态栏图标；否则夜间 windowBackground 透出的黑底会盖过未刷到的区域。 */
private fun Activity.syncWindowAndStatusBar() {
    val resolver = ThemeManager.get().getResolver(this) ?: return
    window.decorView.setBackgroundColor(resolver.getColor(R.color.skin_page_bg))
    syncStatusBarIconAppearance()
}

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
        ThemeLayerDialogFragment.show(host.supportFragmentManager, R.string.theme_demo_dialog_message)
    }
    val popupAnchor = findViewById<Button>(R.id.btnThemePopup)
    var popup: PopupWindow? = null
    popupAnchor.setOnClickListener {
        popup?.dismiss()
        // inflate(..., null) 会走 ThemeDelegate.watchForeignWindow，挂上后记入 windowRoots，
        // 换主题时 ThemeManager.apply → refresh 会刷到这层浮层。
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

internal fun Activity.applyThemeNight(dark: Boolean) {
    demoThemeMode = if (dark) DemoThemeMode.DARK else DemoThemeMode.LIGHT
    syncAppCompatLocalNightMode(
        if (dark) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO,
    )
    ThemeManager.get().apply(
        this,
        if (this is AppCompatActivity) {
            AppCompatDayNightResourceResolver.of(this, dark)
        } else {
            DayNightResourceResolver.of(this, dark)
        },
    )
    syncWindowAndStatusBar()
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
