package io.github.oppsgo.android.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.theme.core.R;

/**
 * 一个 View 上的主题绑定。记下要随主题变化的属性，刷新时再写回 View。
 * <p>
 * 实例通常挂在 View 的 tag 上，由 {@link ResourceBindingFactory} 创建；
 * 也可自行实现本接口（含代理包装），不必继承 {@code BaseViewResourceBinding}。
 *
 * @param <V> 绑定的 View 类型
 */
public interface ResourceBinding<V extends View> {

    /**
     * 无效资源 id（值为 {@code 0}）。
     * 语义同系统 {@code Resources.ID_NULL}，自建常量以避免 API 29+ 告警。
     */
    int ID_NULL = 0;

    int TAG_BINDING = R.id.theme_attribute_binding_tag;

    @NonNull
    V getView();

    /**
     * 从布局 AttributeSet 记下资源引用，不立刻刷到 View。
     */
    @NonNull
    ResourceBinding<V> bind(@Nullable AttributeSet attrs);

    /**
     * 手动绑定。key 不要求是 {@code R.attr}，自定义 View 可以用 Binding 内部常量。
     * 值的类型由 {@link io.github.oppsgo.android.theme.resource.ResourceRef} 的子类约束。
     */
    @NonNull
    ResourceBinding<V> bind(@AttrRes int attr, @NonNull ResourceRef<?> ref);

    @NonNull
    ResourceBinding<V> unbind(@AttrRes int attr);

    /**
     * 关闭后 {@link #refresh()} 和 {@link #apply} 都不再改这个 View。
     */
    @NonNull
    ResourceBinding<V> setEnable(boolean enable);

    boolean isEnable();

    /**
     * 用给定的 {@link ResourceResolver} 把已绑定的资源刷到 View。
     */
    void apply(@NonNull ResourceResolver resolver);

    /**
     * 用当前已安装的 {@link ResourceResolver} 刷新。还没 {@link ThemeManager#apply(Context, ResourceResolver)} 时什么都不做。
     */
    void refresh();

    /**
     * 最近一次 {@link #apply} 时 {@link ThemeManager} 的刷新代数，供列表复用判断要不要再刷。
     */
    int getModCount();

    /**
     * 是否已挂在 View 的 tag 上（{@link ThemeManager#obtain} / inflate）。
     */
    default boolean isAttached() {
        return ThemeManager.get().find(getView()) == this;
    }

    /**
     * 确保拿到「挂在 View 上」的那份 Binding。
     * <ul>
     *   <li>已是本实例 → 返回 this</li>
     *   <li>View 上已有别的 Binding（inflate / obtain / 用户自写 / 代理）→ <b>绝不覆盖</b>，返回已挂载的那份</li>
     *   <li>尚无 → 把 this 挂上去</li>
     * </ul>
     * 日常请优先 {@link ThemeManager#edit(View)} / 各 Binding 的 {@code of()}；不要先 {@code new} 再 attach。
     */
    @NonNull
    @SuppressWarnings("unchecked")
    default ResourceBinding<? extends V> attach() {
        V view = getView();
        ResourceBinding<?> existing = ThemeManager.get().find(view);
        if (existing == this) {
            return this;
        }
        if (existing != null) {
            return (ResourceBinding<? extends V>) existing;
        }
        view.setTag(ResourceBinding.TAG_BINDING, this);
        return this;
    }
}
