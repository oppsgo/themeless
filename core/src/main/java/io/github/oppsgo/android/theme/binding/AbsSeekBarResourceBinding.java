package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.AbsSeekBar;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link AbsSeekBar}（SeekBar / RatingBar）：在 {@link ProgressBarResourceBinding} 之上跟踪 thumb / thumbTint。
 */
public class AbsSeekBarResourceBinding extends ProgressBarResourceBinding {

    public static final int ATTR_THUMB = android.R.attr.thumb;
    public static final int ATTR_THUMB_TINT = android.R.attr.thumbTint;

    public AbsSeekBarResourceBinding(@NonNull AbsSeekBar view) {
        super(view);
    }

    @NonNull
    @Override
    public AbsSeekBar getView() {
        return (AbsSeekBar) view;
    }

    @NonNull
    public static AbsSeekBarResourceBinding of(@NonNull AbsSeekBar view) {
        return of(view, AbsSeekBarResourceBinding.class, AbsSeekBarResourceBinding::new);
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(super.getViewStyleable(),
                ATTR_THUMB,
                ATTR_THUMB_TINT);
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (attr == ATTR_THUMB) {
            return resId == ID_NULL ? null : DrawableRef.of(resId);
        }
        if (attr == ATTR_THUMB_TINT) {
            return createColorResource(resId);
        }
        return super.createResource(attr, resId);
    }

    @NonNull
    public AbsSeekBarResourceBinding setThumb(ResourceRef<?> thumb) {
        putAndUpdate(ATTR_THUMB, thumb);
        return this;
    }

    @NonNull
    public AbsSeekBarResourceBinding setThumbResource(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_THUMB);
            return this;
        }
        return setThumb(DrawableRef.of(resId));
    }

    @NonNull
    public AbsSeekBarResourceBinding setThumbTint(ResourceRef<?> tint) {
        putAndUpdate(ATTR_THUMB_TINT, tint);
        return this;
    }

    @NonNull
    public AbsSeekBarResourceBinding setThumbTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_THUMB_TINT);
            return this;
        }
        return setThumbTint(createColorResource(tint));
    }

    @Override
    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr, @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_THUMB && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getView().setThumb(drawable);
            return;
        }
        if (attr == ATTR_THUMB_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getView().setThumbTintList(tint);
            }
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }
}
