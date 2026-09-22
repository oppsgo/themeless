package io.github.oppsgo.android.theme.androidx.binding;

import android.content.res.ColorStateList;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.view.ViewCompat;
import androidx.core.widget.ImageViewCompat;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewResourceBinding;
import io.github.oppsgo.android.theme.binding.ImageViewResourceBinding;
import io.github.oppsgo.theme.androidx.R;

/**
 * 读 {@code app:srcCompat} / {@code app:tint} / {@code app:backgroundTint}，
 * 收成 {@link ImageViewResourceBinding} 的平台 attr。
 */
public class AppCompatImageViewResourceBinding extends ImageViewResourceBinding {

    public static final int ATTR_SRC_COMPAT = R.attr.srcCompat;
    /** {@code app:tint}，和 {@link ImageViewResourceBinding#ATTR_TINT}（{@code android:tint}）不是同一个 id。 */
    public static final int ATTR_TINT_COMPAT = R.attr.tint;
    /** {@code app:backgroundTint}，和 {@link BaseViewResourceBinding#ATTR_BACKGROUND_TINT} 不是同一个 id。 */
    public static final int ATTR_BACKGROUND_TINT_COMPAT = R.attr.backgroundTint;

    public static void register() {
        ThemeManager.get().bindings().register(AppCompatImageView.class, AppCompatImageViewResourceBinding::new);
    }

    public AppCompatImageViewResourceBinding(@NonNull AppCompatImageView view) {
        super(view);
    }

    @NonNull
    public static AppCompatImageViewResourceBinding of(@NonNull AppCompatImageView view) {
        return of(view, AppCompatImageViewResourceBinding.class, AppCompatImageViewResourceBinding::new);
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
        if (attr == ATTR_BACKGROUND_TINT_COMPAT) return BaseViewResourceBinding.ATTR_BACKGROUND_TINT;
        return super.normalizeAttr(attr);
    }

    @Override
    protected void applyBackgroundTint(@Nullable ColorStateList tint) {
        ViewCompat.setBackgroundTintList(view, tint);
    }

    @Override
    protected void applyImageTint(@Nullable ColorStateList tint) {
        ImageViewCompat.setImageTintList(view, tint);
    }
}
