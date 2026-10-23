package io.github.oppsgo.android.theme.webview;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.view.ContextThemeWrapper;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

import java.lang.reflect.Method;

import io.github.oppsgo.android.theme.R2;

/**
 * WebView 日夜：跟<strong>页面宿主自己的暗黑状态</strong>（例如当前皮肤），
 * 而不是系统 / Application 的 uiMode。
 * <p>
 * 前提：网页已用 {@code prefers-color-scheme} 适配（建议同时声明
 * {@code <meta name="color-scheme" content="light dark">}）。
 * <p>
 * <b>两套职责，不要混：</b>
 * <ol>
 *   <li><b>明暗切换</b>（已适配页走 {@code prefers-color-scheme}）
 *     <ul>
 *       <li>{@link #isThemeDriven} 为 false（targetSdk ≤ 32）：用
 *           {@link WebSettings#setForceDark}（API 29+ 平台；更低 androidx.webkit 反射）
 *           写 {@code FORCE_DARK_ON}/{@code OFF}。这是 WebSettings 上的切换 API。</li>
 *       <li>{@link #isThemeDriven} 为 true（API 33+ 且 targetSdk ≥ 33）：
 *           {@code setForceDark} 为 no-op。明暗由<strong>当前主题</strong>
 *           {@code android:isLightTheme} 决定（DayNight + {@code localNightMode} /
 *           uiMode）。{@link #apply} <strong>不会</strong>、也<strong>不能</strong>
 *           用 WebSettings 切已适配页；宿主先改主题，再 {@link #apply} 只配算法加深。
 *           皮肤暗色与 Activity uiMode 不一致时，用 {@link #createWebViewContext} 造
 *           Context 并重建 WebView。</li>
 *     </ul>
 *   </li>
 *   <li><b>未适配页算法加深</b>（与明暗开关无关）
 *     <ul>
 *       <li>主题驱动：{@link WebSettings#setAlgorithmicDarkeningAllowed} —
 *           仅当主题已是暗色、且页未声明 dark style 时，是否允许算法改色。</li>
 *       <li>force dark 路径：{@code setForceDarkStrategy}（反射）—
 *           优先网页主题 vs 允许 UA 算法加深。</li>
 *     </ul>
 *     {@code setAlgorithmicDarkeningAllowed(true/false)} <strong>不是</strong>
 *     亮/暗切换开关。
 *   </li>
 * </ol>
 * {@link #applyAndReload}：force dark 已改但内核不重绘时的保底；主题驱动不 reload。
 * <p>
 * 不放进 binding：不落资源 id，重建也不合 Binding 同步重绘模型。
 * Themeless 下 Activity 可能为避 Force Dark 保持夜间而皮肤仍浅，勿用
 * {@link #isActivityNight} 代替皮肤状态。
 * <p>
 * 仅主线程。典型：创建后、{@code loadUrl} 前 {@link #apply}；宿主暗黑变时再
 * {@link #apply}（主题驱动须宿主已改好主题）；需要催重绘时用 {@link #applyAndReload}。
 */
@MainThread
public final class WebViewDayNightCompat {

    private static final int TAG_STATE = R2.id.webview_dark_mode_compat_tag;

    /**
     * 网页<strong>未</strong>适配暗黑时的算法加深策略（不是明暗开关）。
     */
    public enum Strategy {
        /**
         * 优先网页自己的暗黑；未适配时允许算法加深（默认）。
         */
        PREFER_WEB_THEME,
        /**
         * 只用网页暗黑；未适配页绝不算法改色。
         */
        WEB_THEME_ONLY
    }

    private WebViewDayNightCompat() {
    }

    /**
     * 是否由主题 {@code isLightTheme} 决定已适配页的明暗（API 33+ 且 targetSdk ≥ 33）。
     * 为 true 时 {@code setForceDark} 为 no-op；{@link #apply} 只配算法加深，
     * 明暗靠宿主主题 / {@link #createWebViewContext}。
     */
    public static boolean isThemeDriven(@NonNull Context context) {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.TIRAMISU;
    }

    /**
     * Context 的 uiMode 是否夜间。请传 Activity，勿传 Application。
     * 皮肤与 Activity uiMode 可能不一致时，请把皮肤暗色直接传给 {@link #apply}。
     */
    public static boolean isActivityNight(@NonNull Context context) {
        int nightBits = context.getResources().getConfiguration().uiMode
                & Configuration.UI_MODE_NIGHT_MASK;
        return nightBits == Configuration.UI_MODE_NIGHT_YES;
    }

