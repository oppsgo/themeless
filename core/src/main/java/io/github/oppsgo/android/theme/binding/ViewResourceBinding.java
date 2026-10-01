package io.github.oppsgo.android.theme.binding;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleableRes;

import java.util.Arrays;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.resolver.ContextResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * Binding 基础设施与 View 级属性（{@code background} / {@code backgroundTint}）。
 * 更具体的能力由各子类声明。
 * <p>
 * 不带 View 泛型；子类用协变 {@link #getView()} 暴露真实类型。
 * setter 以 {@link ResourceRef} 为单入口；{@code int} 重载只作资源 id 便捷包装。
 */
public class ViewResourceBinding implements ResourceBinding {

    public static final int ATTR_BACKGROUND = android.R.attr.background;
    public static final int ATTR_BACKGROUND_TINT = android.R.attr.backgroundTint;

    private static final SparseBooleanArray COLOR_RES_CACHE = new SparseBooleanArray();

    @NonNull
    protected final View view;

    /**
     * 同一张表同时服务有 attr 和没有 attr 的 View。
     * 没有 {@code R.attr} 时，用本类私有常量当 key，不要占用已声明的 {@code ATTR_*}。
     */
    protected final SparseArray<ResourceRef<?>> attributes = new SparseArray<>();

    private boolean enable;
    protected volatile int modCount;
    /**
     * 临时 Binding 在未 apply 时复用的 Context Resolver，避免每次 setXxx 都 new。
     */
    @Nullable
    private transient volatile ResourceResolver fallbackResolver;

    public ViewResourceBinding(@NonNull View view) {
        this.view = view;
        this.enable = true;
    }

    /**
     * View 上已有 {@code type} 或它的子类就返回（含用户子类 / 代理子类）。
     * 没有：用 {@code factory} 造临时实例（不挂 tag）。
     */
    @NonNull
    protected static <V extends View, T extends ResourceBinding> T of(
            @NonNull V view,
            @NonNull Class<T> type,
            @NonNull Factory<V, T> factory
    ) {
        T existing = ThemeManager.get().find(view, type);
        if (existing != null) {
            return existing;
        }
        return factory.create(view);
    }

    protected interface Factory<V extends View, T extends ResourceBinding> {
        @NonNull
        T create(@NonNull V view);
    }

    /**
     * 已有本类或子类就返回；否则给一个不挂到 View 上的实例。
     */
    @NonNull
    public static ViewResourceBinding of(@NonNull View view) {
        return of(view, ViewResourceBinding.class, ViewResourceBinding::new);
    }

    /**
     * 挂到 View：已挂载则幂等；有其它 Binding 则先 {@link #detach} 再挂本实例并 {@link #onAttached()}。
     */
    @NonNull
    @Override
    public ViewResourceBinding attach() {
        if (isAttached()) {
            return this;
        }
        ResourceBinding existing = ThemeManager.get().find(view);
        if (existing != null) {
            existing.detach();
        }
        view.setTag(ResourceBinding.TAG_BINDING, this);
        onAttached();
        return this;
    }

    /**
     * 卸下：{@link #onDetached()} 后清 tag。未挂载时为空操作。
     */
    @NonNull
    @Override
    public ViewResourceBinding detach() {
        if (!isAttached()) {
            return this;
        }
        onDetached();
        view.setTag(ResourceBinding.TAG_BINDING, null);
        return this;
    }

    /** 挂载成功后调用；容器 Binding 在此注册监听。 */
    protected void onAttached() {
    }

    /** 卸载前调用；容器 Binding 在此移除监听。 */
    protected void onDetached() {
    }

    @Override
    @NonNull
    public View getView() {
        return view;
    }

    @Override
    public int getModCount() {
        return modCount;
    }

    /**
     * 子类声明要跟踪的属性。{@code obtainStyledAttributes} 要求数组升序，这里会排序。
     */
    @StyleableRes
    protected int[] getViewStyleable() {
        return new int[]{
                ATTR_BACKGROUND,
                ATTR_BACKGROUND_TINT,
        };
    }

    /**
     * 把布局或手动设置的 attr 收成 Binding 内部使用的 key。
     * AppCompat 把 {@code app:srcCompat} 等别名归一成对应 Binding 的平台 {@code ATTR_*}。
     */
    @AttrRes
    protected int normalizeAttr(@AttrRes int attr) {
        return attr;
    }

    @NonNull
    protected static int[] mergeStyleable(@NonNull int[] base, @NonNull int... extra) {
        int[] merged = Arrays.copyOf(base, base.length + extra.length);
        System.arraycopy(extra, 0, merged, base.length, extra.length);
        return merged;
    }

    @NonNull
    @Override
    public ViewResourceBinding bind(@Nullable AttributeSet set) {
        if (set == null) return this;
        int[] attrs = getViewStyleable();
        Arrays.sort(attrs);

        try (TypedArray ta = view.getContext().obtainStyledAttributes(set, attrs)) {
            for (int i = 0; i < attrs.length; i++) {
                int resId = ta.getResourceId(i, ID_NULL);
                if (resId == ID_NULL) continue;
                int raw = attrs[i];
                int attr = normalizeAttr(raw);
                // 别名和平台 attr 都有值时，别名覆盖。
                if (raw == attr && attributes.indexOfKey(attr) >= 0) {
                    continue;
                }
                putAttr(attr, createResource(attr, resId));
            }
        }
        return this;
    }

    /**
     * 把布局里读到的资源 id 收成 {@link ResourceRef}。子类按自己的属性补充分支。
     */
    @Nullable
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (resId == ID_NULL) return null;
        if (attr == ATTR_BACKGROUND) {
            return isPureColor(resId) ? ColorRef.of(resId) : DrawableRef.of(resId);
        }
        if (attr == ATTR_BACKGROUND_TINT) {
            return createColorResource(resId);
        }
        return null;
    }

    @Nullable
    protected ResourceRef<?> createColorResource(@AnyRes int resId) {
        if (resId == ID_NULL) return null;
        return isPureColor(resId) ? ColorRef.of(resId) : ColorStateListRef.of(resId);
    }

    @NonNull
    @Override
    public ViewResourceBinding bind(@AttrRes int attr, @NonNull ResourceRef<?> value) {
        putAttr(attr, value);
        return this;
    }

    @NonNull
    @Override
    public ViewResourceBinding unbind(@AttrRes int attr) {
        attributes.delete(normalizeAttr(attr));
        return this;
    }

    protected void putAttr(@AttrRes int attr, @Nullable ResourceRef<?> value) {
        attr = normalizeAttr(attr);
        if (value == null || value.isEmpty()) {
            attributes.delete(attr);
            return;
        }
        attributes.put(attr, value);
    }

    protected void putAndUpdate(@AttrRes int attr, @Nullable ResourceRef<?> value) {
        putAttr(attr, value);
        ResourceResolver resolver = currentResolver();
        if (resolver != null) {
            updateAttribute(resolver, attr, attributes.get(attr));
        }
    }

    /**
     * 已 {@link ThemeManager#apply(Context, ResourceResolver)} 时用安装好的 Resolver。
     * 临时 Binding 且尚未 apply 时，懒缓存一份基于 View Context 的 Resolver，供 setXxx 立刻取值。
     */
    @Nullable
    protected ResourceResolver currentResolver() {
        ThemeManager manager = ThemeManager.get();
        ResourceResolver resolver = manager.getResolver(view.getContext());
        if (resolver != null) {
            return resolver;
        }
        if (isAttached()) {
            return null;
        }
        if (fallbackResolver == null) {
            synchronized (this) {
                if (fallbackResolver == null) {
                    fallbackResolver = new ContextResourceResolver(view.getContext());
                }
            }
        }
        return fallbackResolver;
    }

    @NonNull
    @Override
    public ViewResourceBinding setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void refresh() {
        int current = ThemeManager.get().getModCount(view.getContext());
        if (current != ThemeManager.MOD_COUNT_NONE && this.modCount == current) {
            return;
        }
        ResourceResolver resolver = ThemeManager.get().getResolver(view.getContext());
        if (resolver != null) {
            apply(resolver);
        }
    }

    /**
     * 记下当前刷新代数再刷。未启用时不记代数，避免列表复用时把“没刷过”当成已经最新。
     * 换肤路径应始终走到这里，不做代数跳过。
     */
    @Override
    public void apply(@NonNull ResourceResolver resolver) {
        if (!isEnable()) return;
        int modCount = ThemeManager.get().getModCount(view.getContext());
        if (modCount != ThemeManager.MOD_COUNT_NONE) {
            this.modCount = modCount;
        }
        invalidate(resolver);
    }

    protected void invalidate(@NonNull ResourceResolver resolver) {
        int size = attributes.size();
        for (int i = 0; i < size; i++) {
            updateAttribute(resolver, attributes.keyAt(i), attributes.valueAt(i));
        }
    }

    /**
     * 按 attr 把已解析的 {@link ResourceRef} 写回 View。子类按自己的属性补充分支。
     */
    protected void updateAttribute(
            @NonNull ResourceResolver resolver,
            @AttrRes int attr,
            @Nullable ResourceRef<?> value
    ) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_BACKGROUND) {
            if (value instanceof ColorRef) {
                Integer color = ((ColorRef) value).resolve(resolver);
                if (color != null) {
                    view.setBackgroundColor(color);
                }
            } else if (value instanceof DrawableRef) {
                view.setBackground(((DrawableRef) value).resolve(resolver));
            }
            return;
        }
        if (attr == ATTR_BACKGROUND_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            resolver.getViewCompat().setBackgroundTintList(view, tint);
        }
    }

    @Nullable
    protected ColorStateList resolveTint(@NonNull ResourceResolver resolver, @Nullable ResourceRef<?> value) {
        if (value instanceof ColorRef) {
            Integer color = ((ColorRef) value).resolve(resolver);
            return color == null ? null : ColorStateList.valueOf(color);
        }
        if (value instanceof ColorStateListRef) {
            return ((ColorStateListRef) value).resolve(resolver);
        }
        return null;
    }

    @Nullable
    protected Drawable resolveDrawable(@NonNull ResourceResolver resolver, @AttrRes int attr) {
        ResourceRef<?> value = attributes.get(attr);
        if (!(value instanceof DrawableRef)) return null;
        return ((DrawableRef) value).resolve(resolver);
    }

    /**
     * 资源既可以是颜色也可以是图片时，靠 {@link TypedValue} 的类型区分。
     */
    public boolean isPureColor(@ColorRes int id) {
        if (id == ID_NULL) return false;
        // 命中缓存直接返回
        int index = COLOR_RES_CACHE.indexOfKey(id);
        if (index >= 0) {
            return COLOR_RES_CACHE.valueAt(index);
        }

        Resources resources = view.getContext().getResources();
        try {
            TypedValue tv = new TypedValue();
            // 首次查询：调用 Resources.getValue 解析资源表
            resources.getValue(id, tv, true);
            // 写入缓存 (id -> isColor 的映射在 App 生命周期内不变)
            boolean isColor = tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT;
            COLOR_RES_CACHE.put(id, isColor);
            return isColor;
        } catch (Resources.NotFoundException ignored) {

        }

        // id 无效或不是资源 ID，视为非纯颜色
        COLOR_RES_CACHE.put(id, false);
        return false;
    }

    @NonNull
    public ViewResourceBinding setBackground(ResourceRef<?> background) {
        putAndUpdate(ATTR_BACKGROUND, background);
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackground(@AnyRes int background) {
        if (background == ID_NULL) {
            unbind(ATTR_BACKGROUND);
            return this;
        }
        return setBackground(isPureColor(background) ? ColorRef.of(background) : DrawableRef.of(background));
    }

    @NonNull
    public ViewResourceBinding setBackgroundTint(ResourceRef<?> tint) {
        putAndUpdate(ATTR_BACKGROUND_TINT, tint);
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackgroundTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_BACKGROUND_TINT);
            return this;
        }
        return setBackgroundTint(createColorResource(tint));
    }
}
