package io.github.oppsgo.android.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * 一个 View 上的主题绑定。记下要随主题变化的属性，刷新时再写回 View。
 * 实例挂在 View 的 tag 上，由 {@link ResourceBindingFactory} 创建。
 */
public interface ResourceBinding {

    /**
     * 无效资源 id（值为 {@code 0}）。
     * 语义同系统 {@code Resources.ID_NULL}，自建常量以避免 API 29+ 告警。
     */
    int ID_NULL = 0;

    @NonNull
    View getView();

    /** 从布局 AttributeSet 记下资源引用，不立刻刷到 View。 */
    @NonNull
    ResourceBinding bind(@Nullable AttributeSet attrs);

    /**
     * 手动绑定。key 不要求是 {@code R.attr}，自定义 View 可以用 Binding 内部常量。
     * 值的类型由 {@link io.github.oppsgo.android.theme.resource.ResourceRef} 的子类约束。
     */
    @NonNull
    ResourceBinding bind(@AttrRes int attr, @NonNull ResourceRef<?> ref);

    @NonNull
    ResourceBinding unbind(@AttrRes int attr);

    /** 关闭后 {@link #refresh()} 和 {@link #apply} 都不再改这个 View。 */
    @NonNull
    ResourceBinding setEnable(boolean enable);

    boolean isEnable();

    /**
     * 用给定的 {@link ResourceResolver} 把已绑定的资源刷到 View。
     */
    void apply(@NonNull ResourceResolver resolver);

    /** 用当前已安装的 {@link ResourceResolver} 刷新。还没 {@link ThemeManager#apply(Context, ResourceResolver)} 时什么都不做。 */
    void refresh();

    /** 最近一次 {@link #apply} 时 {@link ThemeManager} 的刷新代数，供列表复用判断要不要再刷。 */
    int getModCount();
}
