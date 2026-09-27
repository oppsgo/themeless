package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.widget.ProgressBar;

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
 * {@link ProgressBar}：跟踪 progress / indeterminate drawable 及其 tint。
 * {@link android.widget.AbsSeekBar} 见 {@link AbsSeekBarResourceBinding}。
 */
public class ProgressBarResourceBinding extends ViewResourceBinding {

    public static final int ATTR_PROGRESS_DRAWABLE = android.R.attr.progressDrawable;
    public static final int ATTR_INDETERMINATE_DRAWABLE = android.R.attr.indeterminateDrawable;
    public static final int ATTR_PROGRESS_TINT = android.R.attr.progressTint;
    public static final int ATTR_INDETERMINATE_TINT = android.R.attr.indeterminateTint;
    public static final int ATTR_PROGRESS_BACKGROUND_TINT = android.R.attr.progressBackgroundTint;
    public static final int ATTR_SECONDARY_PROGRESS_TINT = android.R.attr.secondaryProgressTint;

    public ProgressBarResourceBinding(@NonNull ProgressBar view) {
        super(view);
    }

    @NonNull
    @Override
    public ProgressBar getView() {
        return (ProgressBar) view;
    }

    @NonNull
    public static ProgressBarResourceBinding of(@NonNull ProgressBar view) {
        return of(view, ProgressBarResourceBinding.class, ProgressBarResourceBinding::new);
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(super.getViewStyleable(),
                ATTR_PROGRESS_DRAWABLE,
                ATTR_INDETERMINATE_DRAWABLE,
                ATTR_PROGRESS_TINT,
                ATTR_INDETERMINATE_TINT,
                ATTR_PROGRESS_BACKGROUND_TINT,
                ATTR_SECONDARY_PROGRESS_TINT);
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (attr == ATTR_PROGRESS_DRAWABLE || attr == ATTR_INDETERMINATE_DRAWABLE) {
            return resId == ID_NULL ? null : DrawableRef.of(resId);
        }
        if (attr == ATTR_PROGRESS_TINT
                || attr == ATTR_INDETERMINATE_TINT
                || attr == ATTR_PROGRESS_BACKGROUND_TINT
                || attr == ATTR_SECONDARY_PROGRESS_TINT) {
            return createColorResource(resId);
        }
        return super.createResource(attr, resId);
    }

    @NonNull
    public ProgressBarResourceBinding setProgressDrawable(ResourceRef<?> drawable) {
        putAndUpdate(ATTR_PROGRESS_DRAWABLE, drawable);
        return this;
    }

    @NonNull
    public ProgressBarResourceBinding setProgressDrawable(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_PROGRESS_DRAWABLE);
            return this;
        }
        return setProgressDrawable(DrawableRef.of(resId));
    }

    @NonNull
    public ProgressBarResourceBinding setIndeterminateDrawable(ResourceRef<?> drawable) {
        putAndUpdate(ATTR_INDETERMINATE_DRAWABLE, drawable);
        return this;
    }

    @NonNull
    public ProgressBarResourceBinding setIndeterminateDrawable(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_INDETERMINATE_DRAWABLE);
            return this;
        }
        return setIndeterminateDrawable(DrawableRef.of(resId));
    }

    @NonNull
    public ProgressBarResourceBinding setProgressTint(ResourceRef<?> tint) {
        putAndUpdate(ATTR_PROGRESS_TINT, tint);
        return this;
    }

    @NonNull
    public ProgressBarResourceBinding setProgressTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_PROGRESS_TINT);
            return this;
        }
        return setProgressTint(createColorResource(tint));
    }

    @NonNull
    public ProgressBarResourceBinding setIndeterminateTint(ResourceRef<?> tint) {
        putAndUpdate(ATTR_INDETERMINATE_TINT, tint);
        return this;
    }

    @NonNull
    public ProgressBarResourceBinding setIndeterminateTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_INDETERMINATE_TINT);
            return this;
        }
        return setIndeterminateTint(createColorResource(tint));
    }

    @NonNull
    public ProgressBarResourceBinding setProgressBackgroundTint(ResourceRef<?> tint) {
        putAndUpdate(ATTR_PROGRESS_BACKGROUND_TINT, tint);
        return this;
    }

    @NonNull
    public ProgressBarResourceBinding setProgressBackgroundTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_PROGRESS_BACKGROUND_TINT);
            return this;
        }
        return setProgressBackgroundTint(createColorResource(tint));
    }

    @NonNull
    public ProgressBarResourceBinding setSecondaryProgressTint(ResourceRef<?> tint) {
        putAndUpdate(ATTR_SECONDARY_PROGRESS_TINT, tint);
        return this;
    }

    @NonNull
    public ProgressBarResourceBinding setSecondaryProgressTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_SECONDARY_PROGRESS_TINT);
            return this;
        }
        return setSecondaryProgressTint(createColorResource(tint));
    }

    @Override
    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr, @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_PROGRESS_DRAWABLE && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getView().setProgressDrawable(drawable);
            return;
        }
        if (attr == ATTR_INDETERMINATE_DRAWABLE && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getView().setIndeterminateDrawable(drawable);
            return;
        }
        if (attr == ATTR_PROGRESS_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getView().setProgressTintList(tint);
            }
            return;
        }
        if (attr == ATTR_INDETERMINATE_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getView().setIndeterminateTintList(tint);
            }
            return;
        }
        if (attr == ATTR_PROGRESS_BACKGROUND_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getView().setProgressBackgroundTintList(tint);
            }
            return;
        }
        if (attr == ATTR_SECONDARY_PROGRESS_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getView().setSecondaryProgressTintList(tint);
            }
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }
}
