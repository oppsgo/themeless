package io.github.oppsgo.android.theme.binding;

import android.annotation.SuppressLint;
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
// thumbTint（API 21）/ trackTint（API 23）attr ID 在低版本上查询落空，写回另有 SDK 守卫。
@SuppressLint("InlinedApi")
public class SwitchBinding extends CompoundButtonBinding {

    public static final int ATTR_THUMB = android.R.attr.thumb;
    public static final int ATTR_TRACK = android.R.attr.track;
    public static final int ATTR_THUMB_TINT = android.R.attr.thumbTint;
    public static final int ATTR_TRACK_TINT = android.R.attr.trackTint;

    public SwitchBinding(@NonNull Switch view) {
        super(view);
        bindRaw(ATTR_THUMB, DrawableRef::of, this::applyThumb);
        bindRaw(ATTR_TRACK, DrawableRef::of, this::applyTrack);
        bindRaw(ATTR_THUMB_TINT, colorFactory(), this::applyThumbTint);
        bindRaw(ATTR_TRACK_TINT, colorFactory(), this::applyTrackTint);
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
        setAttrResource(ATTR_THUMB_TINT, tint, colorFactory());
    }

    public void setTrackTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_TRACK_TINT, tint);
    }

    public void setTrackTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_TRACK_TINT, tint);
    }

    public void setTrackTint(@ColorRes int tint) {
        setAttrResource(ATTR_TRACK_TINT, tint, colorFactory());
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
