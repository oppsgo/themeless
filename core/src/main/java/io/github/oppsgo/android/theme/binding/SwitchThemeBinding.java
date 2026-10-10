package io.github.oppsgo.android.theme.binding;

import android.os.Build;
import android.widget.Switch;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * 系统 {@link Switch}：在 CompoundButton 之上跟踪 thumb / track 及其 tint。
 * {@code SwitchCompat} 请放在 AppCompat 适配模块。
 */
public class SwitchThemeBinding extends CompoundButtonThemeBinding {

    public static final int ATTR_THUMB = android.R.attr.thumb;
    public static final int ATTR_TRACK = android.R.attr.track;
    public static final int ATTR_THUMB_TINT = android.R.attr.thumbTint;
    public static final int ATTR_TRACK_TINT = android.R.attr.trackTint;

    public SwitchThemeBinding(@NonNull Switch view) {
        super(view);
        bindRaw(ATTR_THUMB,
                DrawableRef::of,
                (resolver, ref) -> applyThumb(resolver, ref));
        bindRaw(ATTR_TRACK,
                DrawableRef::of,
                (resolver, ref) -> applyTrack(resolver, ref));
        bindRaw(ATTR_THUMB_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) -> applyThumbTint(resolver, ref));
        bindRaw(ATTR_TRACK_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) -> applyTrackTint(resolver, ref));
    }

    @NonNull
    @Override
    public Switch getView() {
        return (Switch) view;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        getView().setThumbTintList(resolveTint(resolver, ref));
    }

    private void applyTrackTint(@NonNull ResourceResolver resolver, @NonNull ResourceRef<?> ref) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        getView().setTrackTintList(resolveTint(resolver, ref));
    }
}
