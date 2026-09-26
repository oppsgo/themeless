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

    protected DrawableRef(@DrawableRes int resourceId, @Nullable ResourceValue<Drawable> custom) {
        super(resourceId, custom);
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
