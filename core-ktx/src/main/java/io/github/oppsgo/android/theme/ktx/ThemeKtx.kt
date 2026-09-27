@file:JvmName("ThemeKtx")

package io.github.oppsgo.android.theme.ktx

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import io.github.oppsgo.android.theme.ResourceBinding
import io.github.oppsgo.android.theme.ResourceBindingFactory
import io.github.oppsgo.android.theme.ResourceResolver
import io.github.oppsgo.android.theme.ThemeManager
import io.github.oppsgo.android.theme.binding.CompoundButtonResourceBinding
import io.github.oppsgo.android.theme.binding.ImageViewResourceBinding
import io.github.oppsgo.android.theme.binding.SwitchResourceBinding
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding
import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver

// ── ThemeManager ──

/** 在 `super.onCreate()` 之前调用，等价于 [ThemeManager.install]。 */
fun Activity.installTheme(factory: LayoutInflater.Factory2? = null) {
    ThemeManager.get().install(this, factory)
}

/** 换一套 [ResourceResolver] 并刷新内容树，等价于 [ThemeManager.apply]。 */
fun Context.applyTheme(resolver: ResourceResolver) {
    ThemeManager.get().apply(this, resolver)
}

/** 用当前 Resolver 再刷一遍，等价于 [ThemeManager.refresh]。 */
fun Context.refreshTheme() {
    ThemeManager.get().refresh(this)
}

/** 从该 View 起刷新（浮层根也可用）。 */
fun View.refreshTheme() {
    ThemeManager.get().refresh(this)
}

/** 当前已 apply 的 [ResourceResolver]；未安装则为 null。 */
val Context.resourceResolver: ResourceResolver?
    get() = ThemeManager.get().getResolver(this)

/** inflate 后是否立刻跟肤。 */
var Context.refreshOnInflate: Boolean
    get() = ThemeManager.get().shouldRefreshOnInflate(this)
    set(value) = ThemeManager.get().setRefreshOnInflate(this, value)

/** 配置 Binding 注册表，等价于 `ThemeManager.get().registry().…`。 */
inline fun registry(block: ResourceBindingFactory.() -> Unit) {
    ThemeManager.get().registry().block()
}

// ── DayNight ──

/** 亮色隔离 Configuration，等价于 [DayNightResourceResolver.light]。 */
fun Context.dayNightLight(): DayNightResourceResolver = DayNightResourceResolver.light(this)

/** 暗色隔离 Configuration，等价于 [DayNightResourceResolver.night]。 */
fun Context.dayNightNight(): DayNightResourceResolver = DayNightResourceResolver.night(this)

/** 跟随系统 Application uiMode，等价于 [DayNightResourceResolver.followSystem]。 */
fun Context.dayNightFollowSystem(): DayNightResourceResolver =
    DayNightResourceResolver.followSystem(this)

/** 按亮/暗 apply 平台 DayNight Resolver。 */
fun Context.applyDayNight(dark: Boolean) {
    applyTheme(DayNightResourceResolver.of(this, dark))
}

// ── 取 Binding（对齐 ThemeManager.edit / obtain / find）──

/**
 * 取 Binding（有则复用，无则临时），等价于 [ThemeManager.edit]。
 * 运行时类型由 registry 按 View 类决定；需要具体 API 时用 [binding]。
 *
 * 带 lambda 的配置请用 [theme]，不要做成 `edit { }`（与零参抢解析）。
 */
@Suppress("UNCHECKED_CAST")
fun <V : View> V.edit(): ResourceBinding =
    ThemeManager.get().edit(this)

/**
 * 取 Binding 并转为 [B]（registry 已按 View 类型创建好的实例）。
 * 新控件一般不必再写 `Xxx.edit()` 扩展，register 后直接：
 * `view.binding<XxxResourceBinding>().…` / `view.themeAs<XxxResourceBinding> { }`。
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified B : ResourceBinding> View.binding(): B =
    ThemeManager.get().edit(this) as B

fun TextView.edit(): TextViewResourceBinding = TextViewResourceBinding.of(this)

fun ImageView.edit(): ImageViewResourceBinding = ImageViewResourceBinding.of(this)

fun CompoundButton.edit(): CompoundButtonResourceBinding = CompoundButtonResourceBinding.of(this)

fun Switch.edit(): SwitchResourceBinding = SwitchResourceBinding.of(this)

/** 取或创建并挂 tag，等价于 [ThemeManager.obtain]。 */
fun View.obtain(): ResourceBinding = ThemeManager.get().obtain(this)

/** 只查已挂载 Binding，等价于 [ThemeManager.find]。 */
fun View.findBinding(): ResourceBinding? = ThemeManager.get().find(this)

/** 只查已挂载且类型匹配的 Binding。 */
fun <T : ResourceBinding> View.findBinding(clazz: Class<T>): T? =
    ThemeManager.get().find(this, clazz)

/**
 * 在 Binding 上配置主题属性。
 * 平台 TextView / ImageView / CompoundButton / Switch 有更具体的重载；
 * 其它 View 用本方法，或 [themeAs] / [binding] 指定具体 Binding 类型。
 */
inline fun <V : View> V.theme(block: ResourceBinding.() -> Unit): V {
    edit().block()
    return this
}

/**
 * 指定 Binding 类型的配置块，免去为每个控件写 `edit`/`theme` 扩展。
 * 只有一个类型参数，才能写成 `view.themeAs<XxxBinding> { }`（Kotlin 不允许部分指定泛型）。
 * 控件本身用外层变量即可；需要时在块内用 `this@themeAs` 取 View。
 * 例：`viewPager.themeAs<ViewPager2ResourceBinding> { }`
 */
inline fun <reified B : ResourceBinding> View.themeAs(
    block: B.() -> Unit,
): View {
    binding<B>().block()
    return this
}

inline fun TextView.theme(block: TextViewResourceBinding.(TextView) -> Unit): TextView {
    edit().block(this)
    return this
}

inline fun ImageView.theme(block: ImageViewResourceBinding.(ImageView) -> Unit): ImageView {
    edit().block(this)
    return this
}

inline fun CompoundButton.theme(
    block: CompoundButtonResourceBinding.(CompoundButton) -> Unit,
): CompoundButton {
    edit().block(this)
    return this
}

inline fun Switch.theme(block: SwitchResourceBinding.(Switch) -> Unit): Switch {
    edit().block(this)
    return this
}
