package io.github.oppsgo.android.theme.androidx.resolver;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;

import io.github.oppsgo.android.theme.ThemeViewCompat;
import io.github.oppsgo.android.theme.resolver.ContextResourceResolver;

/**
 * AppCompat 下的 {@link ContextResourceResolver}。
 * {@code ColorStateList} / {@code Drawable} 走 {@link AppCompatResources}，才能解析 vector 和 AppCompat 的 selector。
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
