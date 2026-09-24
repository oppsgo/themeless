package io.github.oppsgo.android.theme.resolver;

import android.content.res.ColorStateList;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ThemeViewCompat;

/** 平台实现：API 不够时 no-op。 */
public class ThemeViewCompatImpl implements ThemeViewCompat {

    @Override
    public void setBackgroundTintList(@NonNull View view, @Nullable ColorStateList tint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        view.setBackgroundTintList(tint);
    }

    @Override
    public void setImageTintList(@NonNull ImageView view, @Nullable ColorStateList tint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;
        view.setImageTintList(tint);
    }

    @Override
    public void setCompoundDrawableTintList(@NonNull TextView view, @Nullable ColorStateList tint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        view.setCompoundDrawableTintList(tint);
    }
}
