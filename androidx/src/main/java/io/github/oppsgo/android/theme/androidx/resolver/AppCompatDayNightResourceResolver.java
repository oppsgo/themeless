package io.github.oppsgo.android.theme.androidx.resolver;

import android.content.Context;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ThemeViewCompat;
import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver;

/**
 * AppCompat 下的日夜 Resolver：隔离 Context + {@link AppCompatContextResourceResolver} 读资源。
 */
public class AppCompatDayNightResourceResolver extends AppCompatContextResourceResolver {

    @NonNull
    public static AppCompatDayNightResourceResolver light(@NonNull Context context) {
        return of(context, false);
    }

    @NonNull
    public static AppCompatDayNightResourceResolver night(@NonNull Context context) {
        return of(context, true);
    }

    @NonNull
    public static AppCompatDayNightResourceResolver followSystem(@NonNull Context context) {
        return of(context, DayNightResourceResolver.isSystemNight(context));
    }

    @NonNull
    public static AppCompatDayNightResourceResolver of(@NonNull Context context, boolean night) {
        return new AppCompatDayNightResourceResolver(DayNightResourceResolver.isolated(context, night));
    }

    public AppCompatDayNightResourceResolver(@NonNull Context context) {
        super(context);
    }

    public AppCompatDayNightResourceResolver(@NonNull Context context, @NonNull ThemeViewCompat viewCompat) {
        super(context, viewCompat);
    }
}
