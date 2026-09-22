package io.github.oppsgo.android.theme.resource;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ResourceBinding;

/** 纯色引用，对应 {@code setTextColor(int)}、{@code setBackgroundColor}。 */
public final class ColorRef extends ResourceRef<Integer> {

    private ColorRef(@ColorRes int resourceId, @Nullable Resolver<Integer> resolver) {
        super(resourceId, resolver);
    }

    @NonNull
    public static ColorRef of(@ColorRes int resourceId) {
        return new ColorRef(resourceId, null);
    }

    @NonNull
    public static ColorRef of(@ColorRes int resourceId, @NonNull Resolver<Integer> resolver) {
        return new ColorRef(resourceId, resolver);
    }

    @NonNull
    public static ColorRef of(@NonNull Resolver<Integer> resolver) {
        return new ColorRef(ResourceBinding.ID_NULL, resolver);
    }

    @ColorInt
    @Override
    protected Integer load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getColor(resourceId);
    }
}
