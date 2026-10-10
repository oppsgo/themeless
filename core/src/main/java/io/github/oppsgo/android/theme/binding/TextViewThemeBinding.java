package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

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
 * {@link TextView} 绑定：textColor / textColorHint / drawableTint 与 compound drawable。
 * 文字颜色按资源类型分成 {@link ColorRef} 和 {@link ColorStateListRef}。
 * <p>
 * compound drawable：与平台 API 一样整组写入四个方向，统一由 {@link #applySpecialAttrs} 刷；
 * {@link DrawableRef#none()} 表示该方向为空。尺寸沿用 {@code Drawable.setBounds}，没设过才用 intrinsic。
 */
public class TextViewThemeBinding extends ViewBinding {

    public static final int ATTR_TEXT_COLOR = android.R.attr.textColor;
    public static final int ATTR_TEXT_COLOR_HINT = android.R.attr.textColorHint;
    public static final int ATTR_DRAWABLE_TINT = android.R.attr.drawableTint;
    public static final int ATTR_DRAWABLE_LEFT = android.R.attr.drawableLeft;
    public static final int ATTR_DRAWABLE_TOP = android.R.attr.drawableTop;
    public static final int ATTR_DRAWABLE_RIGHT = android.R.attr.drawableRight;
    public static final int ATTR_DRAWABLE_BOTTOM = android.R.attr.drawableBottom;
    public static final int ATTR_DRAWABLE_START = android.R.attr.drawableStart;
    public static final int ATTR_DRAWABLE_END = android.R.attr.drawableEnd;

    public TextViewThemeBinding(@NonNull TextView view) {
        super(view);
        bindRaw(ATTR_TEXT_COLOR,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) -> applyTextColor(false, resolver, ref));
        bindRaw(ATTR_TEXT_COLOR_HINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) -> applyTextColor(true, resolver, ref));
        bindRaw(ATTR_DRAWABLE_TINT,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId),
                (resolver, ref) ->
                        resolver.getViewCompat().setCompoundDrawableTintList(getView(), resolveTint(resolver, ref)));
        bindSpecial(ATTR_DRAWABLE_LEFT, DrawableRef::of);
        bindSpecial(ATTR_DRAWABLE_TOP, DrawableRef::of);
        bindSpecial(ATTR_DRAWABLE_RIGHT, DrawableRef::of);
        bindSpecial(ATTR_DRAWABLE_BOTTOM, DrawableRef::of);
        bindSpecial(ATTR_DRAWABLE_START, DrawableRef::of);
        bindSpecial(ATTR_DRAWABLE_END, DrawableRef::of);
    }

    @NonNull
    @Override
    public TextView getView() {
        return (TextView) view;
    }

    public void setTextColor(@NonNull ColorRef color) {
        setAttrAndApply(ATTR_TEXT_COLOR, color);
    }

    public void setTextColor(@NonNull ColorStateListRef color) {
        setAttrAndApply(ATTR_TEXT_COLOR, color);
    }

    public void setTextColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TEXT_COLOR, colorRes,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    public void setHintTextColor(@NonNull ColorRef color) {
        setAttrAndApply(ATTR_TEXT_COLOR_HINT, color);
    }

    public void setHintTextColor(@NonNull ColorStateListRef color) {
        setAttrAndApply(ATTR_TEXT_COLOR_HINT, color);
    }

    public void setHintTextColor(@ColorRes int colorRes) {
        setAttrResource(ATTR_TEXT_COLOR_HINT, colorRes,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    public void setCompoundDrawableTint(@NonNull ColorRef tint) {
        setAttrAndApply(ATTR_DRAWABLE_TINT, tint);
    }

    public void setCompoundDrawableTint(@NonNull ColorStateListRef tint) {
        setAttrAndApply(ATTR_DRAWABLE_TINT, tint);
    }

    public void setCompoundDrawableTint(@ColorRes int tint) {
        setAttrResource(ATTR_DRAWABLE_TINT, tint,
                resourceId -> ResourceRef.fromColor(resourceTable(), resourceId));
    }

    public void setCompoundDrawablesWithIntrinsicBounds(
            @DrawableRes int left, @DrawableRes int top,
            @DrawableRes int right, @DrawableRes int bottom) {
        setCompoundDrawablesWithIntrinsicBounds(
                drawableOrNone(left), drawableOrNone(top),
                drawableOrNone(right), drawableOrNone(bottom));
    }

    /**
     * 与平台 API 一样一次覆盖四个方向；传 {@code null} 按 {@link DrawableRef#none()} 写入（该方向为空）。
     */
    public void setCompoundDrawablesWithIntrinsicBounds(
            @Nullable DrawableRef left, @Nullable DrawableRef top,
            @Nullable DrawableRef right, @Nullable DrawableRef bottom) {
        setAttr(ATTR_DRAWABLE_LEFT, drawableOrNone(left));
        setAttr(ATTR_DRAWABLE_TOP, drawableOrNone(top));
        setAttr(ATTR_DRAWABLE_RIGHT, drawableOrNone(right));
        setAttr(ATTR_DRAWABLE_BOTTOM, drawableOrNone(bottom));
        unbind(ATTR_DRAWABLE_START);
        unbind(ATTR_DRAWABLE_END);
        applyCompoundDrawablesNow();
    }

    public void setCompoundDrawablesRelativeWithIntrinsicBounds(
            @DrawableRes int start, @DrawableRes int top,
            @DrawableRes int end, @DrawableRes int bottom) {
        setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawableOrNone(start), drawableOrNone(top),
                drawableOrNone(end), drawableOrNone(bottom));
    }

    /**
     * 与平台 API 一样一次覆盖四个方向；传 {@code null} 按 {@link DrawableRef#none()} 写入（该方向为空）。
     */
    public void setCompoundDrawablesRelativeWithIntrinsicBounds(
            @Nullable DrawableRef start, @Nullable DrawableRef top,
            @Nullable DrawableRef end, @Nullable DrawableRef bottom) {
        setAttr(ATTR_DRAWABLE_START, drawableOrNone(start));
        setAttr(ATTR_DRAWABLE_TOP, drawableOrNone(top));
        setAttr(ATTR_DRAWABLE_END, drawableOrNone(end));
        setAttr(ATTR_DRAWABLE_BOTTOM, drawableOrNone(bottom));
        unbind(ATTR_DRAWABLE_LEFT);
        unbind(ATTR_DRAWABLE_RIGHT);
        applyCompoundDrawablesNow();
    }

    @Override
    protected void applySpecialAttrs(@NonNull ResourceResolver resolver) {
        applyCompoundDrawables(resolver);
    }

    private void applyCompoundDrawablesNow() {
        applyCompoundDrawables(currentResolver());
    }

    /** 有任一方向的 {@link DrawableRef} 时整组写入；未跟踪的方向为 null。 */
    private void applyCompoundDrawables(@NonNull ResourceResolver resolver) {
        boolean relative = isDrawableTracked(ATTR_DRAWABLE_START) || isDrawableTracked(ATTR_DRAWABLE_END);
        boolean any = relative
                || isDrawableTracked(ATTR_DRAWABLE_LEFT)
                || isDrawableTracked(ATTR_DRAWABLE_TOP)
                || isDrawableTracked(ATTR_DRAWABLE_RIGHT)
                || isDrawableTracked(ATTR_DRAWABLE_BOTTOM);
        if (!any) return;

        Drawable top = resolveDrawable(resolver, ATTR_DRAWABLE_TOP);
        Drawable bottom = resolveDrawable(resolver, ATTR_DRAWABLE_BOTTOM);
        if (relative) {
            Drawable start = resolveDrawable(resolver, ATTR_DRAWABLE_START);
            Drawable end = resolveDrawable(resolver, ATTR_DRAWABLE_END);
            keepBounds(start);
            keepBounds(top);
            keepBounds(end);
            keepBounds(bottom);
            getView().setCompoundDrawablesRelative(start, top, end, bottom);
        } else {
            Drawable left = resolveDrawable(resolver, ATTR_DRAWABLE_LEFT);
            Drawable right = resolveDrawable(resolver, ATTR_DRAWABLE_RIGHT);
            keepBounds(left);
            keepBounds(top);
            keepBounds(right);
            keepBounds(bottom);
            getView().setCompoundDrawables(left, top, right, bottom);
        }
    }

    private boolean isDrawableTracked(@AttrRes int attr) {
        return getAttrValue(attr) instanceof DrawableRef;
    }

    @Nullable
    private Drawable resolveDrawable(@NonNull ResourceResolver resolver, @AttrRes int attr) {
        ResourceRef<?> ref = getAttrValue(attr);
        if (ref instanceof DrawableRef) {
            return ((DrawableRef) ref).provide(resolver);
        }
        return null;
    }

    @NonNull
    private static DrawableRef drawableOrNone(@Nullable DrawableRef ref) {
        return ref != null ? ref : DrawableRef.none();
    }

    @NonNull
    private static DrawableRef drawableOrNone(@DrawableRes int resId) {
        return resId == ResourceRef.ID_NULL ? DrawableRef.none() : DrawableRef.of(resId);
    }

    /** 已经 {@link Drawable#setBounds} 的沿用；没设过的才用 intrinsic 尺寸。 */
    private static void keepBounds(@Nullable Drawable drawable) {
        if (drawable == null) return;
        Rect bounds = drawable.getBounds();
        if (!bounds.isEmpty()) return;
        int width = Math.max(drawable.getIntrinsicWidth(), 0);
        int height = Math.max(drawable.getIntrinsicHeight(), 0);
        drawable.setBounds(0, 0, width, height);
    }

    private void applyTextColor(
            boolean hint,
            @NonNull ResourceResolver resolver,
            @NonNull ResourceRef<?> ref
    ) {
        if (ref instanceof ColorRef) {
            Integer color = ((ColorRef) ref).provide(resolver);
            if (color == null) return;
            if (hint) getView().setHintTextColor(color);
            else getView().setTextColor(color);
            return;
        }
        if (ref instanceof ColorStateListRef) {
            ColorStateList colors = ((ColorStateListRef) ref).provide(resolver);
            if (colors == null) return;
            if (hint) getView().setHintTextColor(colors);
            else getView().setTextColor(colors);
        }
    }
}
