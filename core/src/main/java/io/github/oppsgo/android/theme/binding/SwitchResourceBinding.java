package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.Switch;

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
 * 系统 {@link Switch}：在 CompoundButton 之上跟踪 thumb / track 及其 tint。
 * {@code SwitchCompat} 请放在 AppCompat 适配模块。
 */
public class SwitchResourceBinding extends CompoundButtonResourceBinding {

    public static final int ATTR_THUMB = android.R.attr.thumb;
    public static final int ATTR_TRACK = android.R.attr.track;
    public static final int ATTR_THUMB_TINT = android.R.attr.thumbTint;
    public static final int ATTR_TRACK_TINT = android.R.attr.trackTint;

    public SwitchResourceBinding(@NonNull Switch view) {
        super(view);
    }

    @NonNull
    public static SwitchResourceBinding of(@NonNull Switch view) {
        return of(view, SwitchResourceBinding.class, SwitchResourceBinding::new);
    }

    @NonNull
    protected Switch getSwitch() {
        return (Switch) view;
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(super.getViewStyleable(),
                ATTR_THUMB,
                ATTR_TRACK,
                ATTR_THUMB_TINT,
                ATTR_TRACK_TINT);
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (attr == ATTR_THUMB || attr == ATTR_TRACK) {
            return resId == ID_NULL ? null : DrawableRef.of(resId);
        }
        if (attr == ATTR_THUMB_TINT || attr == ATTR_TRACK_TINT) {
            return createColorResource(resId);
        }
        return super.createResource(attr, resId);
    }

    @NonNull
    public SwitchResourceBinding setThumbResource(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_THUMB);
            return this;
        }
        return setThumb(DrawableRef.of(resId));
    }

    @NonNull
    public SwitchResourceBinding setThumb(@NonNull DrawableRef thumb) {
        putAndUpdate(ATTR_THUMB, thumb);
        return this;
    }

    @NonNull
    public SwitchResourceBinding setTrackResource(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_TRACK);
            return this;
        }
        return setTrack(DrawableRef.of(resId));
    }

    @NonNull
    public SwitchResourceBinding setTrack(@NonNull DrawableRef track) {
        putAndUpdate(ATTR_TRACK, track);
        return this;
    }

    @NonNull
    public SwitchResourceBinding setThumbTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_THUMB_TINT);
            return this;
        }
        putAndUpdate(ATTR_THUMB_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public SwitchResourceBinding setThumbTint(@NonNull ColorRef tint) {
        putAndUpdate(ATTR_THUMB_TINT, tint);
        return this;
    }

    @NonNull
    public SwitchResourceBinding setThumbTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(ATTR_THUMB_TINT, tint);
        return this;
    }

    @NonNull
    public SwitchResourceBinding setTrackTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_TRACK_TINT);
            return this;
        }
        putAndUpdate(ATTR_TRACK_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public SwitchResourceBinding setTrackTint(@NonNull ColorRef tint) {
        putAndUpdate(ATTR_TRACK_TINT, tint);
        return this;
    }

    @NonNull
    public SwitchResourceBinding setTrackTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(ATTR_TRACK_TINT, tint);
        return this;
    }

    @Override
    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr, @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_THUMB && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getSwitch().setThumbDrawable(drawable);
            return;
        }
        if (attr == ATTR_TRACK && value instanceof DrawableRef) {
            Drawable drawable = ((DrawableRef) value).resolve(resolver);
            getSwitch().setTrackDrawable(drawable);
            return;
        }
        if (attr == ATTR_THUMB_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            resolver.getViewCompat().setThumbTintList(getSwitch(), tint);
            return;
        }
        if (attr == ATTR_TRACK_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            resolver.getViewCompat().setTrackTintList(getSwitch(), tint);
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }
}
