package io.github.oppsgo.android.theme.binding.cardview;

import android.content.res.ColorStateList;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.androidx.R2;
import io.github.oppsgo.android.theme.binding.ViewGroupBinding;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * {@link CardView}：跟踪 {@code app:cardBackgroundColor}，换肤时写回
 * {@link CardView#setCardBackgroundColor(ColorStateList)}。
 * <p>
 * {@code MaterialCardView} 的描边、前景、水波纹和勾选图标见
 * {@link io.github.oppsgo.android.theme.binding.material.MaterialCardViewBinding}，
 * 背景色仍由本类跟踪。
 */
public class CardViewBinding extends ViewGroupBinding {

    /** 卡片背景色，{@code app:cardBackgroundColor}。 */
    public static final int ATTR_CARD_BACKGROUND_COLOR = R2.cardview.attr.cardBackgroundColor;

    public CardViewBinding(@NonNull CardView view) {
        super(view);
        bindRaw(ATTR_CARD_BACKGROUND_COLOR, colorFactory(), this::applyCardBackgroundColor);
    }

    @NonNull
    @Override
    public CardView getView() {
        return (CardView) view;
    }

    public void setCardBackgroundColor(ColorRef color) {
        setAttrAndApply(ATTR_CARD_BACKGROUND_COLOR, color);
    }

    public void setCardBackgroundColor(ColorStateListRef color) {
        setAttrAndApply(ATTR_CARD_BACKGROUND_COLOR, color);
    }

    public void setCardBackgroundColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_CARD_BACKGROUND_COLOR, colorRes, colorFactory());
    }

    private void applyCardBackgroundColor(
            @NonNull ResourceResolver resolver,
            @NonNull ResourceRef<?> ref
    ) {
        ResourceValue<ColorStateList> color = resolveTint(resolver, ref);
        if (color.isPresent()) {
            getView().setCardBackgroundColor(color.get());
        }
    }
}
