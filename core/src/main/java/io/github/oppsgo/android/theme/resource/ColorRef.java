package io.github.oppsgo.android.theme.resource;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ResourceBinding;

/** 纯色引用，对应 {@code setTextColor(int)}、{@code setBackgroundColor}。 */
public class ColorRef extends ResourceRef<Integer> {

    protected ColorRef(@ColorRes int resourceId, @Nullable ResourceValue<Integer> custom) {
        super(resourceId, custom);
    }

    @NonNull
    public static ColorRef of(@ColorRes int resourceId) {
        return new ColorRef(resourceId, null);
    }

    @NonNull
    public static ColorRef of(@ColorRes int resourceId, @NonNull ResourceValue<Integer> custom) {
        return new ColorRef(resourceId, custom);
    }

    @NonNull
    public static ColorRef of(@NonNull ResourceValue<Integer> custom) {
        return new ColorRef(ResourceBinding.ID_NULL, custom);
    }

    @ColorInt
    @Override
    protected Integer load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getColor(resourceId);
    }
}
