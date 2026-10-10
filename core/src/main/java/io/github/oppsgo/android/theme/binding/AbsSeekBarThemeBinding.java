package io.github.oppsgo.android.theme.binding;

import android.os.Build;
import android.widget.AbsSeekBar;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link AbsSeekBar}（SeekBar / RatingBar）：在 {@link ProgressBarThemeBinding} 之上跟踪 thumb / thumbTint。
 */
public class AbsSeekBarThemeBinding extends ProgressBarThemeBinding {

    public static final int ATTR_THUMB = android.R.attr.thumb;
    public static final int ATTR_THUMB_TINT = android.R.attr.thumbTint;

    public AbsSeekBarThemeBinding(@NonNull AbsSeekBar view) {
        super(view);
        bindRaw(ATTR_THUMB, DrawableRef::of, this::applyThumb);
        bindRaw(ATTR_THUMB_TINT, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId), this::applyThumbTint);
    }

    @NonNull
    @Override
    public AbsSeekBar getView() {
        return (AbsSeekBar) view;
    }

    public void setThumb(@NonNull DrawableRef thumb) {
        setAttrAndApply(ATTR_THUMB, thumb);
    }

    public void setThumbResource(@DrawableRes int resId) {
        setAttrResource(ATTR_THUMB, resId, DrawableRef::of);
    }

    public void setThumbTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_THUMB_TINT, tint);
    }

    public void setThumbTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_THUMB_TINT, tint);
    }

    public void setThumbTint(@ColorRes int tint) {
        setAttrResource(ATTR_THUMB_TINT, tint, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    private void applyThumb(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            getView().setThumb(((DrawableRef) ref).provide(resolver));
        }
    }

    private void applyThumbTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        getView().setThumbTintList(resolveTint(resolver, ref));
    }
}
