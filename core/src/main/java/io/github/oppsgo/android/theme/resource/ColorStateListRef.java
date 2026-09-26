package io.github.oppsgo.android.theme.resource;

import android.content.res.ColorStateList;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ResourceBinding;

/**
 * 颜色状态列表引用，对应 selector，以及 {@code setTextColor(ColorStateList)}。
 */
public class ColorStateListRef extends ResourceRef<ColorStateList> {

    protected ColorStateListRef(@ColorRes int resourceId, @Nullable ResourceValue<ColorStateList> custom) {
        super(resourceId, custom);
    }

    @NonNull
    public static ColorStateListRef of(@ColorRes int resourceId) {
        return new ColorStateListRef(resourceId, null);
    }

    @NonNull
    public static ColorStateListRef of(
            @ColorRes int resourceId,
            @NonNull ResourceValue<ColorStateList> custom
    ) {
        return new ColorStateListRef(resourceId, custom);
    }

    @NonNull
    public static ColorStateListRef of(@NonNull ResourceValue<ColorStateList> custom) {
        return new ColorStateListRef(ResourceBinding.ID_NULL, custom);
    }

    @Nullable
    @Override
    protected ColorStateList load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getColorStateList(resourceId);
    }
}
