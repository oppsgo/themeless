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

    /**
     * 用状态表 + 各态颜色拼 {@link ColorStateList}。
     * {@code states.length} 须等于 {@code colors.length}；换肤时再分别 resolve。
     * 传入 {@link ColorRef}，或自定义 {@link ResourceValue}（例如对 CSL 取 default）。
     */
    @NonNull
    @SafeVarargs
    public static ColorStateListRef of(
            @NonNull int[][] states,
            @NonNull ResourceValue<Integer>... colors
    ) {
        if (states.length != colors.length) {
            throw new IllegalArgumentException(
                    "states.length (" + states.length + ") != colors.length (" + colors.length + ")");
        }
        return of(resolver -> {
            int[] resolved = new int[colors.length];
            for (int i = 0; i < colors.length; i++) {
                Integer c = colors[i].resolve(resolver);
                if (c == null) return null;
                resolved[i] = c;
            }
            return new ColorStateList(states, resolved);
        });
    }

    @Nullable
    @Override
    protected ColorStateList load(@NonNull ResourceResolver resolver, @AnyRes int resourceId) {
        return resolver.getColorStateList(resourceId);
    }
}
