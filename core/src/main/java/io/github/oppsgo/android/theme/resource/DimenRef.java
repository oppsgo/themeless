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
 * 字号要用带小数、且跟随 {@code fontScale} 的值时，用 {@link ResourceValue} 调 {@link ResourceResolver#getDimension}。
 */
public class DimenRef extends ResourceRef<Integer> {

    protected DimenRef(@DimenRes int resourceId, @Nullable ResourceValue<Integer> custom) {
        super(resourceId, custom);
    }

    @NonNull
    public static DimenRef of(@DimenRes int resourceId) {
        return new DimenRef(resourceId, null);
    }

    @NonNull
    public static DimenRef of(@DimenRes int resourceId, @NonNull ResourceValue<Integer> custom) {
        return new DimenRef(resourceId, custom);
    }

    @NonNull
    public static DimenRef of(@NonNull ResourceValue<Integer> custom) {
        return new DimenRef(ResourceBinding.ID_NULL, custom);
    }

    @Px
    @Override
    protected Integer load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getDimensionPixelSize(resourceId);
    }
}
