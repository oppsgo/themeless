package io.github.oppsgo.android.theme.binding.appcompat;

import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.androidx.R2;
import io.github.oppsgo.android.theme.binding.CompoundButtonBinding;

/**
 * AppCompat 系列 CompoundButton（{@code AppCompatCheckBox} / {@code AppCompatRadioButton} /
 * {@code AppCompatToggleButton} 等）：把布局里的 {@code app:buttonTint} 归一到平台同名属性。
 */
public class AppCompatCompoundButtonBinding extends CompoundButtonBinding {

    /** {@code app:buttonTint} → 平台 {@code android:buttonTint}。 */
    public static final int ATTR_BUTTON_TINT_COMPAT = R2.appcompat.attr.buttonTint;

    public AppCompatCompoundButtonBinding(@NonNull CompoundButton view) {
        super(view);
        alias(ATTR_BUTTON_TINT_COMPAT, ATTR_BUTTON_TINT);
    }
}
