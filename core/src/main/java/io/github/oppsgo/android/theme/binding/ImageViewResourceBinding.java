package io.github.oppsgo.android.theme.binding;

import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.AnyRes;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.resource.ColorRef;
import io.github.oppsgo.android.theme.resource.ColorStateListRef;
import io.github.oppsgo.android.theme.resource.DrawableRef;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * {@link ImageView} 绑定，额外跟踪 src 和 tint。
 * src 的显示尺寸见 {@link #applySrc}：调用方用 {@code Drawable.setBounds} 指定。
 */
public class ImageViewResourceBinding extends BaseViewResourceBinding<ImageView> {

    public static final int ATTR_SRC = android.R.attr.src;
    public static final int ATTR_TINT = android.R.attr.tint;

    public ImageViewResourceBinding(@NonNull ImageView view) {
        super(view);
    }

    /** 已有本类或子类就返回；否则给一个不挂到 View 上的实例。 */
    @NonNull
    public static ImageViewResourceBinding of(@NonNull ImageView view) {
        return of(view, ImageViewResourceBinding.class, ImageViewResourceBinding::new);
    }

    @Override
    protected int[] getViewStyleable() {
        return mergeStyleable(super.getViewStyleable(),
                ATTR_SRC,
                ATTR_TINT);
    }

    @Nullable
    @Override
    protected ResourceRef<?> createResource(@AttrRes int attr, @AnyRes int resId) {
        if (attr == ATTR_SRC) {
            return resId == ID_NULL ? null : DrawableRef.of(resId);
        }
        if (attr == ATTR_TINT) {
            return createColorResource(resId);
        }
        return super.createResource(attr, resId);
    }

    @NonNull
    public ImageViewResourceBinding setImageResource(@DrawableRes int resId) {
        if (resId == ID_NULL) {
            unbind(ATTR_SRC);
            return this;
        }
        return setImage(DrawableRef.of(resId));
    }

    @NonNull
    public ImageViewResourceBinding setImage(@NonNull DrawableRef src) {
        putAndUpdate(ATTR_SRC, src);
        return this;
    }

    @NonNull
    public ImageViewResourceBinding setImageTint(@ColorRes int tint) {
        if (tint == ID_NULL) {
            unbind(ATTR_TINT);
            return this;
        }
        putAndUpdate(ATTR_TINT, createColorResource(tint));
        return this;
    }

    @NonNull
    public ImageViewResourceBinding setImageTint(@NonNull ColorRef tint) {
        putAndUpdate(ATTR_TINT, tint);
        return this;
    }

    @NonNull
    public ImageViewResourceBinding setImageTint(@NonNull ColorStateListRef tint) {
        putAndUpdate(ATTR_TINT, tint);
        return this;
    }

    @Override
    protected void updateAttribute(@NonNull ResourceResolver resolver, @AttrRes int attr, @Nullable ResourceRef<?> value) {
        if (value == null || value.isEmpty()) return;
        if (attr == ATTR_SRC && value instanceof DrawableRef) {
            applySrc(((DrawableRef) value).resolve(resolver));
            return;
        }
        if (attr == ATTR_TINT) {
            ColorStateList tint = resolveTint(resolver, value);
            resolver.getViewCompat().setImageTintList(view, tint);
            return;
        }
        super.updateAttribute(resolver, attr, value);
    }

    /**
     * ImageView 在 measure / draw 时会按 View 尺寸重写 Drawable 的 bounds，调用方的 {@code setBounds} 留不住。
     * 这里在设 src 之前把非空 bounds 写到 LayoutParams，View 就按这个大小排版，图才会放大或缩小。
     * bounds 为空表示没改过尺寸，不动原来的 layout 宽高。
     */
    private void applySrc(@Nullable Drawable drawable) {
        if (drawable != null) {
            Rect bounds = drawable.getBounds();
            if (bounds.width() > 0 && bounds.height() > 0) {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if (lp == null) {
                    view.setLayoutParams(new ViewGroup.LayoutParams(bounds.width(), bounds.height()));
                } else if (lp.width != bounds.width() || lp.height != bounds.height()) {
                    lp.width = bounds.width();
                    lp.height = bounds.height();
                    view.setLayoutParams(lp);
                }
            }
        }
        view.setImageDrawable(drawable);
    }
}
