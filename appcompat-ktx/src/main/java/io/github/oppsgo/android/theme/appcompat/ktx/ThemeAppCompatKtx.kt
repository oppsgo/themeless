@file:JvmName("ThemeAppCompatKtx")

package io.github.oppsgo.android.theme.appcompat.ktx

import android.content.Context
import io.github.oppsgo.android.theme.appcompat.resolver.AppCompatDayNightResourceResolver
import io.github.oppsgo.android.theme.ktx.applyTheme

// Binding 登记请用 :appcompat 的 ThemeAppCompat.registerAvailable()，不在本 ktx 重复提供。

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
