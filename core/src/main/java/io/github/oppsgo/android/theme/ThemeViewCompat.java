package io.github.oppsgo.android.theme;

import android.content.res.ColorStateList;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 往 View 上写属性的兼容层（角色类似官方 {@code ViewCompat}）。
 * 挂在 {@link ResourceResolver} 上：选哪套 Resolver，就带哪套写 View 的实现。
 */
public interface ThemeViewCompat {

    void setBackgroundTintList(@NonNull View view, @Nullable ColorStateList tint);

    void setImageTintList(@NonNull ImageView view, @Nullable ColorStateList tint);

    void setCompoundDrawableTintList(@NonNull TextView view, @Nullable ColorStateList tint);
}
