package io.github.oppsgo.android.theme.appcompat.binding;

import android.support.v7.appcompat.R;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.widget.CompoundButton;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewResourceBinding;
import io.github.oppsgo.android.theme.binding.CompoundButtonResourceBinding;

/**
 * Support Library 版：读 {@code app:buttonTint} / {@code app:backgroundTint}，
 * 收成 {@link CompoundButtonResourceBinding} 的平台 attr。
 * 覆盖 {@link AppCompatCheckBox}、{@link AppCompatRadioButton}。
 * <p>
 * Support 无 {@code drawableTint}；无 {@code AppCompatToggleButton}。
 */
public class AppCompatCompoundButtonResourceBinding extends CompoundButtonResourceBinding {

    public static final int ATTR_BUTTON_TINT_COMPAT = R.attr.buttonTint;
    public static final int ATTR_BACKGROUND_TINT_COMPAT = R.attr.backgroundTint;

    public static void register() {
        ThemeManager.get().registry().register(
                AppCompatCheckBox.class, AppCompatCompoundButtonResourceBinding::new);
        ThemeManager.get().registry().register(
                AppCompatRadioButton.class, AppCompatCompoundButtonResourceBinding::new);
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
                ATTR_BACKGROUND_TINT_COMPAT
        );
    }

    @AttrRes
    @Override
    protected int normalizeAttr(@AttrRes int attr) {
        if (attr == ATTR_BUTTON_TINT_COMPAT) return CompoundButtonResourceBinding.ATTR_BUTTON_TINT;
        if (attr == ATTR_BACKGROUND_TINT_COMPAT) return BaseViewResourceBinding.ATTR_BACKGROUND_TINT;
        return super.normalizeAttr(attr);
    }
}
