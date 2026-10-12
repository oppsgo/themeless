package io.github.oppsgo.android.theme;

import android.app.Activity;

import androidx.annotation.NonNull;

/**
 * 整个 Activity 主题刷新完成的回调。
 * 通过 {@link ThemeManager#addOnThemeUpdateListener} 注册，支持多个；
 * 列表存在该页的 {@link ThemeDelegate} 上，随页面回收，不随 {@link ThemeManager} 单例泄漏。
 */
public interface OnThemeUpdateListener {

    /**
     * 该 Activity 的内容树（含已登记浮层根）按新资源刷完之后调用。
     * 在发起刷新的线程同步触发，一般是主线程。
     */
    void onThemeUpdate(@NonNull Activity activity, @NonNull ResourceResolver resolver);
}
