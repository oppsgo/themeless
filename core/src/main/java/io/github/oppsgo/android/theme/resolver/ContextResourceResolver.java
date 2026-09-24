package io.github.oppsgo.android.theme.resolver;


import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeViewCompat;

/**
 * 默认实现：直接读构造时传入的 Context。
 * 日夜切换请用 {@link DayNightResourceResolver}。
 */
@SuppressWarnings("deprecation")
public class ContextResourceResolver implements ResourceResolver {

    private final Context context;
    private final ThemeViewCompat viewCompat;

    public ContextResourceResolver(@NonNull Context context) {
        this(context, new ThemeViewCompatImpl());
    }

    public ContextResourceResolver(@NonNull Context context, @NonNull ThemeViewCompat viewCompat) {
        this.context = context;
        this.viewCompat = viewCompat;
    }

    @NonNull
    protected Context getContext() {
        return context;
    }

    Resources.Theme getTheme() {
        return context.getTheme();
    }

    @Override
    public int getColor(@ColorRes int id) {
        Resources res = getResources();
        // 纯 color 资源只看 Resources 的 Configuration；不要带 Theme，
        // 避免 Theme.AppCompat.DayNight 在系统暗色时干扰。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(id, null);
        }
        return res.getColor(id);
    }

    @Nullable
    @Override
    public ColorStateList getColorStateList(@ColorRes int id) {
        Resources res = getResources();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColorStateList(id, null);
        }
        return res.getColorStateList(id);
    }

    @Nullable
    @Override
    public Drawable getDrawable(@AnyRes int id) {
        Resources resources = getResources();
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            drawable = resources.getDrawable(id, getTheme());
        } else {
            drawable = resources.getDrawable(id);
        }
        return drawable;
    }

    @Override
    public float getDimension(@DimenRes int id) {
        return getResources().getDimension(id);
    }

    @Override
    public int getDimensionPixelSize(@DimenRes int id) {
        return getResources().getDimensionPixelSize(id);
    }

    @Override
    public Resources getResources() {
        return context.getResources();
    }

    @NonNull
    @Override
    public ThemeViewCompat getViewCompat() {
        return viewCompat;
    }
}
