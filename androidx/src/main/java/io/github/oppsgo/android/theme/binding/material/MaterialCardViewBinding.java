package io.github.oppsgo.android.theme.binding.material;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import com.google.android.material.card.MaterialCardView;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.androidx.R2;
import io.github.oppsgo.android.theme.binding.cardview.CardViewBinding;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * {@link MaterialCardView}：在 {@link CardViewBinding} 的背景色之外，跟踪描边色、
 * 前景色、水波纹和勾选图标（含 tint）。
 */
public class MaterialCardViewBinding extends CardViewBinding {

    /** 描边色，{@code app:strokeColor}。 */
    public static final int ATTR_STROKE_COLOR = R2.material.attr.strokeColor;
    /** 点击水波纹颜色，{@code app:rippleColor}。 */
    public static final int ATTR_RIPPLE_COLOR = R2.material.attr.rippleColor;
    /** 前景色，{@code app:cardForegroundColor}。 */
    public static final int ATTR_CARD_FOREGROUND_COLOR = R2.material.attr.cardForegroundColor;
    /** 可勾选状态下的勾选图标，{@code app:checkedIcon}。 */
    public static final int ATTR_CHECKED_ICON = R2.material.attr.checkedIcon;
    /** 勾选图标着色，{@code app:checkedIconTint}。 */
    public static final int ATTR_CHECKED_ICON_TINT = R2.material.attr.checkedIconTint;

    public MaterialCardViewBinding(@NonNull MaterialCardView view) {
        super(view);
        bindRaw(ATTR_STROKE_COLOR, colorFactory(), this::applyStrokeColor);
        bindRaw(ATTR_RIPPLE_COLOR, colorFactory(), this::applyRippleColor);
        bindRaw(ATTR_CARD_FOREGROUND_COLOR, colorFactory(), this::applyCardForegroundColor);
        bindRaw(ATTR_CHECKED_ICON, DrawableRef::of, this::applyCheckedIcon);
        bindRaw(ATTR_CHECKED_ICON_TINT, colorFactory(), this::applyCheckedIconTint);
    }

    @NonNull
    @Override
    public MaterialCardView getView() {
        return (MaterialCardView) view;
    }

    public void setStrokeColor(ColorRef color) {
        setAttrAndApply(ATTR_STROKE_COLOR, color);
    }

    public void setStrokeColor(ColorStateListRef color) {
        setAttrAndApply(ATTR_STROKE_COLOR, color);
    }

    public void setStrokeColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_STROKE_COLOR, colorRes, colorFactory());
    }

    public void setRippleColor(ColorRef color) {
        setAttrAndApply(ATTR_RIPPLE_COLOR, color);
    }

    public void setRippleColor(ColorStateListRef color) {
        setAttrAndApply(ATTR_RIPPLE_COLOR, color);
    }

    public void setRippleColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_RIPPLE_COLOR, colorRes, colorFactory());
    }

    public void setCardForegroundColor(ColorRef color) {
        setAttrAndApply(ATTR_CARD_FOREGROUND_COLOR, color);
    }

    public void setCardForegroundColor(ColorStateListRef color) {
        setAttrAndApply(ATTR_CARD_FOREGROUND_COLOR, color);
    }

    public void setCardForegroundColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_CARD_FOREGROUND_COLOR, colorRes, colorFactory());
    }

    public void setCheckedIcon(DrawableRef icon) {
        setAttrAndApply(ATTR_CHECKED_ICON, icon);
    }

    public void setCheckedIconResource(@DrawableRes int resId) {
        setAttrResource(ATTR_CHECKED_ICON, resId, DrawableRef::of);
    }

    public void setCheckedIconTint(ColorRef tint) {
        setAttrAndApply(ATTR_CHECKED_ICON_TINT, tint);
    }

    public void setCheckedIconTint(ColorStateListRef tint) {
        setAttrAndApply(ATTR_CHECKED_ICON_TINT, tint);
    }

    public void setCheckedIconTint(@ColorRes int tint) {
        setAttrResource(ATTR_CHECKED_ICON_TINT, tint, colorFactory());
    }

    private void applyStrokeColor(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> color = resolveTint(resolver, ref);
        if (color.isPresent()) {
            getView().setStrokeColor(color.get());
        }
    }

    private void applyRippleColor(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> color = resolveTint(resolver, ref);
        if (color.isPresent()) {
            getView().setRippleColor(color.get());
        }
    }

    private void applyCardForegroundColor(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> color = resolveTint(resolver, ref);
        if (color.isPresent()) {
            getView().setCardForegroundColor(color.get());
        }
    }

    private void applyCheckedIcon(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            ResourceValue<Drawable> icon = ((DrawableRef) ref).provide(resolver);
            if (icon.isPresent()) {
                getView().setCheckedIcon(icon.get());
            }
        }
    }

    private void applyCheckedIconTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> tint = resolveTint(resolver, ref);
        if (tint.isPresent()) {
            getView().setCheckedIconTint(tint.get());
        }
    }
}
