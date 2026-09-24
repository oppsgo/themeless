package io.github.oppsgo.android.theme.androidx.resolver;

import android.content.res.ColorStateList;
import android.os.Build;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.core.widget.TextViewCompat;

import io.github.oppsgo.android.theme.ThemeViewCompat;

/** AndroidX：写 View 走 {@code androidx.core} 的 Compat API。 */
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
        TextViewCompat.setCompoundDrawableTintList(view, tint);
    }

    @Override
    public void setButtonTintList(@NonNull CompoundButton button, @Nullable ColorStateList tint) {
        CompoundButtonCompat.setButtonTintList(button, tint);
    }

    @Override
    public void setThumbTintList(@NonNull Switch switchView, @Nullable ColorStateList tint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        switchView.setThumbTintList(tint);
    }

    @Override
    public void setTrackTintList(@NonNull Switch switchView, @Nullable ColorStateList tint) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        switchView.setTrackTintList(tint);
    }
}
