package io.github.oppsgo.android.theme;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Nullable;

/**
 * 解析当前主题下的 color / drawable / dimen。
 * 亮暗、字号都是不同实现，框架不枚举模式。
 */
public interface ResourceResolver {

    int getColor(@ColorRes int id);

    @Nullable
    ColorStateList getColorStateList(@ColorRes int id);

    /**
     * 以 Drawable 形式加载资源；实现需要支持 drawable 及 color（含 ColorStateList）资源。
     */
    @Nullable
    Drawable getDrawable(@AnyRes int id);

    /**
     * 精确像素值（含小数）。{@code sp} 会乘 {@code fontScale}，适合 {@code setTextSize}。
     */
    float getDimension(@DimenRes int id);

    /**
     * 四舍五入到像素，非 0 至少为 1。适合 padding、宽高。
     */
    int getDimensionPixelSize(@DimenRes int id);

    Resources getResources();
}
