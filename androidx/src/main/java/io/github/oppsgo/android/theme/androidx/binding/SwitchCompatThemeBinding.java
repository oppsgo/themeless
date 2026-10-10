package io.github.oppsgo.android.theme.androidx.binding;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link SwitchCompat}：在 {@link AppCompatCompoundButtonThemeBinding} 之上跟踪
 * thumb / track 及其 tint，并把布局里的 {@code app:thumbTint} / {@code app:trackTint}
 * 归一到平台同名属性。
 * <p>
 * 注意 SwitchCompat 继承自 {@code CompoundButton}，不是系统 {@code Switch}，
 * 所以不复用 {@code SwitchThemeBinding}。
 */
public class SwitchCompatThemeBinding extends AppCompatCompoundButtonThemeBinding {

    public static final int ATTR_THUMB = android.R.attr.thumb;
    public static final int ATTR_TRACK = android.R.attr.track;
    public static final int ATTR_THUMB_TINT = android.R.attr.thumbTint;
    public static final int ATTR_TRACK_TINT = android.R.attr.trackTint;

    /** {@code app:thumbTint} → 平台 {@code android:thumbTint}。 */
    public static final int ATTR_THUMB_TINT_COMPAT = androidx.appcompat.R.attr.thumbTint;
    /** {@code app:trackTint} → 平台 {@code android:trackTint}。 */
    public static final int ATTR_TRACK_TINT_COMPAT = androidx.appcompat.R.attr.trackTint;

    public SwitchCompatThemeBinding(@NonNull SwitchCompat view) {
        super(view);
        bindRaw(ATTR_THUMB, DrawableRef::of, (resolver, ref) -> applyThumb(resolver, ref));
        bindRaw(ATTR_TRACK, DrawableRef::of, (resolver, ref) -> applyTrack(resolver, ref));
        bindRaw(ATTR_THUMB_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) -> applyThumbTint(resolver, ref));
        bindRaw(ATTR_TRACK_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) -> applyTrackTint(resolver, ref));
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
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    public void setTrackTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_TRACK_TINT, tint);
    }

    public void setTrackTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_TRACK_TINT, tint);
    }

    public void setTrackTint(@ColorRes int tint) {
        setAttrResource(ATTR_TRACK_TINT, tint,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    private void applyThumb(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            getView().setThumbDrawable(((DrawableRef) ref).provide(resolver));
        }
    }

    private void applyTrack(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (ref instanceof DrawableRef) {
            getView().setTrackDrawable(((DrawableRef) ref).provide(resolver));
        }
    }

    private void applyThumbTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        getView().setThumbTintList(resolveTint(resolver, ref));
    }

    private void applyTrackTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        getView().setTrackTintList(resolveTint(resolver, ref));
    }
}
