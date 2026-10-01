package io.github.oppsgo.android.theme.androidx.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.R;
import com.google.android.material.tabs.TabLayout;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ViewGroupResourceBinding;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * Material {@link TabLayout}：公开 setter 与 {@link TabLayout} 一一对应。
 * <p>
 * inflate 前在 {@link io.github.oppsgo.android.theme.androidx.ThemeAndroidX#registerAvailable()}
 * 中登记（material 为 compileOnly，缺依赖则跳过）。
 * <p>
 * View 侧文字色最终都是一份 {@link ColorStateList}。
 * 两参 {@link TabLayout#setTabTextColors(int, int)} 与 XML 的
 * {@code tabSelectedTextColor} 都先收成 {@link ColorStateListRef}，再走同一 attr。
 */
public class TabLayoutResourceBinding extends ViewGroupResourceBinding {

    /**
     * {@code app:tabIndicatorColor} → {@link TabLayout#setSelectedTabIndicatorColor(int)}
     */
    public static final int ATTR_TAB_INDICATOR_COLOR = R.attr.tabIndicatorColor;
    /**
     * {@code app:tabTextColor} → {@link TabLayout#setTabTextColors(ColorStateList)}
     */
    public static final int ATTR_TAB_TEXT_COLOR = R.attr.tabTextColor;
    /**
     * {@code app:tabSelectedTextColor}：XML 读入后立刻 fold 进 {@link #ATTR_TAB_TEXT_COLOR}。
     */
    public static final int ATTR_TAB_SELECTED_TEXT_COLOR = R.attr.tabSelectedTextColor;
    /**
     * {@code app:tabRippleColor} → {@link TabLayout#setTabRippleColor(ColorStateList)}
     */
    public static final int ATTR_TAB_RIPPLE_COLOR = R.attr.tabRippleColor;
    /**
     * {@code app:tabIndicator} → {@link TabLayout#setSelectedTabIndicator(Drawable)}
     */
    public static final int ATTR_TAB_INDICATOR = R.attr.tabIndicator;


    /**
     * Material {@code createColorStateList}：selected 在前，default 在后。
     */
    private static final int[][] TAB_TEXT_COLOR_STATES = new int[][]{
            new int[]{android.R.attr.state_selected},
            new int[]{},
    };

    public static void register() {
        ThemeManager.get().registry().register(TabLayout.class, TabLayoutResourceBinding::new);
    }

    public TabLayoutResourceBinding(@NonNull TabLayout view) {
        super(view);
    }

    @NonNull
    @Override
    public TabLayout getView() {
        return (TabLayout) view;
    }

    @NonNull
    public static TabLayoutResourceBinding of(@NonNull TabLayout view) {
        return of(view, TabLayoutResourceBinding.class, TabLayoutResourceBinding::new);
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(
                super.getViewStyleable(),
                ATTR_TAB_INDICATOR_COLOR,
                ATTR_TAB_TEXT_COLOR,
                ATTR_TAB_SELECTED_TEXT_COLOR,
                ATTR_TAB_RIPPLE_COLOR,
                ATTR_TAB_INDICATOR
        );
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (resId == ID_NULL) return null;
        if (attr == ATTR_TAB_INDICATOR_COLOR
                || attr == ATTR_TAB_TEXT_COLOR
                || attr == ATTR_TAB_SELECTED_TEXT_COLOR
                || attr == ATTR_TAB_RIPPLE_COLOR) {
            return createColorResource(resId);
        }
        if (attr == ATTR_TAB_INDICATOR) {
            return DrawableRef.of(resId);
        }
        return super.createResource(attr, resId);
    }

    @NonNull
    @Override
    public TabLayoutResourceBinding bind(@Nullable AttributeSet set) {
        super.bind(set);
        foldSelectedTextColor();
        return this;
    }

    /**
     * {@link TabLayout#setSelectedTabIndicatorColor(int)}
     */
    @NonNull
    public TabLayoutResourceBinding setSelectedTabIndicatorColor(ResourceRef<?> color) {
        putAndUpdate(ATTR_TAB_INDICATOR_COLOR, color);
        return this;
    }

    @NonNull
    public TabLayoutResourceBinding setSelectedTabIndicatorColor(@ColorRes int colorRes) {
        if (colorRes == ID_NULL) {
            unbind(ATTR_TAB_INDICATOR_COLOR);
            return this;
        }
        return setSelectedTabIndicatorColor(createColorResource(colorRes));
    }

    /**
     * {@link TabLayout#setTabTextColors(ColorStateList)}
     */
    @NonNull
    public TabLayoutResourceBinding setTabTextColors(@Nullable ColorStateListRef colors) {
        putAndUpdate(ATTR_TAB_TEXT_COLOR, colors);
        return this;
    }

    /**
     * 纯色包装为单色 {@link ColorStateList}，对应 {@link TabLayout#setTabTextColors(ColorStateList)}。
     */
    @NonNull
    public TabLayoutResourceBinding setTabTextColors(@Nullable ColorRef color) {
        putAndUpdate(ATTR_TAB_TEXT_COLOR, color);
        return this;
    }


    /**
     * {@link TabLayout#setTabTextColors(int, int)}：内部即
     * {@code setTabTextColors(createColorStateList(normal, selected))}。
     */
    @NonNull
    public TabLayoutResourceBinding setTabTextColors(@ColorRes int normal, @ColorRes int selected) {
        return setTabTextColors(ColorStateListRef.of(
                TAB_TEXT_COLOR_STATES,
                ColorRef.of(selected),
                ColorRef.of(normal)
        ));
    }

    /**
     * {@link TabLayout#setTabRippleColor(ColorStateList)}
     */
    @NonNull
    public TabLayoutResourceBinding setTabRippleColor(ResourceRef<?> color) {
        putAndUpdate(ATTR_TAB_RIPPLE_COLOR, color);
        return this;
    }

    @NonNull
    public TabLayoutResourceBinding setTabRippleColor(@ColorRes int colorRes) {
        if (colorRes == ID_NULL) {
            unbind(ATTR_TAB_RIPPLE_COLOR);
            return this;
        }
        return setTabRippleColor(createColorResource(colorRes));
    }

    /**
     * {@link TabLayout#setSelectedTabIndicator(Drawable)}
     */
    @NonNull
    public TabLayoutResourceBinding setSelectedTabIndicator(ResourceRef<?> indicator) {
        putAndUpdate(ATTR_TAB_INDICATOR, indicator);
        return this;
    }

    @NonNull
    public TabLayoutResourceBinding setSelectedTabIndicator(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_TAB_INDICATOR);
            return this;
        }
        return setSelectedTabIndicator(DrawableRef.of(resId));
    }

    @Override
    protected void updateAttribute(
            @NonNull ResourceResolver resolver,
            @AttrRes int attr,
            @Nullable ResourceRef<?> value
    ) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_TAB_INDICATOR_COLOR) {
            Integer color = resolveColorInt(resolver, value);
            if (color != null) {
                getView().setSelectedTabIndicatorColor(color);
            }
            return;
        }
        if (attr == ATTR_TAB_TEXT_COLOR) {
            getView().setTabTextColors(resolveTint(resolver, value));
            return;
        }
        if (attr == ATTR_TAB_RIPPLE_COLOR) {
            getView().setTabRippleColor(resolveTint(resolver, value));
            return;
        }
        if (attr == ATTR_TAB_INDICATOR) {
            if (value instanceof DrawableRef) {
                Drawable d = ((DrawableRef) value).resolve(resolver);
                getView().setSelectedTabIndicator(d);
            }
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }

    /**
     * 对齐 Material inflate：有 {@code tabSelectedTextColor} 时，
     * 与 text 拼成一份 CSL，只保留 {@link #ATTR_TAB_TEXT_COLOR}。
     * 表里仍挂原始 {@link ResourceRef}；取纯色 int 推迟到 resolve。
     */
    private void foldSelectedTextColor() {
        ResourceRef<?> selected = attributes.get(ATTR_TAB_SELECTED_TEXT_COLOR);
        attributes.delete(ATTR_TAB_SELECTED_TEXT_COLOR);
        if (selected == null || selected.isEmpty()) return;

        ResourceRef<?> text = attributes.get(ATTR_TAB_TEXT_COLOR);
        if (text == null || text.isEmpty()) {
            putAttr(ATTR_TAB_TEXT_COLOR, selected);
            return;
        }
        putAttr(ATTR_TAB_TEXT_COLOR, ColorStateListRef.of(
                TAB_TEXT_COLOR_STATES,
                asColorValue(selected),
                asColorValue(text)
        ));
    }

    /**
     * resolve 时才收成 int：{@link ColorRef} 原样；{@link ColorStateListRef} 取 default。
     */
    @NonNull
    private static ResourceValue<Integer> asColorValue(@NonNull ResourceRef<?> value) {
        if (value instanceof ColorRef) {
            return (ColorRef) value;
        }
        if (value instanceof ColorStateListRef) {
            ColorStateListRef csl = (ColorStateListRef) value;
            return resolver -> {
                ColorStateList colors = csl.resolve(resolver);
                return colors == null ? null : colors.getDefaultColor();
            };
        }
        return resolver -> null;
    }
}
