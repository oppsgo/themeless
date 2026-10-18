package io.github.oppsgo.android.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.binding.ViewBinding;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * 一个 View 上的主题绑定。记下要随主题变化的属性，刷新时再写回 View。
 * <p>
 * 实例挂在 View 的 tag 上，由 {@link ThemeBindingFactory} 创建、{@link ThemeManager#of(View)} 取用；
 * 也可自行实现本接口（含代理包装），不必继承 {@link ViewBinding}。
 * <p>
 * 不带 View 泛型：具体 Binding 用协变 {@link #getView()} 暴露真实类型
 * （如 {@code SwitchBinding#getView()} 返回 {@code Switch}），避免继承链把类型收窄。
 * <p>
 * 仅主线程使用，契约见 {@link ThemeManager}。
 */
@MainThread
public interface IViewBinding {

    @NonNull
    View getView();

    /**
     * 从布局 AttributeSet 记下资源引用，不立刻刷到 View。
     * 只读已登记（带 {@link ResourceFactory}）的属性；布局里有值的以 XML 为准，
     * 包括覆盖此前手动 {@link #bind(int, ResourceRef, AttrSetter)} 绑过的值
     * （手动绑定不清掉 {@code factory} 登记，见下）。
     */
    void bind(@Nullable AttributeSet attrs);

    /**
     * 声明式登记一个属性：刷新时用 {@code factory} 按资源 id 造 {@link ResourceRef}，
     * 再把解析出的值交给 {@code setter} 写回 View。自定义 View 接入用它，一行一个属性。
     */
    <T> void bind(@AttrRes int attr, @NonNull ResourceFactory<T> factory, @NonNull AttrSetter<T> setter);

    /**
     * 手动绑定一个值并立刻写回 View：是对登记的**扩展**，只替换写回 {@code setter} 与当前值，
     * 登记里的 {@code factory} 保留；之后再次 {@link #bind(AttributeSet)} 读布局时以 XML 值为准。
     */
    <T> void bind(@AttrRes int attr, @NonNull ResourceRef<T> ref, @NonNull AttrSetter<T> setter);

    /**
     * 清掉该属性当前的值（登记保留）。没有登记过返回 {@code false}。
     */
    void unbind(@AttrRes int attr);

    /**
     * 关闭后 {@link #apply} 不再改这个 View。
     * 布局可用 {@code app:themeViewAttrEnable}（由
     * {@link ViewBinding#bind} 解析）。
     */
    void setEnabled(boolean enabled);

    boolean isEnabled();

    /**
     * 用给定的 {@link ResourceResolver} 把已绑定的资源刷到 View。
     * ViewGroup 会把同一套 resolver 传给已绑定的直接子 View。
     */
    void apply(ResourceResolver resolver);

    /**
     * 设置该 View 每次主题刷新完成的回调；{@code null} 清除。
     * 单回调：后设覆盖先设，需要多方接收时在回调里自行转发。
     */
    void setOnViewThemeUpdateListener(@Nullable OnViewThemeUpdateListener listener);

    /**
     * 按需刷新：若当前 {@link ThemeManager#getModCount(Context)}与本 Binding 上次 apply 时记录的一致，
     * 说明资源未变，直接跳过；否则用当前安装的 Resolver 刷一遍。
     * 用于 View 重新挂载（如 RecyclerView 复用）时的补刷，避免每次都全量重写。
     */
    void refresh();

    /**
     * 把解析出的值写回 View。
     */
    @FunctionalInterface
    interface AttrSetter<T> {
        void set(T value);
    }

    /**
     * 刷新时按资源 id 造 {@link ResourceRef}。
     */
    @FunctionalInterface
    interface ResourceFactory<T> {
        @Nullable
        ResourceRef<? extends T> create(int resourceId);
    }
}
