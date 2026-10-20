package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.R2;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * {@link CompoundButton} 绑定：在 TextView 之上跟踪 {@code button} / {@code buttonTint}。
 * 覆盖 CheckBox、RadioButton、ToggleButton 等；系统 {@link android.widget.Switch} 见
 * {@link SwitchBinding}。
 */
public class CompoundButtonBinding extends TextViewBinding {

    public static final int ATTR_BUTTON = R2.attr.button;
    public static final int ATTR_BUTTON_TINT = R2.attr.buttonTint;

    public CompoundButtonBinding(@NonNull CompoundButton view) {
        super(view);
        bindRaw(ATTR_BUTTON,
                DrawableRef::of,
                this::applyButtonDrawable);
        bindRaw(ATTR_BUTTON_TINT,
                colorFactory(),
                this::applyButtonTint);
    }

    @NonNull
    @Override
    public CompoundButton getView() {
        return (CompoundButton) view;
    }

    public void setButtonDrawable(@NonNull DrawableRef button) {
        setAttrAndApply(ATTR_BUTTON, button);
    }

    public void setButtonDrawable(@DrawableRes int resId) {
        setAttrResource(ATTR_BUTTON, resId, DrawableRef::of);
    }

    public void setButtonTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_BUTTON_TINT, tint);
    }

    public void setButtonTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_BUTTON_TINT, tint);
    }

    public void setButtonTint(@ColorRes int tint) {
        setAttrResource(ATTR_BUTTON_TINT, tint,
                colorFactory());
    }

    private void applyButtonTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> tint = resolveTint(resolver, ref);
        if (tint.isPresent()) {
            resolver.getViewCompat().setButtonTintList(getView(), tint.get());
        }
    }

    private void applyButtonDrawable(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            ResourceValue<Drawable> button = ((DrawableRef) ref).provide(resolver);
            if (button.isPresent()) {
                getView().setButtonDrawable(button.get());
            }
        }
    }
}
