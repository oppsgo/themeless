package io.github.oppsgo.android.theme.androidx.binding;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import io.github.oppsgo.android.theme.binding.ImageViewThemeBinding;

/**
 * {@link AppCompatImageView} 及同继承链（AppCompatImageButton / ShapeableImageView 等）：
 * 把布局里的 {@code app:srcCompat} / {@code app:tint} / {@code app:backgroundTint} 归一到平台同名属性。
 * 具体属性能力见 {@link ImageViewThemeBinding}。
 */
public class AppCompatImageViewThemeBinding extends ImageViewThemeBinding {

    /** {@code app:srcCompat} → 平台 {@code android:src}。 */
    public static final int ATTR_SRC_COMPAT = androidx.appcompat.R.attr.srcCompat;
    /** {@code app:tint} → 平台 {@code android:tint}。 */
    public static final int ATTR_TINT_COMPAT = androidx.appcompat.R.attr.tint;
    /** {@code app:backgroundTint} → 平台 {@code android:backgroundTint}。 */
    public static final int ATTR_BACKGROUND_TINT_COMPAT = androidx.appcompat.R.attr.backgroundTint;

    public AppCompatImageViewThemeBinding(@NonNull ImageView view) {
        super(view);
        alias(ATTR_SRC_COMPAT, ATTR_SRC);
        alias(ATTR_TINT_COMPAT, ATTR_TINT);
        alias(ATTR_BACKGROUND_TINT_COMPAT, ATTR_BACKGROUND_TINT);
    }
}
