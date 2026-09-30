package io.github.oppsgo.android.theme.resource;

import android.graphics.drawable.Drawable;

import androidx.annotation.AnyRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;

/**
 * Drawable 引用，对应 {@code setBackground}、{@code setImageDrawable} 等。
 */
public class DrawableRef extends ResourceRef<Drawable> {

    /** 显式空 Drawable：resolve 恒为 null，用于清除已跟踪的 compound drawable 等。 */
    private static final DrawableRef NONE = new DrawableRef(ResourceBinding.ID_NULL, resolver -> null);

    protected DrawableRef(@DrawableRes int resourceId, @Nullable ResourceValue<Drawable> custom) {
        super(resourceId, custom);
    }

    /**
     * 标记「这里就是 null」：会写入 Binding，刷新时 {@link #resolve} 返回 null。
     * 与属性未跟踪（{@code attributes} 里没有 {@link DrawableRef}）不同，后者表示沿用 View 上已有 Drawable。
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
    public static DrawableRef of(@DrawableRes int resourceId, @NonNull ResourceValue<Drawable> custom) {
        return new DrawableRef(resourceId, custom);
    }

    @NonNull
    public static DrawableRef of(@NonNull ResourceValue<Drawable> custom) {
        return new DrawableRef(ResourceBinding.ID_NULL, custom);
    }

    @Nullable
    @Override
    protected Drawable load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getDrawable(resourceId);
    }
}
