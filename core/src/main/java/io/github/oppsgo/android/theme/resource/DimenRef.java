package io.github.oppsgo.android.theme.resource;

import androidx.annotation.AnyRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ResourceBinding;

/**
 * dimen 引用，默认取出 {@link ResourceResolver#getDimensionPixelSize}。
 * 字号要用带小数、且跟随 {@code fontScale} 的值时，用 {@link Resolver} 调 {@link ResourceResolver#getDimension}。
 */
public final class DimenRef extends ResourceRef<Integer> {

    private DimenRef(@DimenRes int resourceId, @Nullable Resolver<Integer> resolver) {
        super(resourceId, resolver);
    }

    @NonNull
    public static DimenRef of(@DimenRes int resourceId) {
        return new DimenRef(resourceId, null);
    }

    @NonNull
    public static DimenRef of(@DimenRes int resourceId, @NonNull Resolver<Integer> resolver) {
        return new DimenRef(resourceId, resolver);
    }

    @NonNull
    public static DimenRef of(@NonNull Resolver<Integer> resolver) {
        return new DimenRef(ResourceBinding.ID_NULL, resolver);
    }

    @Px
    @Override
    protected Integer load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getDimensionPixelSize(resourceId);
    }
}
