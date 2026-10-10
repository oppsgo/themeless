package io.github.oppsgo.android.theme.binding;

import android.widget.CompoundButton;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link CompoundButton} 绑定：在 TextView 之上跟踪 {@code button} / {@code buttonTint}。
 * 覆盖 CheckBox、RadioButton、ToggleButton 等；系统 {@link android.widget.Switch} 见
 * {@link SwitchThemeBinding}。
 */
public class CompoundButtonThemeBinding extends TextViewThemeBinding {

    public static final int ATTR_BUTTON = android.R.attr.button;
    public static final int ATTR_BUTTON_TINT = android.R.attr.buttonTint;

    public CompoundButtonThemeBinding(@NonNull CompoundButton view) {
        super(view);
        bindRaw(ATTR_BUTTON,
                DrawableRef::of,
                (resolver, ref) -> applyButtonDrawable(resolver, ref));
        bindRaw(ATTR_BUTTON_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) ->
                        resolver.getViewCompat().setButtonTintList(getView(), resolveTint(resolver, ref)));
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
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    private void applyButtonDrawable(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            getView().setButtonDrawable(((DrawableRef) ref).provide(resolver));
        }
    }
}
