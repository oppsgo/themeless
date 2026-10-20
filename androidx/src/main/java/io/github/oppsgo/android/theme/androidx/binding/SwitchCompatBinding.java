package io.github.oppsgo.android.theme.androidx.binding;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.androidx.R2;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;
import io.github.oppsgo.android.theme.resource.ResourceValue;

/**
 * {@link SwitchCompat}：在 {@link AppCompatCompoundButtonBinding} 之上跟踪
 * thumb / track 及其 tint，并把布局里的 {@code app:thumbTint} / {@code app:trackTint}
 * 归一到平台同名属性。
 * <p>
 * 注意 SwitchCompat 继承自 {@code CompoundButton}，不是系统 {@code Switch}，
 * 所以不复用 {@code SwitchBinding}。
 */
public class SwitchCompatBinding extends AppCompatCompoundButtonBinding {

    public static final int ATTR_THUMB = io.github.oppsgo.android.theme.R2.attr.thumb;
    public static final int ATTR_TRACK = io.github.oppsgo.android.theme.R2.attr.track;
    public static final int ATTR_THUMB_TINT = io.github.oppsgo.android.theme.R2.attr.thumbTint;
    public static final int ATTR_TRACK_TINT = io.github.oppsgo.android.theme.R2.attr.trackTint;

    /** {@code app:thumbTint} → 平台 {@code android:thumbTint}。 */
    public static final int ATTR_THUMB_TINT_COMPAT = R2.appcompat.attr.thumbTint;
    /** {@code app:trackTint} → 平台 {@code android:trackTint}。 */
    public static final int ATTR_TRACK_TINT_COMPAT = R2.appcompat.attr.trackTint;

    public SwitchCompatBinding(@NonNull SwitchCompat view) {
        super(view);
        bindRaw(ATTR_THUMB, DrawableRef::of, this::applyThumb);
        bindRaw(ATTR_TRACK, DrawableRef::of, this::applyTrack);
        bindRaw(ATTR_THUMB_TINT,
                colorFactory(),
                this::applyThumbTint);
        bindRaw(ATTR_TRACK_TINT,
                colorFactory(),
                this::applyTrackTint);
        alias(ATTR_THUMB_TINT_COMPAT, ATTR_THUMB_TINT);
        alias(ATTR_TRACK_TINT_COMPAT, ATTR_TRACK_TINT);
    }

    @NonNull
    @Override
    public SwitchCompat getView() {
        return (SwitchCompat) view;
    }

    public void setThumb(@NonNull DrawableRef thumb) {
        setAttrAndApply(ATTR_THUMB, thumb);
    }

    public void setThumbResource(@DrawableRes int resId) {
        setAttrResource(ATTR_THUMB, resId, DrawableRef::of);
    }

    public void setTrack(@NonNull DrawableRef track) {
        setAttrAndApply(ATTR_TRACK, track);
    }

    public void setTrackResource(@DrawableRes int resId) {
        setAttrResource(ATTR_TRACK, resId, DrawableRef::of);
    }

    public void setThumbTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_THUMB_TINT, tint);
    }

    public void setThumbTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_THUMB_TINT, tint);
    }

    public void setThumbTint(@ColorRes int tint) {
        setAttrResource(ATTR_THUMB_TINT, tint,
                colorFactory());
    }

    public void setTrackTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_TRACK_TINT, tint);
    }

    public void setTrackTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_TRACK_TINT, tint);
    }

    public void setTrackTint(@ColorRes int tint) {
        setAttrResource(ATTR_TRACK_TINT, tint,
                colorFactory());
    }

    private void applyThumb(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            ResourceValue<Drawable> thumb = ((DrawableRef) ref).provide(resolver);
            if (thumb.isPresent()) {
                getView().setThumbDrawable(thumb.get());
            }
        }
    }

    private void applyTrack(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            ResourceValue<Drawable> track = ((DrawableRef) ref).provide(resolver);
            if (track.isPresent()) {
                getView().setTrackDrawable(track.get());
            }
        }
    }

    private void applyThumbTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> tint = resolveTint(resolver, ref);
        if (tint.isPresent()) {
            getView().setThumbTintList(tint.get());
        }
    }

    private void applyTrackTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        ResourceValue<ColorStateList> tint = resolveTint(resolver, ref);
        if (tint.isPresent()) {
            getView().setTrackTintList(tint.get());
        }
    }
}
