package io.github.oppsgo.android.theme.binding.material;

import android.content.res.ColorStateList;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.androidx.R2;
import io.github.oppsgo.android.theme.binding.ViewBinding;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * Material {@link TabLayout}：跟踪 tab 文字 / 指示器 / 水波纹 / 图标 tint。
 * <p>
 * 布局里的 {@code app:tabTextColor} / {@code app:tabSelectedTextColor} 统一收为
 * {@code setTabTextColors(int, int)}；其它 tint 走对应 ColorStateList setter。
 */
public class TabLayoutBinding extends ViewBinding {

    public static final int ATTR_TAB_TEXT_COLOR = R2.material.attr.tabTextColor;
    public static final int ATTR_TAB_SELECTED_TEXT_COLOR = R2.material.attr.tabSelectedTextColor;
    public static final int ATTR_TAB_INDICATOR_COLOR = R2.material.attr.tabIndicatorColor;
    public static final int ATTR_TAB_RIPPLE_COLOR = R2.material.attr.tabRippleColor;
    public static final int ATTR_TAB_ICON_TINT = R2.material.attr.tabIconTint;

    public TabLayoutBinding(@NonNull TabLayout view) {
        super(view);
        bindRaw(ATTR_TAB_INDICATOR_COLOR, ColorRef::of, this::applyIndicatorColor);
        bindSpecial(ATTR_TAB_TEXT_COLOR, colorFactory());
        bindSpecial(ATTR_TAB_SELECTED_TEXT_COLOR, colorFactory());
        bindSpecial(ATTR_TAB_RIPPLE_COLOR, colorFactory());
        bindSpecial(ATTR_TAB_ICON_TINT, colorFactory());
    }

    @NonNull
    @Override
    public TabLayout getView() {
        return (TabLayout) view;
    }

    @Override
    protected void applySelf(@NonNull ResourceResolver resolver) {
        super.applySelf(resolver);
        ResourceRef<?> normalRef = getAttrValue(ATTR_TAB_TEXT_COLOR);
        ResourceRef<?> selectedRef = getAttrValue(ATTR_TAB_SELECTED_TEXT_COLOR);
        if (normalRef != null || selectedRef != null) {
            Integer normal = resolveColorOrNull(resolver, normalRef);
            Integer selected = resolveColorOrNull(resolver, selectedRef);
            // 只跟踪到一侧时两侧同色；取不到值（资源缺失 / 颜色解析不到）跳过写入，
            // 不能以 0（全透明黑）静默写进 View。
            if (normal == null) normal = selected;
            if (selected == null) selected = normal;
            if (normal != null) {
                getView().setTabTextColors(normal, selected);
            }
        }

        ResourceValue<ColorStateList> ripple = resolveTint(resolver, ATTR_TAB_RIPPLE_COLOR);
        if (ripple.isPresent()) {
            getView().setTabRippleColor(ripple.get());
        }

        ResourceValue<ColorStateList> iconTint = resolveTint(resolver, ATTR_TAB_ICON_TINT);
        if (iconTint.isPresent()) {
            getView().setTabIconTint(iconTint.get());
        }
    }

    public void setTabTextColors(@ColorRes int normalColor, @ColorRes int selectedColor) {
        setAttrResource(ATTR_TAB_TEXT_COLOR, normalColor, colorFactory());
        setAttrResource(ATTR_TAB_SELECTED_TEXT_COLOR, selectedColor, colorFactory());
        applySelfNow();
    }

    public void setTabIndicatorColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_INDICATOR_COLOR, colorRes, ColorRef::of);
    }

    public void setTabRippleColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_RIPPLE_COLOR, colorRes, colorFactory());
        applySelfNow();
    }

    public void setTabIconTint(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_ICON_TINT, colorRes, colorFactory());
        applySelfNow();
    }

    private void applySelfNow() {
        ResourceResolver resolver = currentResolver();
        if (resolver != null) {
            applySelf(resolver);
        }
    }

    private void applyIndicatorColor(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        Integer color = resolveColorOrNull(resolver, ref);
        if (color != null) {
            getView().setSelectedTabIndicatorColor(color);
        }
    }

    /**
     * 从 ColorRef / ColorStateListRef 取出单个 color int（ColorStateList 取默认色）。
     * 取不到值（{@link ResourceValue#empty()}）或显式 null 都返回 {@code null}——
     * 颜色没有「写 null」的语义，调用方按取不到值跳过。
     */
    @Nullable
    private Integer resolveColorOrNull(@NonNull ResourceResolver resolver, @Nullable ResourceRef<?> ref) {
        if (ref instanceof ColorRef) {
            ResourceValue<Integer> color = ((ColorRef) ref).provide(resolver);
            return color.isPresent() ? color.get() : null;
        }
        if (ref instanceof ColorStateListRef) {
            ResourceValue<ColorStateList> colors = ((ColorStateListRef) ref).provide(resolver);
            if (!colors.hasValue()) return null;
            return colors.require().getDefaultColor();
        }
        return null;
    }
}
