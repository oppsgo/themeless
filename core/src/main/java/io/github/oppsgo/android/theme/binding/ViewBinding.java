package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import io.github.oppsgo.android.theme.IViewBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.core.R;
import io.github.oppsgo.android.theme.resolver.ContextResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * Binding 基础设施与 View 级属性（{@code background} / {@code backgroundTint}）。
 * 更具体的能力由各子类声明。
 * <p>
 * 属性在构造器里一行一个地登记：{@link #bindRaw}（带 setter 的常规属性）、
 * {@link #bindSpecial}（需整组写入等特殊处理、由 {@link #applySpecialAttrs} 统一刷的属性）。
 * 布局 XML 要读哪些属性也从登记结果推导，不再单独声明 styleable。
 * <p>
 * 不带 View 泛型；子类用协变 {@link #getView()} 暴露真实类型。
 * setter 以具体 {@link ResourceRef} 子类为单入口；{@code int} 重载只作资源 id 便捷包装。
 */
public class ViewBinding implements IViewBinding {

    public static final int ATTR_BACKGROUND = android.R.attr.background;
    public static final int ATTR_BACKGROUND_TINT = android.R.attr.backgroundTint;

    /**
     * {@code app:themeViewAttrEnable} → {@link #setEnabled(boolean)}
     */
    private static final int ATTR_ENABLED = R.attr.themeViewAttrEnable;

    @NonNull
    protected final View view;

    /**
     * 同一张表同时服务有 attr 和没有 attr 的 View；key 是归一化（别名展开）后的 attr。
     */
    private final SparseArray<ViewAttr> attrs = new SparseArray<>();

    /**
     * 别名 attr → 平台 attr。布局里两者都有值时，别名覆盖。
     */
    private final SparseIntArray aliases = new SparseIntArray();

    private boolean enabled = true;

    /**
     * 临时 Binding 在未 apply 时复用的 Context Resolver，避免每次 setXxx 都 new。
     */
    @Nullable
    private transient volatile ResourceResolver fallbackResolver;

    public ViewBinding(@NonNull View view) {
        this.view = view;
        bindRaw(ATTR_BACKGROUND, resourceId -> ResourceRef.fromColorOrDrawable(resourceTable(), resourceId), this::applyBackground);
        bindRaw(ATTR_BACKGROUND_TINT, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId), this::applyBackgroundTint);
    }

    /**
     * 把解析值写回 View 的单参函数。
     */
    protected interface AttributeApplier {
        void apply(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref);
    }

    /**
     * 一个被跟踪属性的全部状态。
     */
    private static final class ViewAttr {
        final int attr;
        final AttributeApplier applier;
        @Nullable
        IViewBinding.ResourceFactory<?> factory;


        @Nullable
        ResourceRef<?> ref;

        ViewAttr(int attr, AttributeApplier applier) {
            this.attr = attr;
            this.applier = applier;
        }
    }

    @Override
    @NonNull
    public View getView() {
        return view;
    }

    // ---- 属性登记 ----

    /**
     * 声明式登记常规属性：布局 XML 可读，刷新时经 {@code applier} 写回。
     */
    protected final void bindRaw(
            @AttrRes int attr,
            @NonNull IViewBinding.ResourceFactory<?> factory,
            @NonNull AttributeApplier applier
    ) {
        define(attr, factory, applier).ref = null;
    }

    /**
     * 登记特殊属性：只记 factory 供布局 XML 读取，刷新统一走 {@link #applySpecialAttrs}
     * （如 compound drawable 整组写入）。
     */
    protected final void bindSpecial(@AttrRes int attr, @NonNull IViewBinding.ResourceFactory<?> factory) {
        define(attr, factory, null).ref = null;
    }

    /**
     * 把 {@code aliasAttr} 归一到已登记的 {@code canonicalAttr}（如 {@code app:srcCompat} → {@code src}）。
     */
    public final void alias(@AttrRes int aliasAttr, @AttrRes int canonicalAttr) {
        if (attrs.indexOfKey(canonicalAttr) < 0) {
            throw new IllegalArgumentException("Canonical attribute is not bound: 0x" + Integer.toHexString(canonicalAttr));
        }
        aliases.put(aliasAttr, canonicalAttr);
    }

    @NonNull
    private ViewAttr define(
            @AttrRes int attr,
            @Nullable IViewBinding.ResourceFactory<?> factory,
            @Nullable AttributeApplier applier
    ) {
        int canonical = canonicalAttr(attr);
        ViewAttr target = attrs.get(canonical);
        if (target == null) {
            target = new ViewAttr(canonical, applier);
            attrs.put(canonical, target);
        }
        if (factory != null) {
            target.factory = factory;
        }
        return target;
    }

    @Nullable
    private ViewAttr findAttr(@AttrRes int attr) {
        return attrs.get(canonicalAttr(attr));
    }

    @AttrRes
    private int canonicalAttr(@AttrRes int attr) {
        return aliases.get(attr, attr);
    }

    // ---- IViewBinding ----

    @Override
    public void bind(@Nullable AttributeSet set) {
        if (set == null) return;
        bindEnabled(set);

        int[] xmlAttrs = getXmlAttrs();
        try (TypedArray ta = view.getContext().obtainStyledAttributes(set, xmlAttrs)) {
            readXmlAttrs(ta, xmlAttrs, false);
            readXmlAttrs(ta, xmlAttrs, true);
        }
    }

    /**
     * 读 {@link #ATTR_ENABLED}；未写出则保持当前 {@link #isEnabled()}（默认 true）。
     */
    private void bindEnabled(@NonNull AttributeSet set) {
        try (TypedArray ta = view.getContext().obtainStyledAttributes(set, new int[]{ATTR_ENABLED})) {
            if (ta.hasValue(0)) {
                setEnabled(ta.getBoolean(0, true));
            }
        }
    }

    /**
     * 第一遍读平台 attr，第二遍只读别名（别名覆盖）。只填带 factory 的登记。
     */
    private void readXmlAttrs(@NonNull TypedArray ta, @NonNull int[] xmlAttrs, boolean aliasesOnly) {
        for (int i = 0; i < xmlAttrs.length; i++) {
            int raw = xmlAttrs[i];
            if ((aliases.indexOfKey(raw) >= 0) != aliasesOnly) continue;
            int resId = ta.getResourceId(i, ResourceRef.ID_NULL);
            ViewAttr attr = resId == ResourceRef.ID_NULL ? null : findAttr(raw);
            if (attr != null && attr.factory != null) {
                attr.ref = normalize(attr.factory.create(resId));
            }
        }
    }

    /**
     * 布局要读的 attr：登记的 + 别名，升序（{@code obtainStyledAttributes} 要求）。
     */
    @NonNull
    private int[] getXmlAttrs() {
        int[] xmlAttrs = new int[attrs.size() + aliases.size()];
        int index = 0;
        for (int i = 0; i < attrs.size(); i++) {
            xmlAttrs[index++] = attrs.keyAt(i);
        }
        for (int i = 0; i < aliases.size(); i++) {
            xmlAttrs[index++] = aliases.keyAt(i);
        }
        Arrays.sort(xmlAttrs);
        return xmlAttrs;
    }

    @Override
    public final <T> void bind(
            @AttrRes int attr,
            @NonNull IViewBinding.ResourceFactory<T> factory,
            @NonNull IViewBinding.AttrSetter<T> setter
    ) {
        define(attr, factory, providerSetter(setter)).ref = null;
    }

    @Override
    public final <T> void bind(
            @AttrRes int attr,
            @NonNull ResourceRef<T> ref,
            @NonNull IViewBinding.AttrSetter<T> setter
    ) {
        ViewAttr target = define(attr, null, providerSetter(setter));
        target.ref = normalize(ref);
        applyNow(target);
    }

    @Override
    public boolean unbind(@AttrRes int attr) {
        ViewAttr target = findAttr(attr);
        if (target == null) {
            return false;
        }
        target.ref = null;
        return true;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void apply(@NonNull ResourceResolver resolver) {
        if (!enabled) return;
        applySpecialAttrs(resolver);
        for (int i = 0; i < attrs.size(); i++) {
            applyAttr(resolver, attrs.valueAt(i));
        }
        applyChildren(resolver);
    }

    /**
     * 特殊属性（{@link #bindSpecial}）的统一刷入口；默认没有。
     */
    protected void applySpecialAttrs(@NonNull ResourceResolver resolver) {
    }

    /**
     * 把同一套 resolver 传给已绑定的直接子 View；更深的子树由子 View 自己的 apply 继续传。
     * 开放成 protected 是给需要控制下探深度的容器 Binding（ViewGroupThemeBinding）。
     */
    protected void applyChildren(@NonNull ResourceResolver resolver) {
        if (!(view instanceof ViewGroup)) return;
        ViewGroup group = (ViewGroup) view;
        ThemeManager manager = ThemeManager.get();
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            IViewBinding child = manager.find(group.getChildAt(i));
            if (child != null) {
                child.apply(resolver);
            }
        }
    }

    // ---- 手动 setXxx 的内部通路 ----

    /**
     * 记值，不立刻刷。
     */
    protected final void setAttr(@AttrRes int attr, @Nullable ResourceRef<?> ref) {
        ViewAttr target = findAttr(attr);
        if (target != null) {
            target.ref = normalize(ref);
        }
    }

    /**
     * 记值并立刻写回 View。
     */
    protected final void setAttrAndApply(@AttrRes int attr, @Nullable ResourceRef<?> ref) {
        ViewAttr target = findAttr(attr);
        if (target == null) return;
        target.ref = normalize(ref);
        applyNow(target);
    }

    /**
     * 按资源 id 造值再立刻写回。
     */
    protected final void setAttrResource(
            @AttrRes int attr,
            int resourceId,
            @NonNull IViewBinding.ResourceFactory<?> factory
    ) {
        setAttrAndApply(attr, factory.create(resourceId));
    }

    @Nullable
    protected final ResourceRef<?> getAttrValue(@AttrRes int attr) {
        ViewAttr target = findAttr(attr);
        return target == null ? null : target.ref;
    }

    private void applyNow(@NonNull ViewAttr attr) {
        if (!enabled || attr.ref == null) return;
        applyAttr(currentResolver(), attr);
    }

    private void applyAttr(@NonNull ResourceResolver resolver, @NonNull ViewAttr attr) {
        if (attr.applier != null && attr.ref != null) {
            attr.applier.apply(resolver, attr.ref);
        }
    }

    @NonNull
    private static <T> AttributeApplier providerSetter(@NonNull IViewBinding.AttrSetter<T> setter) {
        return (resolver, ref) -> setter.set(provideValue(resolver, ref));
    }

    @Nullable
    private static <T> T provideValue(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        @SuppressWarnings("unchecked")
        T value = (T) ref.provide(resolver);
        return value;
    }

    @Nullable
    private static ResourceRef<?> normalize(@Nullable ResourceRef<?> ref) {
        if (ref == null || ref.isEmpty()) {
            return null;
        }
        return ref;
    }

    // ---- Resolver / 资源表 ----

    /**
     * 当前资源表：已 {@link ThemeManager#apply(Context, ResourceResolver)} 用它的，否则用 View Context 的。
     */
    @NonNull
    protected final Resources resourceTable() {
        return currentResolver().getResources();
    }

    /**
     * 已 {@link ThemeManager#apply(Context, ResourceResolver)} 时用安装好的 Resolver；
     * 否则懒缓存一份基于 View Context 的 Resolver，供 setXxx 立刻取值。
     */
    @Nullable
    protected ResourceResolver currentResolver() {
        ResourceResolver resolver = ThemeManager.get().getResolver(view.getContext());
        if (resolver != null) {
            return resolver;
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

    // ---- 挂载钩子 ----

    /**
     * 挂载成功后由 {@link ThemeBindingFactory} 调用；容器 Binding 在此注册监听。
     * 仅供库内回调，业务不要调用。
     */
    public void onAttached() {
    }

    /**
     * 卸载前由 {@link ThemeBindingFactory} 调用；容器 Binding 在此移除监听。
     * 仅供库内回调，业务不要调用。
     */
    public void onDetached() {
    }

    // ---- View 级属性 ----

    public void setBackground(@NonNull ColorRef background) {
        setAttrAndApply(ATTR_BACKGROUND, background);
    }

    public void setBackground(@NonNull DrawableRef background) {
        setAttrAndApply(ATTR_BACKGROUND, background);
    }

    public void setBackground(@AnyRes int background) {
        setAttrResource(ATTR_BACKGROUND, background,
                resourceId -> ResourceRef.fromColorOrDrawable(resourceTable(), resourceId));
    }

    public void setBackgroundTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_BACKGROUND_TINT, tint);
    }

    public void setBackgroundTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_BACKGROUND_TINT, tint);
    }

    public void setBackgroundTint(@ColorRes int tint) {
        setAttrResource(ATTR_BACKGROUND_TINT, tint,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    private void applyBackground(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof ColorRef) {
            Integer color = ((ColorRef) ref).provide(resolver);
            if (color != null) {
                view.setBackgroundColor(color);
            }
        } else if (ref instanceof DrawableRef) {
            view.setBackground(((DrawableRef) ref).provide(resolver));
        }
    }

    private void applyBackgroundTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        resolver.getViewCompat().setBackgroundTintList(view, resolveTint(resolver, ref));
    }

    /**
     * {@link ColorRef} 与 {@link ColorStateListRef} 都收成 {@link ColorStateList}。
     */
    @Nullable
    protected ColorStateList resolveTint(@NonNull ResourceResolver resolver, ResourceRef<?> ref) {
        if (ref instanceof ColorRef) {
            Integer color = ((ColorRef) ref).provide(resolver);
            return color == null ? null : ColorStateList.valueOf(color);
        }
        if (ref instanceof ColorStateListRef) {
            return ((ColorStateListRef) ref).provide(resolver);
        }
        return null;
    }

    @Nullable
    protected ColorStateList resolveTint(@NonNull ResourceResolver resolver, int attr) {
        ResourceRef<?> ref = getAttrValue(attr);
        return resolveTint(resolver, ref);
    }
}
