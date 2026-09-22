package io.github.oppsgo.android.theme.resource;

import android.graphics.drawable.Drawable;

import androidx.annotation.AnyRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ResourceBinding;

/** Drawable 引用，对应 {@code setBackground}、{@code setImageDrawable} 等。 */
public final class DrawableRef extends ResourceRef<Drawable> {

    private DrawableRef(@DrawableRes int resourceId, @Nullable Resolver<Drawable> resolver) {
        super(resourceId, resolver);
    }

    @NonNull
    public static DrawableRef of(@DrawableRes int resourceId) {
        return new DrawableRef(resourceId, null);
    }

    @NonNull
    public static DrawableRef of(@DrawableRes int resourceId, @NonNull Resolver<Drawable> resolver) {
        return new DrawableRef(resourceId, resolver);
    }

    @NonNull
    public static DrawableRef of(@NonNull Resolver<Drawable> resolver) {
        return new DrawableRef(ResourceBinding.ID_NULL, resolver);
    }

    @Nullable
    @Override
    protected Drawable load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getDrawable(resourceId);
    }
}
