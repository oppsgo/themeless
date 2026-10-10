package io.github.oppsgo.android.theme.binding;

import android.widget.ImageView;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link ImageView} 绑定，额外跟踪 src 和 tint。
 * src 的显示尺寸由调用方用 {@code Drawable.setBounds} 指定（空 bounds 用 intrinsic）。
 */
public class ImageViewThemeBinding extends ViewBinding {

    public static final int ATTR_SRC = android.R.attr.src;
    public static final int ATTR_TINT = android.R.attr.tint;

    public ImageViewThemeBinding(@NonNull ImageView view) {
        super(view);
        bindRaw(ATTR_SRC,
                DrawableRef::of,
                (resolver, ref) -> applySrc(resolver, ref));
        bindRaw(ATTR_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) ->
                        resolver.getViewCompat().setImageTintList(getView(), resolveTint(resolver, ref)));
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
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    private void applySrc(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            getView().setImageDrawable(((DrawableRef) ref).provide(resolver));
        }
    }
}
