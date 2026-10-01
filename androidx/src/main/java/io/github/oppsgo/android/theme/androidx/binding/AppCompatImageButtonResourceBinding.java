package io.github.oppsgo.android.theme.androidx.binding;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.appcompat.R;
import androidx.appcompat.widget.AppCompatImageButton;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ImageViewResourceBinding;
import io.github.oppsgo.android.theme.binding.ViewResourceBinding;

public class AppCompatImageButtonResourceBinding extends ImageViewResourceBinding {

    public static final int ATTR_SRC_COMPAT = R.attr.srcCompat;
    /**
     * {@code app:tint}，和 {@link ImageViewResourceBinding#ATTR_TINT}（{@code android:tint}）不是同一个 id。
     */
    public static final int ATTR_TINT_COMPAT = R.attr.tint;
    /**
     * {@code app:backgroundTint}，和 {@link ViewResourceBinding#ATTR_BACKGROUND_TINT} 不是同一个 id。
     */
    public static final int ATTR_BACKGROUND_TINT_COMPAT = R.attr.backgroundTint;

    public static void register() {
        ThemeManager.get().registry().register(AppCompatImageButton.class, AppCompatImageButtonResourceBinding::new);
    }

    @NonNull
    public static AppCompatImageButtonResourceBinding of(@NonNull AppCompatImageButton view) {
        return of(view, AppCompatImageButtonResourceBinding.class, AppCompatImageButtonResourceBinding::new);
    }

    public AppCompatImageButtonResourceBinding(@NonNull AppCompatImageButton view) {
        super(view);
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(super.getViewStyleable(),
                ATTR_SRC_COMPAT,
                ATTR_TINT_COMPAT,
                ATTR_BACKGROUND_TINT_COMPAT);
    }

    @AttrRes
    @Override
    protected int normalizeAttr(@AttrRes int attr) {
        if (attr == ATTR_SRC_COMPAT) return ImageViewResourceBinding.ATTR_SRC;
        if (attr == ATTR_TINT_COMPAT) return ImageViewResourceBinding.ATTR_TINT;
        if (attr == ATTR_BACKGROUND_TINT_COMPAT) return ViewResourceBinding.ATTR_BACKGROUND_TINT;
        return super.normalizeAttr(attr);
    }
}
