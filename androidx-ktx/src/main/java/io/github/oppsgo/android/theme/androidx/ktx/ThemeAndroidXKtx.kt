@file:JvmName("ThemeAndroidXKtx")

package io.github.oppsgo.android.theme.androidx.ktx

import android.content.Context
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import io.github.oppsgo.android.theme.androidx.binding.AppCompatCompoundButtonResourceBinding
import io.github.oppsgo.android.theme.androidx.binding.AppCompatImageViewResourceBinding
import io.github.oppsgo.android.theme.androidx.binding.AppCompatTextViewResourceBinding
import io.github.oppsgo.android.theme.androidx.binding.RecyclerViewResourceBinding
import io.github.oppsgo.android.theme.androidx.binding.SwitchCompatResourceBinding
import io.github.oppsgo.android.theme.androidx.resolver.AppCompatDayNightResourceResolver
import io.github.oppsgo.android.theme.ktx.applyTheme

// ── register ──

/** inflate 前登记 AndroidX AppCompat / RecyclerView 的 Binding（幂等）。 */
fun registerAppCompatThemeBindings() {
    AppCompatTextViewResourceBinding.register()
    AppCompatImageViewResourceBinding.register()
    AppCompatCompoundButtonResourceBinding.register()
    SwitchCompatResourceBinding.register()
    RecyclerViewResourceBinding.register()
}

// ── DayNight ──

fun Context.appCompatDayNightLight(): AppCompatDayNightResourceResolver =
    AppCompatDayNightResourceResolver.light(this)

fun Context.appCompatDayNightNight(): AppCompatDayNightResourceResolver =
    AppCompatDayNightResourceResolver.night(this)

fun Context.appCompatDayNightFollowSystem(): AppCompatDayNightResourceResolver =
    AppCompatDayNightResourceResolver.followSystem(this)

fun Context.applyAppCompatDayNight(dark: Boolean) {
    applyTheme(AppCompatDayNightResourceResolver.of(this, dark))
}

// ── edit（对齐 ThemeManager.edit / Binding.of）──

fun AppCompatTextView.edit(): AppCompatTextViewResourceBinding =
    AppCompatTextViewResourceBinding.of(this)

fun AppCompatImageView.edit(): AppCompatImageViewResourceBinding =
    AppCompatImageViewResourceBinding.of(this)

fun AppCompatCheckBox.edit(): AppCompatCompoundButtonResourceBinding =
    AppCompatCompoundButtonResourceBinding.of(this)

fun AppCompatRadioButton.edit(): AppCompatCompoundButtonResourceBinding =
    AppCompatCompoundButtonResourceBinding.of(this)

fun AppCompatToggleButton.edit(): AppCompatCompoundButtonResourceBinding =
    AppCompatCompoundButtonResourceBinding.of(this)

fun SwitchCompat.edit(): SwitchCompatResourceBinding =
    SwitchCompatResourceBinding.of(this)

fun RecyclerView.edit(): RecyclerViewResourceBinding =
    RecyclerViewResourceBinding.of(this)

// ── theme 块 ──

inline fun AppCompatTextView.theme(
    block: AppCompatTextViewResourceBinding.(AppCompatTextView) -> Unit,
): AppCompatTextView {
    edit().block(this)
    return this
}

inline fun AppCompatImageView.theme(
    block: AppCompatImageViewResourceBinding.(AppCompatImageView) -> Unit,
): AppCompatImageView {
    edit().block(this)
    return this
}

inline fun AppCompatCheckBox.theme(
    block: AppCompatCompoundButtonResourceBinding.(AppCompatCheckBox) -> Unit,
): AppCompatCheckBox {
    edit().block(this)
    return this
}

inline fun AppCompatRadioButton.theme(
    block: AppCompatCompoundButtonResourceBinding.(AppCompatRadioButton) -> Unit,
): AppCompatRadioButton {
    edit().block(this)
    return this
}

inline fun AppCompatToggleButton.theme(
    block: AppCompatCompoundButtonResourceBinding.(AppCompatToggleButton) -> Unit,
): AppCompatToggleButton {
    edit().block(this)
    return this
}

inline fun SwitchCompat.theme(
    block: SwitchCompatResourceBinding.(SwitchCompat) -> Unit,
): SwitchCompat {
    edit().block(this)
    return this
}

inline fun RecyclerView.theme(
    block: RecyclerViewResourceBinding.(RecyclerView) -> Unit,
): RecyclerView {
    edit().block(this)
    return this
}
