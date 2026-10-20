package io.github.oppsgo.android.theme;

import android.annotation.SuppressLint;

import io.github.oppsgo.android.theme.core.R;

/**
 * core 模块内资源 id 引用的统一入口：平台 {@code android.R.*} 常量与 core 自身资源。
 * <p>
 * 库模块生成的 {@link R} 字段不是编译期常量，故 core 自身资源字段不加 {@code final}；
 * {@code android.R.attr.*} 为平台稳定常量，保持 {@code final} 以保留编译期内联。
 * androidx 模块需要平台属性时跨模块复用本类，不再各自直接引用 {@code android.R}。
 *
 * @hide 仅供库内收敛引用，不纳入公开 API。
 */
public final class R2 {

    private R2() {
    }

    public static final class attr {

        // ---- 平台 android.R.attr ----

        public static final int background = android.R.attr.background;
        @SuppressLint("InlinedApi")
        public static final int backgroundTint = android.R.attr.backgroundTint;

        public static final int textColor = android.R.attr.textColor;
        public static final int textColorHint = android.R.attr.textColorHint;
        @SuppressLint("InlinedApi")
        public static final int drawableTint = android.R.attr.drawableTint;
        public static final int drawableLeft = android.R.attr.drawableLeft;
        public static final int drawableTop = android.R.attr.drawableTop;
        public static final int drawableRight = android.R.attr.drawableRight;
        public static final int drawableBottom = android.R.attr.drawableBottom;
        @SuppressLint("InlinedApi")
        public static final int drawableStart = android.R.attr.drawableStart;
        @SuppressLint("InlinedApi")
        public static final int drawableEnd = android.R.attr.drawableEnd;

        public static final int src = android.R.attr.src;
        public static final int tint = android.R.attr.tint;

        public static final int button = android.R.attr.button;
        @SuppressLint("InlinedApi")
        public static final int buttonTint = android.R.attr.buttonTint;

        public static final int thumb = android.R.attr.thumb;
        public static final int track = android.R.attr.track;
        @SuppressLint("InlinedApi")
        public static final int thumbTint = android.R.attr.thumbTint;
        @SuppressLint("InlinedApi")
        public static final int trackTint = android.R.attr.trackTint;

        public static final int progressDrawable = android.R.attr.progressDrawable;
        public static final int indeterminateDrawable = android.R.attr.indeterminateDrawable;
        @SuppressLint("InlinedApi")
        public static final int progressTint = android.R.attr.progressTint;
        @SuppressLint("InlinedApi")
        public static final int indeterminateTint = android.R.attr.indeterminateTint;
        @SuppressLint("InlinedApi")
        public static final int progressBackgroundTint = android.R.attr.progressBackgroundTint;
        @SuppressLint("InlinedApi")
        public static final int secondaryProgressTint = android.R.attr.secondaryProgressTint;

        // ---- core 自身资源（库 R 字段非常量，不可 final）----

        public static int themeViewAttrEnable = R.attr.themeViewAttrEnable;
    }

    public static final class id {

        public static int theme_view_attr_binding_tag = R.id.theme_view_attr_binding_tag;
    }
}
