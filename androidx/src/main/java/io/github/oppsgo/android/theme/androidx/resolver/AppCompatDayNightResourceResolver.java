package io.github.oppsgo.android.theme.androidx.resolver;

import android.content.Context;
import android.view.ContextThemeWrapper;

import androidx.annotation.NonNull;
import androidx.appcompat.R;

import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver;

/**
 * AppCompat 下的日夜 Resolver。
 * <p>
 * Context 先按 uiMode 隔离，再套非 DayNight 的 Light / Dark Theme，避免
 * {@code Theme.AppCompat.DayNight} 在系统暗色时把属性又解析回夜间。
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
        Context isolated = DayNightResourceResolver.wrapContext(
                context,
                DayNightResourceResolver.nightConfig(context, night)
        );
        int theme = night
                ? R.style.Theme_AppCompat
                : R.style.Theme_AppCompat_Light;
        return new AppCompatDayNightResourceResolver(new ContextThemeWrapper(isolated, theme));
    }

    protected AppCompatDayNightResourceResolver(@NonNull Context context) {
        super(context);
    }
}
