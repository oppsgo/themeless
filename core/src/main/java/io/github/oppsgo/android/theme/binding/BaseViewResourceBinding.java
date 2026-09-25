package io.github.oppsgo.android.theme.binding;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseArray;
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
 * 把属性 key 映射到 {@link ResourceRef}。
 * 各 Binding 自己声明 {@code ATTR_*}，AppCompat 别名经 {@link #normalizeAttr} 收成平台 attr。
 * 布局解析和手动 setXxx 写进同一张表，刷新时再统一取出来设回 View。
 */
public abstract class BaseViewResourceBinding<VIEW extends View> implements ResourceBinding<VIEW> {

    public static final int ATTR_BACKGROUND = android.R.attr.background;
    public static final int ATTR_BACKGROUND_TINT = android.R.attr.backgroundTint;

    @NonNull
    protected final VIEW view;

    /**
     * 同一张表同时服务有 attr 和没有 attr 的 View。
     * 没有 {@code R.attr} 时，用本类私有常量当 key，不要占用已声明的 {@code ATTR_*}。
     */
    protected final SparseArray<ResourceRef<?>> attributes = new SparseArray<>();

    private boolean enable;
    protected transient int modCount;
    /** 临时 Binding 在未 apply 时复用的 Context Resolver，避免每次 setXxx 都 new。 */
    @Nullable
    private transient ResourceResolver fallbackResolver;

    public BaseViewResourceBinding(@NonNull VIEW view) {
        this.view = view;
        this.enable = true;
    }

    /**
     * View 上已有 {@code type} 或它的子类就返回（含用户子类 / 代理子类）。
     * 没有：用 {@code factory} 造临时实例（不挂 tag）。
     * {@code T} 约束在 {@link ResourceBinding}，不强制 {@code BaseViewResourceBinding}，
     * 这样自写实现只要类型匹配也能被 {@code of()} 复用。
     * {@code ? super V}：AppCompatImageView 可挂在 {@code ImageView} Binding 子类上。
     */
    @NonNull
    protected static <V extends View, T extends ResourceBinding<? super V>> T of(
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

    protected interface Factory<V extends View, T extends ResourceBinding<? super V>> {
        @NonNull
        T create(@NonNull V view);
    }

    @Override
    @NonNull
    public VIEW getView() {
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
    public BaseViewResourceBinding<VIEW> bind(@Nullable AttributeSet set) {
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
    public BaseViewResourceBinding<VIEW> bind(@AttrRes int attr, @NonNull ResourceRef<?> value) {
        putAttr(attr, value);
        return this;
    }

    @NonNull
    @Override
    public BaseViewResourceBinding<VIEW> unbind(@AttrRes int attr) {
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
        attr = normalizeAttr(attr);
        putAttr(attr, value);
        ResourceResolver resolver = currentResolver();
        if (resolver != null) {
            updateAttribute(resolver, attr, attributes.get(attr));
        }
    }

    /**
     * 手动指定 background 资源，写入 Binding 供后续 apply 使用。
     */
    @NonNull
    public BaseViewResourceBinding<VIEW> setBackground(@AnyRes int background) {
        if (background == ID_NULL) {
            unbind(ATTR_BACKGROUND);
            return this;
        }
        if (isPureColor(background)) {
            return setBackground(ColorRef.of(background));
        }
        return setBackground(DrawableRef.of(background));
    }

    @NonNull
    public BaseViewResourceBinding<VIEW> setBackground(@NonNull ColorRef background) {
        putAndUpdate(ATTR_BACKGROUND, background);
        return this;
    }

    @NonNull
    public BaseViewResourceBinding<VIEW> setBackground(@NonNull DrawableRef background) {
        putAndUpdate(ATTR_BACKGROUND, background);
        return this;
    }

    @NonNull
    public BaseViewResourceBinding<VIEW> setBackgroundTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_BACKGROUND_TINT);
            return this;
        }
        putAndUpdate(ATTR_BACKGROUND_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public BaseViewResourceBinding<VIEW> setBackgroundTint(@NonNull ColorRef tint) {
        putAndUpdate(ATTR_BACKGROUND_TINT, tint);
        return this;
    }

    @NonNull
    public BaseViewResourceBinding<VIEW> setBackgroundTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(ATTR_BACKGROUND_TINT, tint);
        return this;
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
            fallbackResolver = new ContextResourceResolver(view.getContext());
        }
        return fallbackResolver;
    }

    @NonNull
    @Override
    public BaseViewResourceBinding<VIEW> setEnable(boolean enable) {
        this.enable = enable;
        return this;
    }

    @Override
    public boolean isEnable() {
        return enable;
    }

    @Override
    public void refresh() {
        ResourceResolver resolver = ThemeManager.get().getResolver(view.getContext());
        if (resolver != null) {
            apply(resolver);
        }
    }

    /**
     * 记下当前刷新代数再刷。未启用时不记代数，避免列表复用时把“没刷过”当成已经最新。
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

    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr, @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_BACKGROUND) {
            if (value instanceof ColorRef) {
                Integer color = ((ColorRef) value).resolve(resolver);
                if (color != null) view.setBackgroundColor(color);
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
     * background 既可以是颜色也可以是图片，靠 {@link TypedValue} 的类型区分。
     */
    public boolean isPureColor(@ColorRes int id) {
        if (id == ID_NULL) return false;
        Resources resources = view.getContext().getResources();
        TypedValue tv = new TypedValue();
        resources.getValue(id, tv, true);
        return tv.type >= TypedValue.TYPE_FIRST_COLOR_INT && tv.type <= TypedValue.TYPE_LAST_COLOR_INT;
    }
}
