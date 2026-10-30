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
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * 一个 View 上的主题绑定。记下要随主题变化的属性，刷新时再写回 View。
 * <p>
 * 参与换肤的实例由 LayoutInflater（{@link ThemeDelegate}）挂到 View tag；
 * {@link ThemeManager#of(View)} 可临时创建且不挂 tag（{@code setXxx} 只靠 fallback 写一次）。
 * 也可自行实现本接口（含代理包装），不必继承 {@link ViewBinding}。
 * <p>
 * 属性槽位统一用 {@link ThemeKey}：布局 {@code R.attr} 与业务自有 token 同一套
 * {@link #bind} / {@link #set} / {@link #unbind}；{@code @AttrRes int} 重载为糖。
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
     * 只读已登记（带 {@link ResourceFactory}）且为 attr 形态的 {@link ThemeKey}；
     * 布局里有值的以 XML 为准，包括覆盖此前 {@link #set(ThemeKey, ResourceRef)} 写过的值
     * （{@link #bind(ThemeKey, AttrSetter)} 不清掉 {@code factory} 登记，见下）。
     */
    void bind(@Nullable AttributeSet attrs);

    /**
     * 声明式登记：刷新时用 {@code factory} 按资源 id 造 {@link ResourceRef}，
     * 再把解析出的 {@link ResourceValue} 交给 {@code setter} 写回 View。
     * <p>
     * 仅 {@link ThemeKey#isAttr()} 的 key 会被 {@link #bind(AttributeSet)} 读到；
     * token 形态只参与后续 {@link #apply} / {@link #set}。
     */
    <T> void bind(@NonNull ThemeKey key, @NonNull ResourceFactory<T> factory, @NonNull AttrSetter<T> setter);

    /**
     * {@link #bind(ThemeKey, ResourceFactory, AttrSetter)} 的 {@code R.attr} 糖。
     */
    <T> void bind(@AttrRes int attr, @NonNull ResourceFactory<T> factory, @NonNull AttrSetter<T> setter);

    /**
     * 只登记 setter，不写入当前值。再次同 key 则替换 setter 并清掉当前 ref；
     * 已有 {@code factory} 保留（若曾声明式登记过）。
     * 值用 {@link #set(ThemeKey, ResourceRef)} 写入。
     */
    <T> void bind(@NonNull ThemeKey key, @NonNull AttrSetter<T> setter);

    /**
     * {@link #bind(ThemeKey, AttrSetter)} 的 {@code R.attr} 糖。
     */
    <T> void bind(@AttrRes int attr, @NonNull AttrSetter<T> setter);

    /**
     * 只更新已登记属性的 {@link ResourceRef} 并立刻写回。
     * 未登记过该 key → 空操作；{@code ref == null} → 等价 {@link #unbind(ThemeKey)}。
     */
    void set(@NonNull ThemeKey key, @Nullable ResourceRef<?> ref);

    /**
     * {@link #set(ThemeKey, ResourceRef)} 的 {@code R.attr} 糖。
     */
    void set(@AttrRes int attr, @Nullable ResourceRef<?> ref);

    /**
     * 清掉该 key 当前的值（登记保留）。
     */
    void unbind(@NonNull ThemeKey key);

    /**
     * {@link #unbind(ThemeKey)} 的 {@code R.attr} 糖。
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
     * {@link io.github.oppsgo.android.theme.binding.ViewBinding} 实现里：先刷自身属性，
     * 再由 {@code applyDescendants} 级联（ViewGroup / RecyclerView 等各自决定怎么找子 Binding）。
     */
    void apply(ResourceResolver resolver);

    /**
     * 设置该 View 每次主题刷新完成的回调；{@code null} 清除。
     * 单回调：后设覆盖先设，需要多方接收时在回调里自行转发。
     * 只登记，不立刻回调；首屏若也要走同一段逻辑，再调 {@link #notifyViewThemeUpdate}。
     */
    void setOnViewThemeUpdateListener(@Nullable OnViewThemeUpdateListener listener);

    /**
     * 用当前 {@link ResourceResolver} 调一次已登记的回调。
     * 已 {@link ThemeManager#apply} 用安装好的；否则用 View Context 兜底，保证有 listener
     * 且 Binding 开启时一定会进回调。没有回调或已关掉时什么都不做。
     * 不刷绑定属性，不级联子 View，也不动刷新代数。
     */
    void notifyViewThemeUpdate();

    /**
     * 按需刷新：若当前 {@link ThemeManager#getModCount(Context)}与本 Binding 上次 apply 时记录的一致，
     * 说明资源未变，直接跳过；否则用当前安装的 Resolver 刷一遍。
     * 用于 View 重新挂载（如 RecyclerView 复用）时的补刷，避免每次都全量重写。
     */
    void refresh();

    /**
     * 把解析出的 {@link ResourceValue} 写回 View。
     * 仅在 {@link ResourceValue#isPresent()} 时调用；值可为显式 null（{@code of(null)}）。
     * 取不到值（{@link ResourceValue#empty()}）不会回调。
     */
    @FunctionalInterface
    interface AttrSetter<T> {
        void set(@NonNull ResourceValue<T> value);
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
