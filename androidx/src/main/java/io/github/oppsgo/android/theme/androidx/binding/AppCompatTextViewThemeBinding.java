package io.github.oppsgo.android.theme.androidx.binding;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import io.github.oppsgo.android.theme.binding.TextViewThemeBinding;

/**
 * {@link AppCompatTextView} 及同继承链（AppCompatButton / AppCompatEditText / MaterialButton 等）：
 * 把布局里的 {@code app:backgroundTint} / {@code app:drawableTint} 归一到平台同名属性。
 * 具体属性能力见 {@link TextViewThemeBinding}。
 */
public class AppCompatTextViewThemeBinding extends TextViewThemeBinding {

    /** {@code app:backgroundTint} → 平台 {@code android:backgroundTint}。 */
    public static final int ATTR_BACKGROUND_TINT_COMPAT = androidx.appcompat.R.attr.backgroundTint;
    /** {@code app:drawableTint} → 平台 {@code android:drawableTint}。 */
    public static final int ATTR_DRAWABLE_TINT_COMPAT = androidx.appcompat.R.attr.drawableTint;

    public AppCompatTextViewThemeBinding(@NonNull TextView view) {
        super(view);
        alias(ATTR_BACKGROUND_TINT_COMPAT, ATTR_BACKGROUND_TINT);
        alias(ATTR_DRAWABLE_TINT_COMPAT, ATTR_DRAWABLE_TINT);
    }
}
