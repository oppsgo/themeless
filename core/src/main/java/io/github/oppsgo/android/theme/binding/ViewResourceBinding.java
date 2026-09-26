package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.view.View;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * View 级属性：{@code background} / {@code backgroundTint}。
 * setter 以 {@link ResourceRef} 为单入口；{@code int} 重载只作资源 id 便捷包装。
 */
public class ViewResourceBinding extends BaseViewResourceBinding {

    public static final int ATTR_BACKGROUND = android.R.attr.background;
    public static final int ATTR_BACKGROUND_TINT = android.R.attr.backgroundTint;

    public ViewResourceBinding(@NonNull View view) {
        super(view);
    }

    /** 已有本类或子类就返回；否则给一个不挂到 View 上的实例。 */
    @NonNull
    public static ViewResourceBinding of(@NonNull View view) {
        return of(view, ViewResourceBinding.class, ViewResourceBinding::new);
    }

    @Override
    protected int[] getViewStyleable() {
        return new int[]{
                ATTR_BACKGROUND,
                ATTR_BACKGROUND_TINT,
        };
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (resId == ID_NULL) return null;
        if (attr == ATTR_BACKGROUND) {
            return isPureColor(resId) ? ColorRef.of(resId) : DrawableRef.of(resId);
        }
        if (attr == ATTR_BACKGROUND_TINT) {
            return createColorResource(resId);
        }
        return null;
    }

    @NonNull
    public ViewResourceBinding setBackground(@NonNull ResourceRef<?> background) {
        putAndUpdate(ATTR_BACKGROUND, background);
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackground(@AnyRes int background) {
        if (background == ID_NULL) {
            unbind(ATTR_BACKGROUND);
            return this;
        }
        return setBackground(isPureColor(background) ? ColorRef.of(background) : DrawableRef.of(background));
    }

    @NonNull
    public ViewResourceBinding setBackgroundTint(@Nullable ResourceRef<?> tint) {
        putAndUpdate(ATTR_BACKGROUND_TINT, tint);
        return this;
    }

    @NonNull
    public ViewResourceBinding setBackgroundTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_BACKGROUND_TINT);
            return this;
        }
        return setBackgroundTint(createColorResource(tint));
    }

    @Override
    protected void updateAttribute(
            @NonNull ResourceResolver resolver,
            @AttrRes int attr,
            @Nullable ResourceRef<?> value
    ) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_BACKGROUND) {
            if (value instanceof ColorRef) {
                Integer color = ((ColorRef) value).resolve(resolver);
                if (color != null) view.setBackgroundColor(color);
            } else if (value instanceof DrawableRef) {
                view.setBackground(((DrawableRef) value).resolve(resolver));
            }
            return;
        }
        if (attr == ATTR_BACKGROUND_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            resolver.getViewCompat().setBackgroundTintList(view, tint);
        }
    }
}
