package io.github.oppsgo.android.theme.androidx.binding;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewResourceBinding;
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding;
import io.github.oppsgo.theme.androidx.R;

/**
 * 读 {@code app:backgroundTint} / {@code app:drawableTint}，
 * 收成 {@link TextViewResourceBinding} 的平台 attr。
 * tint 写入由 {@link io.github.oppsgo.android.theme.ResourceResolver#getViewCompat()} 提供。
 */
public class AppCompatTextViewResourceBinding extends TextViewResourceBinding {

    /**
     * {@code app:backgroundTint}，和 {@link BaseViewResourceBinding#ATTR_BACKGROUND_TINT} 不是同一个 id。
     */
    public static final int ATTR_BACKGROUND_TINT_COMPAT = R.attr.backgroundTint;
    /**
     * {@code app:drawableTint}，和 {@link TextViewResourceBinding#ATTR_DRAWABLE_TINT} 不是同一个 id。
     */
    public static final int ATTR_DRAWABLE_TINT_COMPAT = R.attr.drawableTint;

    public static void register() {
        ThemeManager.get().bindings().register(AppCompatTextView.class, AppCompatTextViewResourceBinding::new);
    }

    public AppCompatTextViewResourceBinding(@NonNull AppCompatTextView view) {
        super(view);
    }

    @NonNull
    public static AppCompatTextViewResourceBinding of(@NonNull AppCompatTextView view) {
        return of(view, AppCompatTextViewResourceBinding.class, AppCompatTextViewResourceBinding::new);
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(
                super.getViewStyleable(),
                ATTR_BACKGROUND_TINT_COMPAT,
                ATTR_DRAWABLE_TINT_COMPAT
        );
    }

    @AttrRes
    @Override
    protected int normalizeAttr(@AttrRes int attr) {
        if (attr == ATTR_BACKGROUND_TINT_COMPAT) return BaseViewResourceBinding.ATTR_BACKGROUND_TINT;
        if (attr == ATTR_DRAWABLE_TINT_COMPAT) return TextViewResourceBinding.ATTR_DRAWABLE_TINT;
        return super.normalizeAttr(attr);
    }
}
