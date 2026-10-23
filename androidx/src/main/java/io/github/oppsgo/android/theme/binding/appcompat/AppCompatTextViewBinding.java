package io.github.oppsgo.android.theme.binding.appcompat;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import io.github.oppsgo.android.theme.androidx.R2;
import io.github.oppsgo.android.theme.binding.TextViewBinding;

/**
 * {@link AppCompatTextView} 及同继承链（AppCompatButton / AppCompatEditText / MaterialButton 等）：
 * 把布局里的 {@code app:backgroundTint} / {@code app:drawableTint} 与
 * {@code app:drawableLeftCompat} 等六个 compound drawable 兼容属性归一到平台同名属性。
 * 具体属性能力见 {@link TextViewBinding}；drawable 值经 Resolver 的
 * {@code AppCompatResources} 加载，低版本 vector 同样有效。
 */
public class AppCompatTextViewBinding extends TextViewBinding {

    /** {@code app:backgroundTint} → 平台 {@code android:backgroundTint}。 */
    public static final int ATTR_BACKGROUND_TINT_COMPAT = R2.appcompat.attr.backgroundTint;
    /** {@code app:drawableTint} → 平台 {@code android:drawableTint}。 */
    public static final int ATTR_DRAWABLE_TINT_COMPAT = R2.appcompat.attr.drawableTint;
    /** {@code app:drawableLeftCompat} → 平台 {@code android:drawableLeft}。 */
    public static final int ATTR_DRAWABLE_LEFT_COMPAT = R2.appcompat.attr.drawableLeftCompat;
    /** {@code app:drawableTopCompat} → 平台 {@code android:drawableTop}。 */
    public static final int ATTR_DRAWABLE_TOP_COMPAT = R2.appcompat.attr.drawableTopCompat;
    /** {@code app:drawableRightCompat} → 平台 {@code android:drawableRight}。 */
    public static final int ATTR_DRAWABLE_RIGHT_COMPAT = R2.appcompat.attr.drawableRightCompat;
    /** {@code app:drawableBottomCompat} → 平台 {@code android:drawableBottom}。 */
    public static final int ATTR_DRAWABLE_BOTTOM_COMPAT = R2.appcompat.attr.drawableBottomCompat;
    /** {@code app:drawableStartCompat} → 平台 {@code android:drawableStart}。 */
    public static final int ATTR_DRAWABLE_START_COMPAT = R2.appcompat.attr.drawableStartCompat;
    /** {@code app:drawableEndCompat} → 平台 {@code android:drawableEnd}。 */
    public static final int ATTR_DRAWABLE_END_COMPAT = R2.appcompat.attr.drawableEndCompat;

    public AppCompatTextViewBinding(@NonNull TextView view) {
        super(view);
        alias(ATTR_BACKGROUND_TINT_COMPAT, ATTR_BACKGROUND_TINT);
        alias(ATTR_DRAWABLE_TINT_COMPAT, ATTR_DRAWABLE_TINT);
        alias(ATTR_DRAWABLE_LEFT_COMPAT, ATTR_DRAWABLE_LEFT);
        alias(ATTR_DRAWABLE_TOP_COMPAT, ATTR_DRAWABLE_TOP);
        alias(ATTR_DRAWABLE_RIGHT_COMPAT, ATTR_DRAWABLE_RIGHT);
        alias(ATTR_DRAWABLE_BOTTOM_COMPAT, ATTR_DRAWABLE_BOTTOM);
        alias(ATTR_DRAWABLE_START_COMPAT, ATTR_DRAWABLE_START);
        alias(ATTR_DRAWABLE_END_COMPAT, ATTR_DRAWABLE_END);
    }
}