    /**
     * 造创建 WebView 用的 Context：以 Activity 的 Configuration 为底，强制改写 uiMode。
     * 主题驱动下 {@code prefers-color-scheme} 由此解析的 {@code isLightTheme} 决定，
     * 故宿主主题须为 DayNight 系；否则用
     * {@link #createWebViewContext(Context, boolean, int, int)} 显式给两套主题。
     * <p>
     * 与 DayNightResourceResolver 不同：这里必须挂 Activity 以带上主题，不能用 Application。
     * 返回值持有 {@code base}，WebView 销毁前勿额外长期持有。
     */
    @NonNull
    public static Context createWebViewContext(@NonNull Context base, boolean dark) {
        Configuration config = new Configuration(base.getResources().getConfiguration());
        int nightFlag = dark ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO;
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK) | nightFlag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return base.createConfigurationContext(config);
        }
        // API 16 造不了隔离 Context；主题驱动本就不适用。
        return base;
    }

    /**
     * 同上，再包显式主题（Activity 主题不是 DayNight 时用）。
     */
    @NonNull
    public static Context createWebViewContext(
            @NonNull Context base,
            boolean dark,
            @StyleRes int darkTheme,
            @StyleRes int lightTheme) {
        return new ContextThemeWrapper(
                createWebViewContext(base, dark), dark ? darkTheme : lightTheme);
    }

    /**
     * 按 WebView Context 的 uiMode 应用；皮肤与 uiMode 不一致时用 {@link #apply(WebView, boolean)}。
     */
    public static void apply(@NonNull WebView webView) {
        apply(webView, isActivityNight(webView.getContext()), Strategy.PREFER_WEB_THEME);
    }

    /**
     * 写入 WebSettings。
     * <ul>
     *   <li>非主题驱动：用 {@code setForceDark} <strong>切换</strong>明暗，再用 strategy
     *       配未适配页算法加深。</li>
     *   <li>主题驱动：假定宿主主题 {@code isLightTheme} 已是目标明暗；此处只写
     *       {@code setAlgorithmicDarkeningAllowed}（未适配页），<strong>不</strong>把
     *       {@code dark} 当成 WebSettings 开关。</li>
     * </ul>
     *
     * @param dark 页面宿主当前是否暗色（非系统 / Application）；主题驱动下供记录状态，
     *             真正明暗仍看宿主主题
     */
    public static void apply(@NonNull WebView webView, boolean dark) {
        apply(webView, dark, Strategy.PREFER_WEB_THEME);
    }

    /**
     * 同 {@link #apply(WebView, boolean)}，并指定未适配页 {@link Strategy}。
     */
    public static void apply(@NonNull WebView webView, boolean dark, @NonNull Strategy strategy) {
        WebSettings settings = webView.getSettings();
        if (isThemeDriven(webView.getContext())) {
            // 已适配页：明暗由主题 isLightTheme，WebSettings 无切换 API。
            // 这里只配「未适配页要不要算法加深」。
            allowAlgorithmicDarkening(settings, dark);
        } else {
            // 已适配页 / 未适配页：setForceDark 才是切换 API。
            applyForceDark(settings, dark, strategy);
        }
        remember(webView, dark, strategy);
    }

    /**
     * 应用设置；非主题驱动且状态有变、已有文档时再 reload（催 force dark 重绘）。
     * 主题驱动不 reload（明暗跟主题，不靠 WebSettings）。
     *
     * @return 本次是否触发了 {@link WebView#reload()}
     */
    public static boolean applyAndReload(@NonNull WebView webView, boolean dark) {
        return applyAndReload(webView, dark, Strategy.PREFER_WEB_THEME);
    }

    /**
     * 同 {@link #applyAndReload(WebView, boolean)}，并指定 {@link Strategy}。
     */
    public static boolean applyAndReload(
            @NonNull WebView webView, boolean dark, @NonNull Strategy strategy) {
        Object last = webView.getTag(TAG_STATE);
        int key = stateKey(dark, strategy);
        boolean changed = last instanceof Integer && (Integer) last != key;

        apply(webView, dark, strategy);

        if (isThemeDriven(webView.getContext()) || !changed) {
            return false;
        }
        String url = webView.getUrl();
        if (url == null || url.isEmpty() || "about:blank".equals(url)) {
            return false;
        }
        webView.reload();
        return true;
    }

    /**
     * targetSdk ≤ 32：用 force dark 切换明暗，并按 strategy 配未适配页。
     */
    private static void applyForceDark(
            @NonNull WebSettings settings, boolean dark, @NonNull Strategy strategy) {
        boolean applied = applyPlatformForceDark(settings, dark);
        WebKit webKit = WebKit.get();
        if (webKit == null) {
            return;
        }
        if (!applied) {
            webKit.setForceDark(settings, dark, strategy);
        } else {
            webKit.applyStrategy(settings, strategy);
        }
    }

    /**
     * @return 是否已用平台 API 写下 force dark（API 29+）
     */
    @SuppressWarnings("deprecation")
    private static boolean applyPlatformForceDark(@NonNull WebSettings settings, boolean dark) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            return false;
        }
        settings.setForceDark(dark ? WebSettings.FORCE_DARK_ON : WebSettings.FORCE_DARK_OFF);
        return true;
    }

    private static void allowAlgorithmicDarkening(@NonNull WebSettings settings, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            settings.setAlgorithmicDarkeningAllowed(dark);
        }
    }

    private static void remember(
            @NonNull WebView webView, boolean dark, @NonNull Strategy strategy) {
        webView.setTag(TAG_STATE, stateKey(dark, strategy));
    }

    private static int stateKey(boolean dark, @NonNull Strategy strategy) {
        return (strategy.ordinal() << 1) | (dark ? 1 : 0);
    }

    /**
     * androidx.webkit 反射薄壳：零依赖。
     * 平台 setForceDark 不可用时（API 28−）写 ON/OFF；API 29+ 只补 strategy。
     */
    private static final class WebKit {

        @Nullable
        private static final WebKit INSTANCE = probe();

        @Nullable
        private final Method mIsFeatureSupported;
        @Nullable
        private final Object mFeatureForceDark;
        @Nullable
        private final Object mFeatureForceDarkStrategy;
        @Nullable
        private final Method mSetForceDark;
        @Nullable
        private final Method mSetForceDarkStrategy;
        @Nullable
        private final Integer mForceDarkOn;
        @Nullable
        private final Integer mForceDarkOff;
        @Nullable
        private final Integer mStrategyWebThemeOnly;
        @Nullable
        private final Integer mStrategyPreferWebTheme;

        @Nullable
        static WebKit get() {
            return INSTANCE;
        }

        private WebKit(
                @Nullable Method isFeatureSupported,
                @Nullable Object featureForceDark,
                @Nullable Object featureForceDarkStrategy,
                @Nullable Method setForceDark,
                @Nullable Method setForceDarkStrategy,
                @Nullable Integer forceDarkOn,
                @Nullable Integer forceDarkOff,
                @Nullable Integer strategyWebThemeOnly,
                @Nullable Integer strategyPreferWebTheme) {
            mIsFeatureSupported = isFeatureSupported;
            mFeatureForceDark = featureForceDark;
            mFeatureForceDarkStrategy = featureForceDarkStrategy;
            mSetForceDark = setForceDark;
            mSetForceDarkStrategy = setForceDarkStrategy;
            mForceDarkOn = forceDarkOn;
            mForceDarkOff = forceDarkOff;
            mStrategyWebThemeOnly = strategyWebThemeOnly;
            mStrategyPreferWebTheme = strategyPreferWebTheme;
        }

        @Nullable
        private static WebKit probe() {
            try {
                Class<?> feature = Class.forName("androidx.webkit.WebViewFeature");
                Class<?> compat = Class.forName("androidx.webkit.WebSettingsCompat");
                Object featureForceDark = staticField(feature, "FORCE_DARK");
                if (featureForceDark == null) {
                    return null;
                }
                Method setForceDarkStrategy = null;
                try {
                    setForceDarkStrategy = compat.getDeclaredMethod(
                            "setForceDarkStrategy", WebSettings.class, int.class);
                } catch (NoSuchMethodError | NoSuchMethodException ignored) {
                    // 旧 webkit 无策略 API。
                }
                return new WebKit(
                        feature.getDeclaredMethod("isFeatureSupported", String.class),
                        featureForceDark,
                        staticField(feature, "FORCE_DARK_STRATEGY"),
                        compat.getDeclaredMethod("setForceDark", WebSettings.class, int.class),
                        setForceDarkStrategy,
                        (Integer) staticField(compat, "FORCE_DARK_ON"),
                        (Integer) staticField(compat, "FORCE_DARK_OFF"),
                        (Integer) staticField(compat, "DARK_STRATEGY_WEB_THEME_DARKENING_ONLY"),
                        (Integer) staticField(
                                compat, "DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING"));
            } catch (Throwable ignored) {
                return null;
            }
        }

        boolean isForceDarkSupported() {
            try {
                if (mIsFeatureSupported == null
                        || mFeatureForceDark == null
                        || mSetForceDark == null
                        || mForceDarkOn == null
                        || mForceDarkOff == null) {
                    return false;
                }
                return mIsFeatureSupported.invoke(null, mFeatureForceDark) == Boolean.TRUE;
            } catch (Throwable ignored) {
                return false;
            }
        }

        void setForceDark(@NonNull WebSettings settings, boolean dark, @NonNull Strategy strategy) {
            try {
                if (!isForceDarkSupported()) {
                    return;
                }
                assert mSetForceDark != null;
                mSetForceDark.invoke(null, settings, dark ? mForceDarkOn : mForceDarkOff);
                applyStrategy(settings, strategy);
            } catch (Throwable ignored) {
                // 反射失败则保持现状。
            }
        }

        void applyStrategy(@NonNull WebSettings settings, @NonNull Strategy strategy) {
            if (mSetForceDarkStrategy == null
                    || mFeatureForceDarkStrategy == null
                    || mIsFeatureSupported == null) {
                return;
            }
            Integer value = strategy == Strategy.WEB_THEME_ONLY
                    ? mStrategyWebThemeOnly
                    : mStrategyPreferWebTheme;
            if (value == null) {
                return;
            }
            try {
                if (mIsFeatureSupported.invoke(null, mFeatureForceDarkStrategy) == Boolean.TRUE) {
                    mSetForceDarkStrategy.invoke(null, settings, value);
                }
            } catch (Throwable ignored) {
                // 策略失败不影响 force dark。
            }
        }

        @Nullable
        private static Object staticField(@NonNull Class<?> type, @NonNull String name) {
            try {
                return type.getDeclaredField(name).get(null);
            } catch (Throwable ignored) {
                return null;
            }
        }
    }
}
