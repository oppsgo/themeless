package io.github.oppsgo.android.theme.androidx.binding;

import android.content.res.ColorStateList;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.binding.ViewBinding;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * Material {@link TabLayout}：跟踪 tab 文字 / 指示器 / 水波纹 / 图标 tint。
 * <p>
 * 布局里的 {@code app:tabTextColor} / {@code app:tabSelectedTextColor} 统一收为
 * {@code setTabTextColors(int, int)}；其它 tint 走对应 ColorStateList setter。
 */
public class TabLayoutBinding extends ViewBinding {

    public static final int ATTR_TAB_TEXT_COLOR = com.google.android.material.R.attr.tabTextColor;
    public static final int ATTR_TAB_SELECTED_TEXT_COLOR = com.google.android.material.R.attr.tabSelectedTextColor;
    public static final int ATTR_TAB_INDICATOR_COLOR = com.google.android.material.R.attr.tabIndicatorColor;
    public static final int ATTR_TAB_RIPPLE_COLOR = com.google.android.material.R.attr.tabRippleColor;
    public static final int ATTR_TAB_ICON_TINT = com.google.android.material.R.attr.tabIconTint;

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
    protected void applySpecialAttrs(ResourceResolver resolver) {
        if (resolver == null) {
            return;
        }

        ResourceRef<?> normalRef = getAttrValue(ATTR_TAB_TEXT_COLOR);
        ResourceRef<?> selectedRef = getAttrValue(ATTR_TAB_SELECTED_TEXT_COLOR);
        if (normalRef != null || selectedRef != null) {
            Integer normal = resolveColorOrNull(resolver, normalRef);
            Integer selected = resolveColorOrNull(resolver, selectedRef);
            // 只跟踪到一侧时两侧同色；取不到值（资源缺失 / provider 返回 null）跳过写入，
            // 不能以 0（全透明黑）静默写进 View。
            if (normal == null) normal = selected;
            if (selected == null) selected = normal;
            if (normal != null) {
                getView().setTabTextColors(normal, selected);
            }
        }

        ColorStateList ripple = resolveTint(resolver, ATTR_TAB_RIPPLE_COLOR);
        if (ripple != null) {
            getView().setTabRippleColor(ripple);
        }

        ColorStateList iconTint = resolveTint(resolver, ATTR_TAB_ICON_TINT);
        if (iconTint != null) {
            getView().setTabIconTint(iconTint);
        }
    }

    public void setTabTextColors(@ColorRes int normalColor, @ColorRes int selectedColor) {
        setAttrResource(ATTR_TAB_TEXT_COLOR, normalColor, colorFactory());
        setAttrResource(ATTR_TAB_SELECTED_TEXT_COLOR, selectedColor, colorFactory());
        applySpecialAttrs(currentResolver());
    }

    public void setTabIndicatorColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_INDICATOR_COLOR, colorRes, ColorRef::of);
    }

    public void setTabRippleColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_RIPPLE_COLOR, colorRes, colorFactory());
        applySpecialAttrs(currentResolver());
    }

    public void setTabIconTint(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_ICON_TINT, colorRes, colorFactory());
        applySpecialAttrs(currentResolver());
    }

    private void applyIndicatorColor(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        Integer color = resolveColorOrNull(resolver, ref);
        if (color != null) {
            getView().setSelectedTabIndicatorColor(color);
        }
    }

    /**
     * 从 ColorRef / ColorStateListRef 取出单个 color int（ColorStateList 取默认色）。
     */
    @Nullable
    private Integer resolveColorOrNull(@NonNull ResourceResolver resolver, @Nullable ResourceRef<?> ref) {
        if (ref instanceof ColorRef) {
            return ((ColorRef) ref).provide(resolver);
        }
        if (ref instanceof ColorStateListRef) {
            ColorStateList colors = ((ColorStateListRef) ref).provide(resolver);
            return colors == null ? null : colors.getDefaultColor();
        }
        return null;
    }
}
