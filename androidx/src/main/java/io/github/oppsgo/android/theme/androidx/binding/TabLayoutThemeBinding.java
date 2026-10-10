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
public class TabLayoutThemeBinding extends ViewBinding {

    public static final int ATTR_TAB_TEXT_COLOR = com.google.android.material.R.attr.tabTextColor;
    public static final int ATTR_TAB_SELECTED_TEXT_COLOR =
            com.google.android.material.R.attr.tabSelectedTextColor;
    public static final int ATTR_TAB_INDICATOR_COLOR =
            com.google.android.material.R.attr.tabIndicatorColor;
    public static final int ATTR_TAB_RIPPLE_COLOR = com.google.android.material.R.attr.tabRippleColor;
    public static final int ATTR_TAB_ICON_TINT = com.google.android.material.R.attr.tabIconTint;

    public TabLayoutThemeBinding(@NonNull TabLayout view) {
        super(view);
        bindSpecial(ATTR_TAB_TEXT_COLOR,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
        bindSpecial(ATTR_TAB_SELECTED_TEXT_COLOR,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
        bindRaw(ATTR_TAB_INDICATOR_COLOR,
                ColorRef::of,
                this::applyIndicatorColor);
        bindSpecial(ATTR_TAB_RIPPLE_COLOR,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
        bindSpecial(ATTR_TAB_ICON_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    @NonNull
    @Override
    public TabLayout getView() {
        return (TabLayout) view;
    }

    @Override
    protected void applySpecialAttrs(@NonNull ResourceResolver resolver) {
        ResourceRef<?> normalRef = getAttrValue(ATTR_TAB_TEXT_COLOR);
        ResourceRef<?> selectedRef = getAttrValue(ATTR_TAB_SELECTED_TEXT_COLOR);
        if (normalRef != null || selectedRef != null) {
            int normal = resolveColor(resolver, normalRef);
            int selected = selectedRef != null ? resolveColor(resolver, selectedRef) : normal;
            getView().setTabTextColors(normal, selected);
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
        setAttrResource(ATTR_TAB_TEXT_COLOR, normalColor,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
        setAttrResource(ATTR_TAB_SELECTED_TEXT_COLOR, selectedColor,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
        applySpecialAttrs(currentResolver());
    }

    public void setTabIndicatorColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_INDICATOR_COLOR, colorRes, ColorRef::of);
    }

    public void setTabRippleColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_RIPPLE_COLOR, colorRes,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
        ResourceResolver resolver = currentResolver();
        if (resolver != null) applySpecialAttrs(resolver);
    }

    public void setTabIconTint(@ColorRes int colorRes) {
        setAttrResource(ATTR_TAB_ICON_TINT, colorRes,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
        ResourceResolver resolver = currentResolver();
        if (resolver != null) applySpecialAttrs(resolver);
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
    private int resolveColor(@NonNull ResourceResolver resolver, @Nullable ResourceRef<?> ref) {
        Integer color = resolveColorOrNull(resolver, ref);
        return color == null ? 0 : color;
    }

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
