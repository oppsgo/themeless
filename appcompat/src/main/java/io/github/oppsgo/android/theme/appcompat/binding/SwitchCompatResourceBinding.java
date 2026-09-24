package io.github.oppsgo.android.theme.appcompat.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v7.appcompat.R;
import android.support.v7.widget.SwitchCompat;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.SwitchResourceBinding;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * Support {@link SwitchCompat}：在 CompoundButton AppCompat 别名之上跟踪 thumb / track 及其 tint。
 * 写入走 {@link SwitchCompat} 自身 API。
 */
public class SwitchCompatResourceBinding extends AppCompatCompoundButtonResourceBinding {

    public static final int ATTR_TRACK_COMPAT = R.attr.track;
    public static final int ATTR_THUMB_TINT_COMPAT = R.attr.thumbTint;
    public static final int ATTR_TRACK_TINT_COMPAT = R.attr.trackTint;

    public static void register() {
        ThemeManager.get().bindings().register(SwitchCompat.class, SwitchCompatResourceBinding::new);
    }

    public SwitchCompatResourceBinding(@NonNull SwitchCompat view) {
        super(view);
    }

    @NonNull
    public static SwitchCompatResourceBinding of(@NonNull SwitchCompat view) {
        return of(view, SwitchCompatResourceBinding.class, SwitchCompatResourceBinding::new);
    }

    @NonNull
    protected SwitchCompat getSwitchCompat() {
        return (SwitchCompat) view;
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(super.getViewStyleable(),
                SwitchResourceBinding.ATTR_THUMB,
                SwitchResourceBinding.ATTR_TRACK,
                SwitchResourceBinding.ATTR_THUMB_TINT,
                SwitchResourceBinding.ATTR_TRACK_TINT,
                ATTR_TRACK_COMPAT,
                ATTR_THUMB_TINT_COMPAT,
                ATTR_TRACK_TINT_COMPAT);
    }

    @AttrRes
    @Override
    protected int normalizeAttr(@AttrRes int attr) {
        if (attr == ATTR_TRACK_COMPAT) return SwitchResourceBinding.ATTR_TRACK;
        if (attr == ATTR_THUMB_TINT_COMPAT) return SwitchResourceBinding.ATTR_THUMB_TINT;
        if (attr == ATTR_TRACK_TINT_COMPAT) return SwitchResourceBinding.ATTR_TRACK_TINT;
        return super.normalizeAttr(attr);
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (attr == SwitchResourceBinding.ATTR_THUMB || attr == SwitchResourceBinding.ATTR_TRACK) {
            return resId == ID_NULL ? null : DrawableRef.of(resId);
        }
        if (attr == SwitchResourceBinding.ATTR_THUMB_TINT
                || attr == SwitchResourceBinding.ATTR_TRACK_TINT) {
            return createColorResource(resId);
        }
        return super.createResource(attr, resId);
    }

    @NonNull
    public SwitchCompatResourceBinding setThumbResource(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(SwitchResourceBinding.ATTR_THUMB);
            return this;
        }
        return setThumb(DrawableRef.of(resId));
    }

    @NonNull
    public SwitchCompatResourceBinding setThumb(@NonNull DrawableRef thumb) {
        putAndUpdate(SwitchResourceBinding.ATTR_THUMB, thumb);
        return this;
    }

    @NonNull
    public SwitchCompatResourceBinding setTrackResource(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(SwitchResourceBinding.ATTR_TRACK);
            return this;
        }
        return setTrack(DrawableRef.of(resId));
    }

    @NonNull
    public SwitchCompatResourceBinding setTrack(@NonNull DrawableRef track) {
        putAndUpdate(SwitchResourceBinding.ATTR_TRACK, track);
        return this;
    }

    @NonNull
    public SwitchCompatResourceBinding setThumbTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(SwitchResourceBinding.ATTR_THUMB_TINT);
            return this;
        }
        putAndUpdate(SwitchResourceBinding.ATTR_THUMB_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public SwitchCompatResourceBinding setThumbTint(@NonNull ColorRef tint) {
        putAndUpdate(SwitchResourceBinding.ATTR_THUMB_TINT, tint);
        return this;
    }

    @NonNull
    public SwitchCompatResourceBinding setThumbTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(SwitchResourceBinding.ATTR_THUMB_TINT, tint);
        return this;
    }

    @NonNull
    public SwitchCompatResourceBinding setTrackTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(SwitchResourceBinding.ATTR_TRACK_TINT);
            return this;
        }
        putAndUpdate(SwitchResourceBinding.ATTR_TRACK_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public SwitchCompatResourceBinding setTrackTint(@NonNull ColorRef tint) {
        putAndUpdate(SwitchResourceBinding.ATTR_TRACK_TINT, tint);
        return this;
    }

    @NonNull
    public SwitchCompatResourceBinding setTrackTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(SwitchResourceBinding.ATTR_TRACK_TINT, tint);
        return this;
    }

    @Override
    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr,
                                   @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == SwitchResourceBinding.ATTR_THUMB && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getSwitchCompat().setThumbDrawable(drawable);
            return;
        }
        if (attr == SwitchResourceBinding.ATTR_TRACK && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getSwitchCompat().setTrackDrawable(drawable);
            return;
        }
        if (attr == SwitchResourceBinding.ATTR_THUMB_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            getSwitchCompat().setThumbTintList(tint);
            return;
        }
        if (attr == SwitchResourceBinding.ATTR_TRACK_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            getSwitchCompat().setTrackTintList(tint);
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }
}
