package io.github.oppsgo.android.theme.resource;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;

/**
 * Drawable 引用，对应 {@code setBackground}、{@code setImageDrawable} 等。
 */
public class DrawableRef extends ResourceRef<Drawable> {

    /** 显式空 Drawable：provide 恒为 null，用于清除已跟踪的 compound drawable 等。 */
    private static final DrawableRef NONE = new DrawableRef(ID_NULL, resolver -> null);

    protected DrawableRef(@DrawableRes int resourceId, @Nullable ResourceProvider<Drawable> provider) {
        super(resourceId, provider);
    }

    /**
     * 标记「这里就是 null」：会写入 Binding，刷新时 {@link #provide} 返回 null。
     * 用于 compound drawable 等需要显式占位空方向的场景（与传入 {@code null} 等价）。
     */
    @NonNull
    public static DrawableRef none() {
        return NONE;
    }

    @NonNull
    public static DrawableRef of(@DrawableRes int resourceId) {
        return new DrawableRef(resourceId, null);
    }

    @NonNull
    public static DrawableRef of(@DrawableRes int resourceId, @NonNull ResourceProvider<Drawable> provider) {
        return new DrawableRef(resourceId, provider);
    }

    @NonNull
    public static DrawableRef of(@NonNull ResourceProvider<Drawable> provider) {
        return new DrawableRef(ID_NULL, provider);
    }

    /**
     * 一行式着色：把 {@code drawableId} 染成 {@code colorId} 的颜色，混合模式默认 {@link PorterDuff.Mode#SRC_IN}。
     */
    @NonNull
    public static DrawableRef tinted(@DrawableRes int drawableId, @ColorRes int colorId) {
        return tinted(drawableId, colorId, PorterDuff.Mode.SRC_IN);
    }

    /**
     * 带混合模式的着色。API 21+ 用 {@link Drawable#setTintList} / {@link Drawable#setTintMode}；
     * 16–20 回退 {@code setColorFilter(color, mode)}。
     */
    @NonNull
    public static DrawableRef tinted(@DrawableRes int drawableId, @ColorRes int colorId, @NonNull PorterDuff.Mode mode) {
        return new DrawableRef(drawableId, resolver -> {
            Drawable drawable = resolver.getDrawable(drawableId);
            if (drawable == null) {
                return null;
            }
            int color = resolver.getColor(colorId);
            // 资源 Drawable 共享 constant state，先 mutate 再染，避免污染其它使用者。
            Drawable target = drawable.mutate();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                target.setTintList(ColorStateList.valueOf(color));
                target.setTintMode(mode);
            } else {
                setColorFilterCompat(target, color, mode);
            }
            return target;
        });
    }

    @SuppressWarnings("deprecation")
    private static void setColorFilterCompat(@NonNull Drawable drawable, int color, @NonNull PorterDuff.Mode mode) {
        drawable.setColorFilter(color, mode);
    }

    @Nullable
    @Override
    protected Drawable load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getDrawable(resourceId);
    }
}
