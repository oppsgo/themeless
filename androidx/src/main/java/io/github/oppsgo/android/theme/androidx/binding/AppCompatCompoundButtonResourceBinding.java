package io.github.oppsgo.android.theme.androidx.binding;

import android.widget.CompoundButton;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.R;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatToggleButton;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ViewResourceBinding;
import io.github.oppsgo.android.theme.binding.CompoundButtonResourceBinding;
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding;

/**
 * 读 {@code app:buttonTint} / {@code app:backgroundTint} / {@code app:drawableTint}，
 * 收成 {@link CompoundButtonResourceBinding} / TextView 的平台 attr。
 * 覆盖 {@link AppCompatCheckBox}、{@link AppCompatRadioButton}、{@link AppCompatToggleButton}。
 */
public class AppCompatCompoundButtonResourceBinding extends CompoundButtonResourceBinding {

    public static final int ATTR_BUTTON_TINT_COMPAT = R.attr.buttonTint;
    public static final int ATTR_BACKGROUND_TINT_COMPAT = R.attr.backgroundTint;
    public static final int ATTR_DRAWABLE_TINT_COMPAT = R.attr.drawableTint;

    public static void register() {
        ThemeManager.get().registry().register(
                AppCompatCheckBox.class, AppCompatCompoundButtonResourceBinding::new);
        ThemeManager.get().registry().register(
                AppCompatRadioButton.class, AppCompatCompoundButtonResourceBinding::new);
        ThemeManager.get().registry().register(
                AppCompatToggleButton.class, AppCompatCompoundButtonResourceBinding::new);
    }

    public AppCompatCompoundButtonResourceBinding(@NonNull CompoundButton view) {
        super(view);
    }

    @NonNull
    public static AppCompatCompoundButtonResourceBinding of(@NonNull CompoundButton view) {
        return of(view, AppCompatCompoundButtonResourceBinding.class,
                AppCompatCompoundButtonResourceBinding::new);
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(
                super.getViewStyleable(),
                ATTR_BUTTON_TINT_COMPAT,
                ATTR_BACKGROUND_TINT_COMPAT,
                ATTR_DRAWABLE_TINT_COMPAT
        );
    }

    @AttrRes
    @Override
    protected int normalizeAttr(@AttrRes int attr) {
        if (attr == ATTR_BUTTON_TINT_COMPAT) return CompoundButtonResourceBinding.ATTR_BUTTON_TINT;
        if (attr == ATTR_BACKGROUND_TINT_COMPAT) return ViewResourceBinding.ATTR_BACKGROUND_TINT;
        if (attr == ATTR_DRAWABLE_TINT_COMPAT) return TextViewResourceBinding.ATTR_DRAWABLE_TINT;
        return super.normalizeAttr(attr);
    }
}
