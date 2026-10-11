package io.github.oppsgo.android.theme.binding;

import android.annotation.SuppressLint;
import android.os.Build;
import android.widget.ProgressBar;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link ProgressBar}：跟踪 progress / indeterminate drawable 及其 tint。
 * {@link android.widget.AbsSeekBar} 见 {@link AbsSeekBarBinding}。
 */
// 各 tint attr ID（API 21 引入）在低版本上查询落空，写回另有 SDK 守卫。
@SuppressLint("InlinedApi")
public class ProgressBarBinding extends ViewBinding {

    public static final int ATTR_PROGRESS_DRAWABLE = android.R.attr.progressDrawable;
    public static final int ATTR_INDETERMINATE_DRAWABLE = android.R.attr.indeterminateDrawable;
    public static final int ATTR_PROGRESS_TINT = android.R.attr.progressTint;
    public static final int ATTR_INDETERMINATE_TINT = android.R.attr.indeterminateTint;
    public static final int ATTR_PROGRESS_BACKGROUND_TINT = android.R.attr.progressBackgroundTint;
    public static final int ATTR_SECONDARY_PROGRESS_TINT = android.R.attr.secondaryProgressTint;

    public ProgressBarBinding(@NonNull ProgressBar view) {
        super(view);
        bindRaw(ATTR_PROGRESS_DRAWABLE, DrawableRef::of, this::applyProgressDrawable);
        bindRaw(ATTR_INDETERMINATE_DRAWABLE, DrawableRef::of, this::applyIndeterminateDrawable);
        bindRaw(ATTR_PROGRESS_TINT, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId), this::applyProgressTint);
        bindRaw(ATTR_INDETERMINATE_TINT, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId), this::applyIndeterminateTint);
        bindRaw(ATTR_PROGRESS_BACKGROUND_TINT, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId), this::applyProgressBackgroundTint);
        bindRaw(ATTR_SECONDARY_PROGRESS_TINT, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId), this::applySecondaryProgressTint);
    }

    @NonNull
    @Override
    public ProgressBar getView() {
        return (ProgressBar) view;
    }

    public void setProgressDrawable(@NonNull DrawableRef drawable) {
        setAttrAndApply(ATTR_PROGRESS_DRAWABLE, drawable);
    }

    public void setProgressDrawable(@DrawableRes int resId) {
        setAttrResource(ATTR_PROGRESS_DRAWABLE, resId, DrawableRef::of);
    }

    public void setIndeterminateDrawable(@NonNull DrawableRef drawable) {
        setAttrAndApply(ATTR_INDETERMINATE_DRAWABLE, drawable);
    }

    public void setIndeterminateDrawable(@DrawableRes int resId) {
        setAttrResource(ATTR_INDETERMINATE_DRAWABLE, resId, DrawableRef::of);
    }

    public void setProgressTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_PROGRESS_TINT, tint);
    }

    public void setProgressTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_PROGRESS_TINT, tint);
    }

    public void setProgressTint(@ColorRes int tint) {
        setAttrResource(ATTR_PROGRESS_TINT, tint,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    public void setIndeterminateTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_INDETERMINATE_TINT, tint);
    }

    public void setIndeterminateTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_INDETERMINATE_TINT, tint);
    }

    public void setIndeterminateTint(@ColorRes int tint) {
        setAttrResource(ATTR_INDETERMINATE_TINT, tint, resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    public void setProgressBackgroundTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_PROGRESS_BACKGROUND_TINT, tint);
    }

    public void setProgressBackgroundTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_PROGRESS_BACKGROUND_TINT, tint);
    }

    public void setProgressBackgroundTint(@ColorRes int tint) {
        setAttrResource(ATTR_PROGRESS_BACKGROUND_TINT, tint,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    public void setSecondaryProgressTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_SECONDARY_PROGRESS_TINT, tint);
    }

    public void setSecondaryProgressTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_SECONDARY_PROGRESS_TINT, tint);
    }

    public void setSecondaryProgressTint(@ColorRes int tint) {
        setAttrResource(ATTR_SECONDARY_PROGRESS_TINT, tint,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    private void applyProgressDrawable(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            getView().setProgressDrawable(((DrawableRef) ref).provide(resolver));
        }
    }

    private void applyIndeterminateDrawable(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            getView().setIndeterminateDrawable(((DrawableRef) ref).provide(resolver));
        }
    }

    private void applyProgressTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        getView().setProgressTintList(resolveTint(resolver, ref));
    }

    private void applyIndeterminateTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        getView().setIndeterminateTintList(resolveTint(resolver, ref));
    }

    private void applyProgressBackgroundTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        getView().setProgressBackgroundTintList(resolveTint(resolver, ref));
    }

    private void applySecondaryProgressTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        getView().setSecondaryProgressTintList(resolveTint(resolver, ref));
    }
}
