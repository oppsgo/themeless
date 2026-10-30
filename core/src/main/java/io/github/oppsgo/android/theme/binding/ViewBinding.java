package io.github.oppsgo.android.theme.binding;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.github.oppsgo.android.theme.IViewBinding;
import io.github.oppsgo.android.theme.OnViewThemeUpdateListener;
import io.github.oppsgo.android.theme.R2;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeDelegate;
import io.github.oppsgo.android.theme.ThemeKey;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.resolver.ContextResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * Binding 基础设施与 View 级属性（{@code background} / {@code backgroundTint}）。
 * 更具体的能力由各子类声明。
 * <p>
 * 属性在构造器里一行一个地登记：{@link #bindRaw}（带 setter 的常规属性）、
 * {@link #bindSpecial}（需整组写入等特殊处理、在子类 {@link #applySelf} 里刷的属性）。
 * 布局 XML 要读哪些属性也从登记结果推导，不再单独声明 styleable。
 * <p>
 * 不带 View 泛型；子类用协变 {@link #getView()} 暴露真实类型。
 * setter 以具体 {@link ResourceRef} 子类为单入口；{@code int} 重载只作资源 id 便捷包装。
 */
public class ViewBinding implements IViewBinding {

    public static final int ATTR_BACKGROUND = R2.attr.background;
    public static final int ATTR_BACKGROUND_TINT = R2.attr.backgroundTint;

    /**
     * {@code app:themeViewAttrEnable} → {@link #setEnabled(boolean)}
     */
    private static final int ATTR_ENABLED = R2.attr.themeViewAttrEnable;

    @NonNull
    protected final View view;

    /**
     * 上次 {@link #apply(ResourceResolver)} 时记录的 {@link ThemeManager#getModCount(Context)}。
     * {@link #refresh()} 据此判断是否需要重刷：一致则跳过。
     * 主线程专用（{@link ThemeManager} 的契约），无需 volatile。
     */
    protected int modCount = ThemeManager.MOD_COUNT_NONE;

    /**
     * 统一属性表：{@link ThemeKey} 为 attr 或业务 token；attr 会先经别名归一。
     * 数量很少，用 {@link HashMap} 即可（无需 SparseArray）。
     */
    private final Map<ThemeKey, ViewAttr> attrs = new HashMap<>();

    /**
     * 别名 attr → 平台 attr。布局里两者都有值时，别名覆盖。
     */
    private final SparseIntArray aliases = new SparseIntArray();

    private boolean enabled = true;

    @Nullable
    private OnViewThemeUpdateListener updateListener;

    /**
     * 临时 Binding 在未 apply 时复用的 Context Resolver，避免每次 setXxx 都 new。
     * 主线程专用，无锁。
     */
    @Nullable
    private ResourceResolver fallbackResolver;

    public ViewBinding(@NonNull View view) {
        this.view = view;
        bindRaw(ATTR_BACKGROUND, resourceId -> ResourceRef.fromColorOrDrawable(resourceTable(), resourceId), this::applyBackground);
        bindRaw(ATTR_BACKGROUND_TINT, colorFactory(), this::applyBackgroundTint);
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
        @NonNull
        final ThemeKey key;
        @Nullable
        AttributeApplier applier;
        @Nullable
        IViewBinding.ResourceFactory<?> factory;
        @Nullable
        ResourceRef<?> ref;

        ViewAttr(@NonNull ThemeKey key, @Nullable AttributeApplier applier) {
            this.key = key;
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
        define(ThemeKey.of(attr), factory, applier).ref = null;
    }

    /**
     * 登记特殊属性：只记 factory 供布局 XML 读取，刷新时由子类在 {@link #applySelf}
     * 里整组写入（如 compound drawable）。
     */
    protected final void bindSpecial(@AttrRes int attr, @NonNull IViewBinding.ResourceFactory<?> factory) {
        define(ThemeKey.of(attr), factory, null).ref = null;
    }

    /**
     * 通用颜色 {@link IViewBinding.ResourceFactory}：按当前资源表判定类型，
     * 造 {@link ColorRef} 或 {@link ColorStateListRef}。各 Binding 的颜色 / tint 登记
     * 统一用它，替代逐个书写的 {@code resourceId -> ResourceRef.fromColor(...)}。
     */
    @NonNull
    protected final IViewBinding.ResourceFactory<Object> colorFactory() {
        return resourceId -> ResourceRef.fromColor(resourceTable(), resourceId);
    }

    /**
     * 把 {@code aliasAttr} 归一到已登记的 {@code canonicalAttr}（如 {@code app:srcCompat} → {@code src}）。
     */
    public final void alias(@AttrRes int aliasAttr, @AttrRes int canonicalAttr) {
        if (!attrs.containsKey(ThemeKey.of(canonicalAttr))) {
            throw new IllegalArgumentException("Canonical attribute is not bound: 0x" + Integer.toHexString(canonicalAttr));
        }
        aliases.put(aliasAttr, canonicalAttr);
    }

    @NonNull
    private ViewAttr define(
            @NonNull ThemeKey key,
            @Nullable IViewBinding.ResourceFactory<?> factory,
            @Nullable AttributeApplier applier
    ) {
        ThemeKey canonical = canonicalKey(key);
        ViewAttr target = attrs.get(canonical);
        if (target == null) {
            target = new ViewAttr(canonical, applier);
            attrs.put(canonical, target);
        } else if (applier != null) {
            target.applier = applier;
        }
        if (factory != null) {
            target.factory = factory;
        }
        return target;
    }

    @Nullable
    private ViewAttr findAttr(@NonNull ThemeKey key) {
        return attrs.get(canonicalKey(key));
    }

    @Nullable
    private ViewAttr findAttr(@AttrRes int attr) {
        return findAttr(ThemeKey.of(attr));
    }

    @NonNull
    private ThemeKey canonicalKey(@NonNull ThemeKey key) {
        if (!key.isAttr()) {
            return key;
        }
        int canonical = canonicalAttr(key.getAttr());
        return canonical == key.getAttr() ? key : ThemeKey.of(canonical);
    }

    @AttrRes
    private int canonicalAttr(@AttrRes int attr) {
        return aliases.get(attr, attr);
    }

    // ---- IViewBinding ----

    @Override
    public void bind(@Nullable AttributeSet set) {
        // 只读 AttributeSet；挂 tag / 容器钩子由 ThemeDelegate inflate 负责
        if (set == null) return;

        int[] xmlAttrs = getXmlAttrs();
        try (TypedArray ta = view.getContext().obtainStyledAttributes(set, xmlAttrs)) {
            // {@link #ATTR_ENABLED} 随主表一次读回，省一次 obtainStyledAttributes；
            // 布局未写出则保持当前 {@link #isEnabled()}（默认 true）。
            int enabledIndex = Arrays.binarySearch(xmlAttrs, ATTR_ENABLED);
            if (enabledIndex >= 0 && ta.hasValue(enabledIndex)) {
                setEnabled(ta.getBoolean(enabledIndex, true));
            }
            readXmlAttrs(ta, xmlAttrs, false);
            readXmlAttrs(ta, xmlAttrs, true);
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
     * 布局要读的 attr：仅 {@link ThemeKey#isAttr()} 的登记 + 别名 + {@link #ATTR_ENABLED}，
     * 升序（{@code obtainStyledAttributes} 要求）。
     * 常规流程每个 View 只在 inflate 时 {@link #bind(AttributeSet)} 一次，不做实例级缓存。
     */
    @NonNull
    private int[] getXmlAttrs() {
        int attrCount = 0;
        for (ThemeKey key : attrs.keySet()) {
            if (key.isAttr()) {
                attrCount++;
            }
        }
        int[] xmlAttrs = new int[attrCount + aliases.size() + 1];
        int index = 0;
        for (ThemeKey key : attrs.keySet()) {
            if (key.isAttr()) {
                xmlAttrs[index++] = key.getAttr();
            }
        }
        for (int i = 0; i < aliases.size(); i++) {
            xmlAttrs[index++] = aliases.keyAt(i);
        }
        xmlAttrs[index] = ATTR_ENABLED;
        Arrays.sort(xmlAttrs);
        return xmlAttrs;
    }

    @Override
    public final <T> void bind(
            @NonNull ThemeKey key,
            @NonNull IViewBinding.ResourceFactory<T> factory,
            @NonNull IViewBinding.AttrSetter<T> setter
    ) {
        define(key, factory, providerSetter(setter)).ref = null;
    }

    @Override
    public final <T> void bind(
            @AttrRes int attr,
            @NonNull IViewBinding.ResourceFactory<T> factory,
            @NonNull IViewBinding.AttrSetter<T> setter
    ) {
        bind(ThemeKey.of(attr), factory, setter);
    }

    @Override
    public final <T> void bind(
            @NonNull ThemeKey key,
            @NonNull IViewBinding.AttrSetter<T> setter
    ) {
        define(key, null, providerSetter(setter)).ref = null;
    }

    @Override
    public final <T> void bind(
            @AttrRes int attr,
            @NonNull IViewBinding.AttrSetter<T> setter
    ) {
        bind(ThemeKey.of(attr), setter);
    }

    @Override
    public final void set(@NonNull ThemeKey key, @Nullable ResourceRef<?> ref) {
        ViewAttr target = findAttr(key);
        if (target == null) {
            return;
        }
        putAndApply(target, ref);
    }

    @Override
    public final void set(@AttrRes int attr, @Nullable ResourceRef<?> ref) {
        set(ThemeKey.of(attr), ref);
    }

    @Override
    public void unbind(@NonNull ThemeKey key) {
        ViewAttr target = findAttr(key);
        if (target != null) {
            target.ref = null;
        }
    }

    @Override
    public void unbind(@AttrRes int attr) {
        unbind(ThemeKey.of(attr));
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
    public void setOnViewThemeUpdateListener(@Nullable OnViewThemeUpdateListener listener) {
        this.updateListener = listener;
    }

    @Override
    public void notifyViewThemeUpdate() {
        OnViewThemeUpdateListener listener = updateListener;
        if (listener == null || !isEnabled()) {
            return;
        }
        ResourceResolver resolver = currentResolver();
        if (resolver != null) {
            listener.onThemeUpdate(view, resolver);
        }
    }

    /**
     * 换肤入口：门禁 → 记代数 → {@link #applySelf} → {@link #applyDescendants} → 回调。
     * final：自身与级联的先后、以及 {@link OnViewThemeUpdateListener} 的时机固定；
     * 特殊属性重写 {@link #applySelf}，容器级联重写 {@link #applyDescendants}。
     */
    @Override
    public final void apply(ResourceResolver resolver) {
        if (!isEnabled() || resolver == null) return;
        int current = ThemeManager.get().getModCount(view.getContext());
        if (current != ThemeManager.MOD_COUNT_NONE) {
            this.modCount = current;
        }
        applySelf(resolver);
        applyDescendants(resolver);
        // 处理自定义View自己实现了 OnViewThemeUpdateListener
        View view = getView();
        if (view instanceof OnViewThemeUpdateListener && !(view instanceof IViewBinding)) {
            ((OnViewThemeUpdateListener) view).onThemeUpdate(view, resolver);
        }
        OnViewThemeUpdateListener listener = updateListener;
        if (listener != null) {
            listener.onThemeUpdate(this.view, resolver);
        }
    }

    /**
     * 只写本 View 已绑定的属性。默认刷带 applier 的常规 attr。
     * {@link #bindSpecial} 登记的属性在子类里先处理完再 {@code super.applySelf}。
     * 不管子树；级联走 {@link #applyDescendants}。
     */
    @CallSuper
    protected void applySelf(@NonNull ResourceResolver resolver) {
        for (ViewAttr attr : attrs.values()) {
            applyAttr(resolver, attr);
        }
    }

    /**
     * 本 View 刷完后的级联。默认空。
     * <p>
     * {@link ViewGroupBinding} 在这里按深度下探子 View；
     * 要换查询方式的容器（如 RecyclerView）整段重写，不必调用 super。
     */
    protected void applyDescendants(@NonNull ResourceResolver resolver) {
    }

    @Override
    public void refresh() {
        int current = ThemeManager.get().getModCount(view.getContext());
        if (current != ThemeManager.MOD_COUNT_NONE && modCount == current) {
            return;
        }
        ResourceResolver resolver = ThemeManager.get().getResolver(view.getContext());
        apply(resolver);
    }

    // ---- 手动 setXxx 的内部通路 ----

    /**
     * 记值，不立刻刷。不挂 tag；未 inflate 挂载的临时 Binding 只影响本次对象。
     */
    protected final void setAttr(@AttrRes int attr, @Nullable ResourceRef<?> ref) {
        ViewAttr target = findAttr(attr);
        if (target != null) {
            target.ref = normalize(ref);
        }
    }

    /**
     * 记值并立刻写回 View。委托 {@link #set(ThemeKey, ResourceRef)}。
     */
    protected final void setAttrAndApply(@AttrRes int attr, @Nullable ResourceRef<?> ref) {
        set(ThemeKey.of(attr), ref);
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

    private void putAndApply(@NonNull ViewAttr target, @Nullable ResourceRef<?> ref) {
        target.ref = normalize(ref);
        if (ref != null) {
            applyNow(target);
        }
    }

    private void applyNow(@NonNull ViewAttr attr) {
        ResourceResolver resolver;
        if (!enabled || attr.ref == null || (resolver = currentResolver()) == null) return;
        applyAttr(resolver, attr);
    }

    private void applyAttr(@NonNull ResourceResolver resolver, @NonNull ViewAttr attr) {
        if (attr.applier != null && attr.ref != null) {
            attr.applier.apply(resolver, attr.ref);
        }
    }

    @NonNull
    private static <T> AttributeApplier providerSetter(@NonNull IViewBinding.AttrSetter<T> setter) {
        return (resolver, ref) -> {
            ResourceValue<T> value = provideValue(resolver, ref);
            // 有值才调 setter：整份 ResourceValue 传入；取不到值跳过。
            if (value.isPresent()) {
                setter.set(value);
            }
        };
    }

    @NonNull
    private static <T> ResourceValue<T> provideValue(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        @SuppressWarnings("unchecked")
        ResourceValue<T> value = (ResourceValue<T>) ref.provide(resolver);
        return value;
    }

    @Nullable
    private static ResourceRef<?> normalize(@Nullable ResourceRef<?> ref) {
        if (ref == null || ref.isUnspecified()) {
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
        ResourceResolver resolver = currentResolver();
        return resolver == null ? getView().getResources() : resolver.getResources();
    }

    /**
     * 已 {@link ThemeManager#apply(Context, ResourceResolver)} 时用安装好的 Resolver；
     * 否则懒缓存一份基于 View Context 的 Resolver，供 setXxx 立刻取值。
     * 主线程专用（{@link ThemeManager} 的契约），无需双重检查锁。
     */
    @Nullable
    protected ResourceResolver currentResolver() {
        ResourceResolver resolver = ThemeManager.get().getResolver(view.getContext());
        if (resolver != null) {
            return resolver;
        }
        if (fallbackResolver == null) {
            fallbackResolver = new ContextResourceResolver(view.getContext());
        }
        return fallbackResolver;
    }

    // ---- 容器钩子 ----

    /**
     * 安装容器钩子（注册子项挂载监听等）。须幂等。
     * <p>
     * 由 {@link ThemeDelegate} 在 inflate 挂载时调用；{@link ThemeManager#of(View)} 临时创建不装钩子。
     * 需要时也可显式调用。
     */
    public void onAttached() {
    }

    /**
     * 卸下容器钩子。须在从未 {@link #onAttached()} 时也安全（勿误清外部监听）。
     * {@link ThemeManager#detach(View)} 会调用。
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
        setAttrResource(ATTR_BACKGROUND_TINT, tint, colorFactory());
    }

    private void applyBackground(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof ColorRef) {
            ResourceValue<Integer> color = ((ColorRef) ref).provide(resolver);
            if (color.hasValue()) {
                view.setBackgroundColor(color.require());
            }
        } else if (ref instanceof DrawableRef) {
            ResourceValue<Drawable> background = ((DrawableRef) ref).provide(resolver);
            if (background.isPresent()) {
                view.setBackground(background.get());
            }
        }
    }

    private void applyBackgroundTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> tint = resolveTint(resolver, ref);
        if (tint.isPresent()) {
            resolver.getViewCompat().setBackgroundTintList(view, tint.get());
        }
    }

    /**
     * {@link ColorRef} 与 {@link ColorStateListRef} 都收成 {@link ColorStateList}。
     * 返回 {@link ResourceValue}：有值（可为显式 null，清 tint）才写回。
     */
    @NonNull
    protected ResourceValue<ColorStateList> resolveTint(@NonNull ResourceResolver resolver, ResourceRef<?> ref) {
        if (ref instanceof ColorRef) {
            ResourceValue<Integer> color = ((ColorRef) ref).provide(resolver);
            if (color.hasValue()) {
                return ResourceValue.of(ColorStateList.valueOf(color.require()));
            }
            return ResourceValue.empty();
        }
        if (ref instanceof ColorStateListRef) {
            return ((ColorStateListRef) ref).provide(resolver);
        }
        return ResourceValue.empty();
    }

    @NonNull
    protected ResourceValue<ColorStateList> resolveTint(@NonNull ResourceResolver resolver, int attr) {
        return resolveTint(resolver, getAttrValue(attr));
    }
}
