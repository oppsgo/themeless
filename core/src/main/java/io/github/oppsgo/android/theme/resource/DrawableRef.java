package io.github.oppsgo.android.theme.resource;

import android.graphics.drawable.Drawable;

import androidx.annotation.AnyRes;
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
    public static DrawableRef fromProvider(@NonNull ResourceProvider<Drawable> provider) {
        return new DrawableRef(ID_NULL, provider);
    }

    @Nullable
    @Override
    protected Drawable load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getDrawable(resourceId);
    }
}
