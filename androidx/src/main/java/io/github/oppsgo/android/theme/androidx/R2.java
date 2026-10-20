package io.github.oppsgo.android.theme.androidx;

/**
 * androidx 模块内第三方库资源 id 引用的统一入口，按来源库组织内部类：
 * {@link appcompat} / {@link material}。
 * <p>
 * 平台 {@code android.R.*} 属性统一收敛在 core 模块的
 * {@code io.github.oppsgo.android.theme.R2}，本类不再重复；
 * 库模块 R 字段非编译期常量，这里的字段一律不加 {@code final}，
 * 运行时始终跟随实际依赖版本的资源 id。
 *
 * @hide 仅供库内收敛引用，不纳入公开 API。
 */
public final class R2 {

    private R2() {
    }

    /** {@code androidx.appcompat} 提供的兼容属性。 */
    public static final class appcompat {

        public static final class attr {

            public static int srcCompat = androidx.appcompat.R.attr.srcCompat;
            public static int tint = androidx.appcompat.R.attr.tint;
            public static int backgroundTint = androidx.appcompat.R.attr.backgroundTint;
            public static int buttonTint = androidx.appcompat.R.attr.buttonTint;
            public static int drawableTint = androidx.appcompat.R.attr.drawableTint;
            public static int drawableLeftCompat = androidx.appcompat.R.attr.drawableLeftCompat;
            public static int drawableTopCompat = androidx.appcompat.R.attr.drawableTopCompat;
            public static int drawableRightCompat = androidx.appcompat.R.attr.drawableRightCompat;
            public static int drawableBottomCompat = androidx.appcompat.R.attr.drawableBottomCompat;
            public static int drawableStartCompat = androidx.appcompat.R.attr.drawableStartCompat;
            public static int drawableEndCompat = androidx.appcompat.R.attr.drawableEndCompat;
            public static int thumbTint = androidx.appcompat.R.attr.thumbTint;
            public static int trackTint = androidx.appcompat.R.attr.trackTint;
        }
    }

    /** {@code com.google.android.material} 提供的属性。 */
    public static final class material {

        public static final class attr {

            public static int tabTextColor = com.google.android.material.R.attr.tabTextColor;
            public static int tabSelectedTextColor = com.google.android.material.R.attr.tabSelectedTextColor;
            public static int tabIndicatorColor = com.google.android.material.R.attr.tabIndicatorColor;
            public static int tabRippleColor = com.google.android.material.R.attr.tabRippleColor;
            public static int tabIconTint = com.google.android.material.R.attr.tabIconTint;
        }
    }
}
