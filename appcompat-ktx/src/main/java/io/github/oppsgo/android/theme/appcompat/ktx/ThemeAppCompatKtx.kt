@file:JvmName("ThemeAppCompatKtx")

package io.github.oppsgo.android.theme.appcompat.ktx

import android.content.Context
import android.support.v7.widget.AppCompatCheckBox
import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatRadioButton
import android.support.v7.widget.AppCompatTextView
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SwitchCompat
import io.github.oppsgo.android.theme.appcompat.binding.AppCompatCompoundButtonResourceBinding
import io.github.oppsgo.android.theme.appcompat.binding.AppCompatImageViewResourceBinding
import io.github.oppsgo.android.theme.appcompat.binding.AppCompatTextViewResourceBinding
import io.github.oppsgo.android.theme.appcompat.binding.RecyclerViewResourceBinding
import io.github.oppsgo.android.theme.appcompat.binding.SwitchCompatResourceBinding
import io.github.oppsgo.android.theme.appcompat.resolver.AppCompatDayNightResourceResolver
import io.github.oppsgo.android.theme.ktx.applyTheme

// ── register ──

/**
 * inflate 前登记 Support AppCompat / RecyclerView 的 Binding（幂等）。
 * Support 无 [android.support.v7.widget.AppCompatToggleButton]。
 */
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

// ── edit ──

fun AppCompatTextView.edit(): AppCompatTextViewResourceBinding =
    AppCompatTextViewResourceBinding.of(this)

fun AppCompatImageView.edit(): AppCompatImageViewResourceBinding =
    AppCompatImageViewResourceBinding.of(this)

fun AppCompatCheckBox.edit(): AppCompatCompoundButtonResourceBinding =
    AppCompatCompoundButtonResourceBinding.of(this)

fun AppCompatRadioButton.edit(): AppCompatCompoundButtonResourceBinding =
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
