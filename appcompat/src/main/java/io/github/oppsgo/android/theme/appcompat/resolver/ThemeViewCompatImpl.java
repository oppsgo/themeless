package io.github.oppsgo.android.theme.appcompat.resolver;

import android.content.res.ColorStateList;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ImageViewCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ThemeViewCompat;

/**
 * Support Library：background / image 走 Compat；
 * compound drawable 在 Support 28 无对应 API，退回平台（API 23+）。
 */
public class ThemeViewCompatImpl implements ThemeViewCompat {

    @Override
    public void setBackgroundTintList(@NonNull View view, @Nullable ColorStateList tint) {
        ViewCompat.setBackgroundTintList(view, tint);
    }

    @Override
    public void setImageTintList(@NonNull ImageView view, @Nullable ColorStateList tint) {
        ImageViewCompat.setImageTintList(view, tint);
    }

    @Override
    public void setCompoundDrawableTintList(@NonNull TextView view, @Nullable ColorStateList tint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        view.setCompoundDrawableTintList(tint);
    }
}
