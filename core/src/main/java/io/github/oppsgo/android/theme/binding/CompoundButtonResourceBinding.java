package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.CompoundButton;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link CompoundButton} 绑定：在 TextView 之上跟踪 {@code button} / {@code buttonTint}。
 * 覆盖 CheckBox、RadioButton、ToggleButton 等；系统 {@link android.widget.Switch} 见
 * {@link SwitchResourceBinding}。
 */
public class CompoundButtonResourceBinding extends TextViewResourceBinding {

    public static final int ATTR_BUTTON = android.R.attr.button;
    public static final int ATTR_BUTTON_TINT = android.R.attr.buttonTint;

    public CompoundButtonResourceBinding(@NonNull CompoundButton view) {
        super(view);
    }

    @NonNull
    public static CompoundButtonResourceBinding of(@NonNull CompoundButton view) {
        return of(view, CompoundButtonResourceBinding.class, CompoundButtonResourceBinding::new);
    }

    @NonNull
    protected CompoundButton getCompoundButton() {
        return (CompoundButton) view;
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(super.getViewStyleable(),
                ATTR_BUTTON,
                ATTR_BUTTON_TINT
        );
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (attr == ATTR_BUTTON) {
            return resId == ID_NULL ? null : DrawableRef.of(resId);
        }
        if (attr == ATTR_BUTTON_TINT) {
            return createColorResource(resId);
        }
        return super.createResource(attr, resId);
    }

    @NonNull
    public CompoundButtonResourceBinding setButtonDrawable(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_BUTTON);
            return this;
        }
        return setButtonDrawable(DrawableRef.of(resId));
    }

    @NonNull
    public CompoundButtonResourceBinding setButtonDrawable(@NonNull DrawableRef button) {
        putAndUpdate(ATTR_BUTTON, button);
        return this;
    }

    @NonNull
    public CompoundButtonResourceBinding setButtonTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_BUTTON_TINT);
            return this;
        }
        putAndUpdate(ATTR_BUTTON_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public CompoundButtonResourceBinding setButtonTint(@NonNull ColorRef tint) {
        putAndUpdate(ATTR_BUTTON_TINT, tint);
        return this;
    }

    @NonNull
    public CompoundButtonResourceBinding setButtonTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(ATTR_BUTTON_TINT, tint);
        return this;
    }

    @Override
    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr, @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_BUTTON && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getCompoundButton().setButtonDrawable(drawable);
            return;
        }
        if (attr == ATTR_BUTTON_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            resolver.getViewCompat().setButtonTintList(getCompoundButton(), tint);
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }
}
