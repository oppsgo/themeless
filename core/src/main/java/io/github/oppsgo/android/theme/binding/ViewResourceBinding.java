package io.github.oppsgo.android.theme.binding;

import android.view.View;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;

/**
 * {@link View} 的默认绑定，处理 background 和 backgroundTint。
 * 颜色和图片分成两个重载，避免把 {@link DrawableRef} 传给颜色、或反过来。
 */
public class ViewResourceBinding extends BaseViewResourceBinding<View> {

    public ViewResourceBinding(@NonNull View view) {
        super(view);
    }

    /** 已有本类或子类就返回；否则给一个不挂到 View 上的实例。 */
    @NonNull
    public static ViewResourceBinding of(@NonNull View view) {
        return of(view, ViewResourceBinding.class, ViewResourceBinding::new);
    }

    @NonNull
    public ViewResourceBinding setBackground(@AnyRes int background) {
        if (background == ID_NULL) {
            unbind(ATTR_BACKGROUND);
            return this;
        }
        if (isPureColor(background)) {
            return setBackground(ColorRef.of(background));
        }
        return setBackground(DrawableRef.of(background));
    }

    @NonNull
    public ViewResourceBinding setBackground(@NonNull ColorRef background) {
        putAndUpdate(ATTR_BACKGROUND, background);
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackground(@NonNull DrawableRef background) {
        putAndUpdate(ATTR_BACKGROUND, background);
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackgroundTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_BACKGROUND_TINT);
            return this;
        }
        putAndUpdate(ATTR_BACKGROUND_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackgroundTint(@NonNull ColorRef tint) {
        putAndUpdate(ATTR_BACKGROUND_TINT, tint);
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackgroundTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(ATTR_BACKGROUND_TINT, tint);
        return this;
    }
}
