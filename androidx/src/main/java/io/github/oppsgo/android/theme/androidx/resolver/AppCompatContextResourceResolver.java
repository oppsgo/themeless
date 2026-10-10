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
 * 在 {@link ContextResourceResolver} 上把 color state list / drawable 的读取换成
 * {@link AppCompatResources}，让 vector / animated-vector 等也能在低版本上取到。
 * <p>
 * 纯颜色 {@code getColor} 不走 AppCompat：color 资源只看 Resources 的 Configuration，
 * 带 Theme 反而会被 AppCompat DayNight 干扰，沿用父类实现。
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
