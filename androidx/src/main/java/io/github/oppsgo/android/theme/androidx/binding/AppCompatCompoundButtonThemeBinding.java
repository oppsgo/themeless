package io.github.oppsgo.android.theme.androidx.binding;

import android.widget.CompoundButton;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.binding.CompoundButtonThemeBinding;

/**
 * AppCompat 系列 CompoundButton（{@code AppCompatCheckBox} / {@code AppCompatRadioButton} /
 * {@code AppCompatToggleButton} 等）：把布局里的 {@code app:buttonTint} 归一到平台同名属性。
 */
public class AppCompatCompoundButtonThemeBinding extends CompoundButtonThemeBinding {

    /** {@code app:buttonTint} → 平台 {@code android:buttonTint}。 */
    public static final int ATTR_BUTTON_TINT_COMPAT = androidx.appcompat.R.attr.buttonTint;

    public AppCompatCompoundButtonThemeBinding(@NonNull CompoundButton view) {
        super(view);
        alias(ATTR_BUTTON_TINT_COMPAT, ATTR_BUTTON_TINT);
    }
}
