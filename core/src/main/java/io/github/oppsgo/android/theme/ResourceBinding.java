package io.github.oppsgo.android.theme;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.core.R;

/**
 * 一个 View 上的主题绑定。记下要随主题变化的属性，刷新时再写回 View。
 * <p>
 * 实例通常挂在 View 的 tag 上，由 {@link ResourceBindingFactory} 创建；
 * 也可自行实现本接口（含代理包装），不必继承 {@link io.github.oppsgo.android.theme.binding.ViewResourceBinding}。
 * <p>
 * 不带 View 泛型：具体 Binding 用协变 {@link #getView()} 暴露真实类型
 * （如 {@code SwitchResourceBinding#getView()} 返回 {@code Switch}），避免继承链把类型收窄。
 */
public interface ResourceBinding {

    /**
     * 无效资源 id（值为 {@code 0}）。
     * 语义同系统 {@code Resources.ID_NULL}，自建常量以避免 API 29+ 告警。
     */
    int ID_NULL = 0;

    int TAG_BINDING = R.id.theme_attribute_binding_tag;

    /**
     * {@code app:themeAttributeEnable} → {@link #setEnable(boolean)}
     */
    int ATTR_THEME_ATTRIBUTE_ENABLE = R.attr.themeAttributeEnable;

    @NonNull
    View getView();

    /**
     * 从布局 AttributeSet 记下资源引用，不立刻刷到 View。
     */
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

    /**
     * 关闭后 {@link #refresh()} 和 {@link #apply} 都不再改这个 View。
     * 布局可用 {@code app:themeAttributeEnable}（由 {@link io.github.oppsgo.android.theme.binding.ViewResourceBinding#bind} 解析）。
     */
    @NonNull
    ResourceBinding setEnable(boolean enable);

    boolean isEnable();

    /**
     * 用给定的 {@link ResourceResolver} 把已绑定的资源刷到 View。
     */
    void apply(@NonNull ResourceResolver resolver);

    /**
     * 用当前已安装的 {@link ResourceResolver} 刷新。还没 {@link ThemeManager#apply(Context, ResourceResolver)} 时什么都不做。
     * <p>
     * 若已是本轮 {@link ThemeManager#getModCount} 代数则跳过，供 RecyclerView / ViewPager 复用补刷。
     * 换肤请走 {@link #apply}，不会因代数跳过。
     */
    void refresh();

    /**
     * 最近一次 {@link #apply} 时 {@link ThemeManager} 的刷新代数，供 {@link #refresh()} 判断要不要再刷。
     */
    int getModCount();

    /**
     * 是否已挂在 View 的 tag 上（经 {@link #attach} / {@link ThemeManager#ensureAttach} / inflate）。
     */
    default boolean isAttached() {
        return ThemeManager.get().find(getView()) == this;
    }

    /**
     * 把本实例挂到 View 上（装容器钩子）。
     * <ul>
     *   <li>已是本实例（{@link #isAttached}）→ 不重复挂载，返回 this</li>
     *   <li>View 上已有别的 Binding → 先对其 {@link #detach}，再挂 this</li>
     *   <li>尚无 → 挂上 this</li>
     * </ul>
     * 不替换已有 Binding 时请用 {@link ThemeManager#ensureAttach(View)}。
     * {@link ThemeManager#obtain} 只取/建实例，不会挂载。
     */
    @NonNull
    default ResourceBinding attach() {
        if (isAttached()) {
            return this;
        }
        View view = getView();
        ResourceBinding existing = ThemeManager.get().find(view);
        if (existing != null) {
            existing.detach();
        }
        view.setTag(ResourceBinding.TAG_BINDING, this);
        return this;
    }

    /**
     * 卸下本实例：清 tag。未挂载时为空操作。
     * 库内 {@link io.github.oppsgo.android.theme.binding.ViewResourceBinding} 还会卸容器钩子。
     */
    @NonNull
    default ResourceBinding detach() {
        if (!isAttached()) {
            return this;
        }
        getView().setTag(ResourceBinding.TAG_BINDING, null);
        return this;
    }
}
