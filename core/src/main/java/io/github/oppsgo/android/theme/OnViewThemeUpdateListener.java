package io.github.oppsgo.android.theme;

import android.view.View;

import androidx.annotation.NonNull;

/**
 * 单个 View 主题刷新完成的回调。
 * 通过 {@link IViewBinding#setOnViewThemeUpdateListener} 设置，一个 Binding 只挂一个，
 * 后设覆盖先设；需要多方接收时在回调里自行转发。
 */
public interface OnViewThemeUpdateListener {

    /**
     * 该 View 按新资源刷完之后调用。在发起刷新的线程同步触发，一般是主线程。
     */
    void onThemeUpdate(@NonNull View view, @NonNull ResourceResolver resolver);
}
