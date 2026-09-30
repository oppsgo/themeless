package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DimenRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link TextView} 绑定。文字颜色按资源类型分成 {@link ColorRef} 和 {@link ColorStateListRef}。
 * <p>
 * compound drawable：属性上有 {@link DrawableRef} 则按 resolve 写入（{@link DrawableRef#none()} /
 * resolve 为 null 表示显式清空）；无 DrawableRef 则沿用 View 上已有 Drawable，刷新不会清掉未跟踪的方向。
 * 尺寸沿用 {@code Drawable.setBounds}，没设过才用 intrinsic。
 */
public class TextViewResourceBinding extends ViewResourceBinding {

    public static final int ATTR_TEXT_COLOR = android.R.attr.textColor;
    public static final int ATTR_TEXT_COLOR_HINT = android.R.attr.textColorHint;
    public static final int ATTR_TEXT_SIZE = android.R.attr.textSize;
    public static final int ATTR_DRAWABLE_LEFT = android.R.attr.drawableLeft;
    public static final int ATTR_DRAWABLE_TOP = android.R.attr.drawableTop;
    public static final int ATTR_DRAWABLE_RIGHT = android.R.attr.drawableRight;
    public static final int ATTR_DRAWABLE_BOTTOM = android.R.attr.drawableBottom;
    public static final int ATTR_DRAWABLE_START = android.R.attr.drawableStart;
    public static final int ATTR_DRAWABLE_END = android.R.attr.drawableEnd;
    public static final int ATTR_DRAWABLE_TINT = android.R.attr.drawableTint;

    /**
     * inflate 时是否自动跟肤 {@code android:textSize}。默认关闭。
     * 全局默认的跟踪行为，使用 volatile 保证可见性。
     * 默认为 false，即不追踪 textSize 变化。
     */
    private static volatile boolean sDefaultTrackTextSize = false;

    /**
     * 全局开关：inflate 时是否自动跟踪 {@code android:textSize}。
     * 默认 {@code false}。手动 {@link #setTextSize} 不受影响。须在相关布局 inflate 之前设置。
     */
    public static void setDefaultTrackTextSize(boolean track) {
        sDefaultTrackTextSize = track;
    }

    /**
     * 获取全局默认配置
     */
    public static boolean getDefaultTrackTextSize() {
        return sDefaultTrackTextSize;
    }

    public TextViewResourceBinding(@NonNull TextView view) {
        super(view);
    }

    @NonNull
    @Override
    public TextView getView() {
        return (TextView) view;
    }

    /**
     * 已有本类或子类就返回；否则给一个不挂到 View 上的实例。
     */
    @NonNull
    public static TextViewResourceBinding of(@NonNull TextView view) {
        return of(view, TextViewResourceBinding.class, TextViewResourceBinding::new);
    }


    /**
     * 实例级别的覆盖配置。
     * null 表示使用全局默认值；true/false 表示针对当前 View 的显式覆盖。
     */
    @Nullable
    private Boolean trackTextSize;

    /**
     * 为当前 TextView 实例设置 trackTextSize，将覆盖全局默认配置
     */
    public void setTrackTextSize(boolean track) {
        this.trackTextSize = track;
    }

    /**
     * 重置为全局默认配置
     */
    public void resetTrackTextSize() {
        this.trackTextSize = null;
    }

    /**
     * 内部方法：获取当前实际生效的配置
     * 优先级：实例覆盖配置 > 全局默认配置
     */
    protected boolean isTrackTextSize() {
        return trackTextSize != null ? trackTextSize : sDefaultTrackTextSize;
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(
                super.getViewStyleable(),
                ATTR_TEXT_COLOR,
                ATTR_TEXT_COLOR_HINT,
                ATTR_TEXT_SIZE,
                ATTR_DRAWABLE_LEFT,
                ATTR_DRAWABLE_TOP,
                ATTR_DRAWABLE_RIGHT,
                ATTR_DRAWABLE_BOTTOM,
                ATTR_DRAWABLE_START,
                ATTR_DRAWABLE_END,
                ATTR_DRAWABLE_TINT
        );
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (resId == ID_NULL) return null;
        switch (attr) {
            case ATTR_TEXT_COLOR:
            case ATTR_TEXT_COLOR_HINT:
            case ATTR_DRAWABLE_TINT:
                return createColorResource(resId);
            case ATTR_TEXT_SIZE:
                return DimenRef.of(resId);
            case ATTR_DRAWABLE_LEFT:
            case ATTR_DRAWABLE_TOP:
            case ATTR_DRAWABLE_RIGHT:
            case ATTR_DRAWABLE_BOTTOM:
            case ATTR_DRAWABLE_START:
            case ATTR_DRAWABLE_END:
                return DrawableRef.of(resId);
            default:
                return super.createResource(attr, resId);
        }
    }

    @NonNull
    public TextViewResourceBinding setTextColor(ResourceRef<?> color) {
        putAndUpdate(ATTR_TEXT_COLOR, color);
        return this;
    }

    @NonNull
    public TextViewResourceBinding setTextColor(@ColorRes int colorRes) {
        if (colorRes == ID_NULL) {
            unbind(ATTR_TEXT_COLOR);
            return this;
        }
        return setTextColor(isPureColor(colorRes) ? ColorRef.of(colorRes) : ColorStateListRef.of(colorRes));
    }

    @NonNull
    public TextViewResourceBinding setHintTextColor(ResourceRef<?> color) {
        putAndUpdate(ATTR_TEXT_COLOR_HINT, color);
        return this;
    }

    @NonNull
    public TextViewResourceBinding setHintTextColor(@ColorRes int colorRes) {
        if (colorRes == ID_NULL) {
            unbind(ATTR_TEXT_COLOR_HINT);
            return this;
        }
        return setHintTextColor(isPureColor(colorRes) ? ColorRef.of(colorRes) : ColorStateListRef.of(colorRes));
    }

    /**
     * 用 {@link ResourceResolver#getDimension} 取出 px（{@code sp} 会跟 {@code fontScale}），
     * 再 {@code setTextSize(PX, ...)}。
     */
    @NonNull
    public TextViewResourceBinding setTextSize(ResourceRef<?> size) {
        if (isTrackTextSize()) {
            putAndUpdate(ATTR_TEXT_SIZE, size);
        } else {
            putAttr(ATTR_TEXT_SIZE, size);
            ResourceResolver resolver = currentResolver();
            if (resolver != null) applyTextSize(size, resolver);
        }
        return this;
    }

    @NonNull
    public TextViewResourceBinding setTextSize(@DimenRes int size) {
        if (size == ID_NULL) {
            unbind(ATTR_TEXT_SIZE);
            return this;
        }
        return setTextSize(DimenRef.of(size));
    }

    @NonNull
    public TextViewResourceBinding setCompoundDrawableTint(ResourceRef<?> tint) {
        putAndUpdate(ATTR_DRAWABLE_TINT, tint);
        return this;
    }

    @NonNull
    public TextViewResourceBinding setCompoundDrawableTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_DRAWABLE_TINT);
            return this;
        }
        return setCompoundDrawableTint(createColorResource(tint));
    }

    @NonNull
    public TextViewResourceBinding setCompoundDrawablesWithIntrinsicBounds(
            @DrawableRes int left, @DrawableRes int top,
            @DrawableRes int right, @DrawableRes int bottom) {
        return setCompoundDrawablesWithIntrinsicBounds(
                drawableOrNull(left), drawableOrNull(top),
                drawableOrNull(right), drawableOrNull(bottom));
    }

    /**
     * @param left / top / right / bottom 为 null 表示该方向不跟踪（刷新时沿用已有）；
     *             {@link DrawableRef#none()} 或 resolve 为 null 表示显式清空。
     */
    @NonNull
    public TextViewResourceBinding setCompoundDrawablesWithIntrinsicBounds(
            @Nullable DrawableRef left, @Nullable DrawableRef top,
            @Nullable DrawableRef right, @Nullable DrawableRef bottom) {
        putAttr(ATTR_DRAWABLE_LEFT, left);
        putAttr(ATTR_DRAWABLE_TOP, top);
        putAttr(ATTR_DRAWABLE_RIGHT, right);
        putAttr(ATTR_DRAWABLE_BOTTOM, bottom);
        attributes.delete(ATTR_DRAWABLE_START);
        attributes.delete(ATTR_DRAWABLE_END);
        applyCompoundDrawablesNow();
        return this;
    }

    @NonNull
    public TextViewResourceBinding setCompoundDrawablesRelativeWithIntrinsicBounds(
            @DrawableRes int start, @DrawableRes int top,
            @DrawableRes int end, @DrawableRes int bottom) {
        return setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawableOrNull(start), drawableOrNull(top),
                drawableOrNull(end), drawableOrNull(bottom));
    }

    /**
     * @param start / top / end / bottom 为 null 表示该方向不跟踪（刷新时沿用已有）；
     *             {@link DrawableRef#none()} 或 resolve 为 null 表示显式清空。
     */
    @NonNull
    public TextViewResourceBinding setCompoundDrawablesRelativeWithIntrinsicBounds(
            @Nullable DrawableRef start, @Nullable DrawableRef top,
            @Nullable DrawableRef end, @Nullable DrawableRef bottom) {
        putAttr(ATTR_DRAWABLE_START, start);
        putAttr(ATTR_DRAWABLE_TOP, top);
        putAttr(ATTR_DRAWABLE_END, end);
        putAttr(ATTR_DRAWABLE_BOTTOM, bottom);
        attributes.delete(ATTR_DRAWABLE_LEFT);
        attributes.delete(ATTR_DRAWABLE_RIGHT);
        applyCompoundDrawablesNow();
        return this;
    }

    private DrawableRef drawableOrNull(@DrawableRes int resId) {
        return resId == ID_NULL ? DrawableRef.none() : DrawableRef.of(resId);
    }

    private void applyCompoundDrawablesNow() {
        ResourceResolver resolver = currentResolver();
        if (resolver != null) {
            applyCompoundDrawables(resolver);
        }
    }

    @Override
    protected void invalidate(@NonNull ResourceResolver resolver) {
        applyCompoundDrawables(resolver);
        super.invalidate(resolver);
    }

    /**
     * 有 {@link DrawableRef} 则按 resolve 结果写入（含显式 null）；
     * 属性未跟踪（无 DrawableRef）则沿用 View 上现有 Drawable。
     * 四个方向都未跟踪时不改 compound drawable。
     */
    private void applyCompoundDrawables(@NonNull ResourceResolver resolver) {
        boolean trackStart = attributes.get(ATTR_DRAWABLE_START) instanceof DrawableRef;
        boolean trackEnd = attributes.get(ATTR_DRAWABLE_END) instanceof DrawableRef;
        boolean trackLeft = attributes.get(ATTR_DRAWABLE_LEFT) instanceof DrawableRef;
        boolean trackRight = attributes.get(ATTR_DRAWABLE_RIGHT) instanceof DrawableRef;
        boolean trackTop = attributes.get(ATTR_DRAWABLE_TOP) instanceof DrawableRef;
        boolean trackBottom = attributes.get(ATTR_DRAWABLE_BOTTOM) instanceof DrawableRef;
        if (!trackStart && !trackEnd && !trackLeft && !trackRight && !trackTop && !trackBottom) {
            return;
        }

        TextView textView = getView();
        Drawable[] absolute = textView.getCompoundDrawables();
        Drawable[] relative = textView.getCompoundDrawablesRelative();

        Drawable top = resolveCompoundDrawable(resolver, ATTR_DRAWABLE_TOP, absolute[1]);
        Drawable bottom = resolveCompoundDrawable(resolver, ATTR_DRAWABLE_BOTTOM, absolute[3]);

        if (trackStart || trackEnd) {
            Drawable start = resolveCompoundDrawable(resolver, ATTR_DRAWABLE_START, relative[0]);
            Drawable end = resolveCompoundDrawable(resolver, ATTR_DRAWABLE_END, relative[2]);
            keepBounds(start);
            keepBounds(top);
            keepBounds(end);
            keepBounds(bottom);
            textView.setCompoundDrawablesRelative(start, top, end, bottom);
        } else {
            Drawable left = resolveCompoundDrawable(resolver, ATTR_DRAWABLE_LEFT, absolute[0]);
            Drawable right = resolveCompoundDrawable(resolver, ATTR_DRAWABLE_RIGHT, absolute[2]);
            keepBounds(left);
            keepBounds(top);
            keepBounds(right);
            keepBounds(bottom);
            textView.setCompoundDrawables(left, top, right, bottom);
        }
    }

    /**
     * {@link DrawableRef} 存在则用 resolve（可为 null）；否则复用 {@code fallback}。
     */
    @Nullable
    private Drawable resolveCompoundDrawable(
            @NonNull ResourceResolver resolver,
            @AttrRes int attr,
            @Nullable Drawable fallback
    ) {
        ResourceRef<?> value = attributes.get(attr);
        if (!(value instanceof DrawableRef)) {
            return fallback;
        }
        return ((DrawableRef) value).resolve(resolver);
    }

    /**
     * 已经 {@link Drawable#setBounds} 的沿用；没设过的才用 intrinsic 尺寸。
     */
    private static void keepBounds(@Nullable Drawable drawable) {
        if (drawable == null) return;
        Rect bounds = drawable.getBounds();
        if (!bounds.isEmpty()) return;
        int width = Math.max(drawable.getIntrinsicWidth(), 0);
        int height = Math.max(drawable.getIntrinsicHeight(), 0);
        drawable.setBounds(0, 0, width, height);
    }

    @Override
    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr, @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_TEXT_COLOR || attr == ATTR_TEXT_COLOR_HINT) {
            applyTextColor(attr == ATTR_TEXT_COLOR_HINT, value, resolver);
            return;
        }
        if (attr == ATTR_TEXT_SIZE) {
            if (isTrackTextSize()) applyTextSize(value, resolver);
            return;
        }
        if (attr == ATTR_DRAWABLE_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            resolver.getViewCompat().setCompoundDrawableTintList(getView(), tint);
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }

    private void applyTextSize(@NonNull ResourceRef<?> value, @NonNull ResourceResolver resolver) {
        if (!(value instanceof DimenRef)) return;
        DimenRef dimen = (DimenRef) value;
        if (!dimen.hasCustom() && dimen.getResourceId() != ID_NULL) {
            getView().setTextSize(TypedValue.COMPLEX_UNIT_PX, resolver.getDimension(dimen.getResourceId()));
            return;
        }
        Integer px = dimen.resolve(resolver);
        if (px != null) {
            getView().setTextSize(TypedValue.COMPLEX_UNIT_PX, px);
        }
    }

    private void applyTextColor(boolean hint, @NonNull ResourceRef<?> value, @NonNull ResourceResolver resolver) {
        if (value instanceof ColorRef) {
            Integer color = ((ColorRef) value).resolve(resolver);
            if (color == null) return;
            if (hint) getView().setHintTextColor(color);
            else getView().setTextColor(color);
            return;
        }
        if (value instanceof ColorStateListRef) {
            ColorStateList colors = ((ColorStateListRef) value).resolve(resolver);
            if (colors == null) return;
            if (hint) getView().setHintTextColor(colors);
            else getView().setTextColor(colors);
        }
    }
}
