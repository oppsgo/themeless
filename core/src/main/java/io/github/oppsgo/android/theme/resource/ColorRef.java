package io.github.oppsgo.android.theme.resource;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;

/** 纯色引用，对应 {@code setTextColor(int)}、{@code setBackgroundColor}。 */
public class ColorRef extends ResourceRef<Integer> {

    protected ColorRef(@ColorRes int resourceId, @Nullable ResourceProvider<Integer> provider) {
        super(resourceId, provider);
    }

    @NonNull
    public static ColorRef of(@ColorRes int resourceId) {
        return new ColorRef(resourceId, null);
    }

    @NonNull
    public static ColorRef of(@ColorRes int resourceId, @NonNull ResourceProvider<Integer> provider) {
        return new ColorRef(resourceId, provider);
    }

    @NonNull
    public static ColorRef fromProvider(@NonNull ResourceProvider<Integer> provider) {
        return new ColorRef(ID_NULL, provider);
    }

    @ColorInt
    @Override
    protected Integer load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getColor(resourceId);
    }
}
