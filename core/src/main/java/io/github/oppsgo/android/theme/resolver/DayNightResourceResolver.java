package io.github.oppsgo.android.theme.resolver;

import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

/**
 * 在 {@link ContextResourceResolver} 上只多做一件事：按亮 / 暗 / 跟随系统造一套 Configuration。
 * <p>
 * 取值 Context 必须与 Activity / AppCompat DayNight 隔离：只能从
 * {@link Context#getApplicationContext()} 调 {@link Context#createConfigurationContext}。
 * 对 Activity 调会被 AppCompat 再盖回「跟随系统夜间」；用共用 {@code AssetManager} 的
 * {@code new Resources(...)} 在新系统上也不会按 uiMode 选 values / values-night。
 */
public class DayNightResourceResolver extends ContextResourceResolver {

    @NonNull
    public static DayNightResourceResolver light(@NonNull Context context) {
        return of(context, false);
    }

    @NonNull
    public static DayNightResourceResolver night(@NonNull Context context) {
        return of(context, true);
    }

    /**
     * 跟随系统当前的 {@code uiMode}（读 Application，不读可能被污染的 Activity）。
     */
    @NonNull
    public static DayNightResourceResolver followSystem(@NonNull Context context) {
        return of(context, isSystemNight(context));
    }

    @NonNull
    public static DayNightResourceResolver of(@NonNull Context context, boolean night) {
        return new DayNightResourceResolver(wrapContext(context, nightConfig(context, night)));
    }

    /**
     * 是否系统（Application）当前为夜间。
     * 不读 Activity：AppCompat {@code DefaultNightMode}/{@code localNightMode} 会改掉 Activity 的 uiMode。
     */
    public static boolean isSystemNight(@NonNull Context context) {
        Context app = context.getApplicationContext();
        int nightBits = app.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightBits == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * 以 Application 的 Configuration 为底，只改 {@code uiMode} 日夜位。
     */
    @NonNull
    public static Configuration nightConfig(@NonNull Context context, boolean night) {
        Context app = context.getApplicationContext();
        Configuration config = new Configuration(app.getResources().getConfiguration());
        int nightFlag = night ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightFlag;
        return config;
    }

    /**
     * 造一个只按 {@code config.uiMode} 解析 values / values-night 的 Context。
     * 从 Application 创建，不修改 Activity，也不对 Activity 调 {@code createConfigurationContext}。
     */
    @NonNull
    public static Context wrapContext(@NonNull Context context, @NonNull Configuration config) {
        return context.getApplicationContext().createConfigurationContext(config);
    }

    protected DayNightResourceResolver(@NonNull Context context) {
        super(context);
    }
}
