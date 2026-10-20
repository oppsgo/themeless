package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

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
 * {@link ImageView} 绑定，额外跟踪 src 和 tint。
 * src 的显示尺寸由调用方用 {@code Drawable.setBounds} 指定（空 bounds 用 intrinsic）。
 */
public class ImageViewBinding extends ViewBinding {

    public static final int ATTR_SRC = R2.attr.src;
    public static final int ATTR_TINT = R2.attr.tint;

    public ImageViewBinding(@NonNull ImageView view) {
        super(view);
        bindRaw(ATTR_SRC,
                DrawableRef::of,
                this::applySrc);
        bindRaw(ATTR_TINT,
                colorFactory(),
                this::applyImageTint);
    }

    @NonNull
    @Override
    public ImageView getView() {
        return (ImageView) view;
    }

    public void setImage(@NonNull DrawableRef src) {
        setAttrAndApply(ATTR_SRC, src);
    }

    public void setImageResource(@DrawableRes int resId) {
        setAttrResource(ATTR_SRC, resId, DrawableRef::of);
    }

    public void setImageTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_TINT, tint);
    }

    public void setImageTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_TINT, tint);
    }

    public void setImageTint(@ColorRes int tint) {
        setAttrResource(ATTR_TINT, tint,
                colorFactory());
    }

    private void applyImageTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> tint = resolveTint(resolver, ref);
        if (tint.isPresent()) {
            resolver.getViewCompat().setImageTintList(getView(), tint.get());
        }
    }

    private void applySrc(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            ResourceValue<Drawable> src = ((DrawableRef) ref).provide(resolver);
            if (src.isPresent()) {
                getView().setImageDrawable(src.get());
            }
        }
    }
}
