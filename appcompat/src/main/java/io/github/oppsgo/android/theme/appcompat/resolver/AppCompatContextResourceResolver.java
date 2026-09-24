package io.github.oppsgo.android.theme.appcompat.resolver;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v7.content.res.AppCompatResources;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ThemeViewCompat;
import io.github.oppsgo.android.theme.resolver.ContextResourceResolver;

/**
 * Support Library AppCompat 下的 {@link ContextResourceResolver}。
 */
public class AppCompatContextResourceResolver extends ContextResourceResolver {

    public AppCompatContextResourceResolver(@NonNull Context context) {
        this(context, new ThemeViewCompatImpl());
    }

    public AppCompatContextResourceResolver(@NonNull Context context, @NonNull ThemeViewCompat viewCompat) {
        super(context, viewCompat);
    }

    @Nullable
    @Override
    public ColorStateList getColorStateList(@ColorRes int id) {
        return AppCompatResources.getColorStateList(getContext(), id);
    }

    @Nullable
    @Override
    public Drawable getDrawable(@AnyRes int id) {
        return AppCompatResources.getDrawable(getContext(), id);
    }
}
