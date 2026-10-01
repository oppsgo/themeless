package io.github.oppsgo.android.theme.appcompat.binding;

import android.support.v7.appcompat.R;
import android.support.v7.widget.AppCompatTextView;
import android.widget.TextView;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding;
import io.github.oppsgo.android.theme.binding.ViewResourceBinding;

/**
 * Support Library（appcompat-v7）版：读 {@code app:backgroundTint}。
 * tint 写入由 {@link io.github.oppsgo.android.theme.ResourceResolver#getViewCompat()} 提供。
 * <p>
 * 构造取平台 {@link TextView}，便于 Button / EditText 等与 {@link AppCompatTextView} 共用登记。
 */
public class AppCompatTextViewResourceBinding extends TextViewResourceBinding {

    public static final int ATTR_BACKGROUND_TINT_COMPAT = R.attr.backgroundTint;

    public static void register() {
        ThemeManager.get().registry().register(AppCompatTextView.class, AppCompatTextViewResourceBinding::new);
    }

    public AppCompatTextViewResourceBinding(@NonNull TextView view) {
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
                ATTR_BACKGROUND_TINT_COMPAT
        );
    }

    @AttrRes
    @Override
    protected int normalizeAttr(@AttrRes int attr) {
        if (attr == ATTR_BACKGROUND_TINT_COMPAT) return ViewResourceBinding.ATTR_BACKGROUND_TINT;
        return super.normalizeAttr(attr);
    }
}
