package io.github.oppsgo.android.theme.resource;

import android.content.res.Resources;
import android.util.TypedValue;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;

/**
 * 绑在 View 上的资源引用。记下资源 id，主题变化时再取出具体值。
 * 自定义 {@link ResourceProvider} 不为空时直接使用其返回值，否则按 id 从 {@link ResourceResolver} 读取。
 */
public abstract class ResourceRef<T> implements ResourceProvider<T> {

    /**
     * 无效资源 id（值为 {@code 0}）。
     * 语义同系统 {@code Resources.ID_NULL}，自建常量以避免 API 29+ 告警。
     */
    public static final int ID_NULL = 0;

    @AnyRes
    private final int resourceId;

    @Nullable
    private final ResourceProvider<T> provider;

    protected ResourceRef(@AnyRes int resourceId, @Nullable ResourceProvider<T> provider) {
        this.resourceId = resourceId;
        this.provider = provider;
    }

    /**
     * 资源既可以是颜色也可以是图片时（如 {@code background}），靠 {@link TypedValue} 的类型区分。
     * 不缓存：只在 bind / setXxx 时调用，直接读当前资源表可避免外挂 APK 换肤时串台。
     */
    @Nullable
    public static ResourceRef<?> fromColorOrDrawable(@NonNull Resources resources, @AnyRes int resourceId) {
        if (resourceId == ID_NULL) {
            return null;
        }
        return isColor(resources, resourceId) ? ColorRef.of(resourceId) : DrawableRef.of(resourceId);
    }

    /**
     * 资源既可以是纯色也可以是 selector 时（如各种 tint / textColor），按类型选 {@link ColorRef} 或 {@link ColorStateListRef}。
     */
    @Nullable
    public static ResourceRef<?> fromColor(@NonNull Resources resources, @ColorRes int resourceId) {
        if (resourceId == ID_NULL) {
            return null;
        }
        return isColor(resources, resourceId) ? ColorRef.of(resourceId) : ColorStateListRef.of(resourceId);
    }

    private static boolean isColor(@NonNull Resources resources, @AnyRes int resourceId) {
        try {
            TypedValue value = new TypedValue();
            resources.getValue(resourceId, value, true);
            return value.type >= TypedValue.TYPE_FIRST_COLOR_INT
                    && value.type <= TypedValue.TYPE_LAST_COLOR_INT;
        } catch (Resources.NotFoundException ignored) {
            return false;
        }
    }

    @AnyRes
    public int getResourceId() {
        return resourceId;
    }

    public boolean hasProvider() {
        return provider != null;
    }

    /**
     * 没有资源 id，也没有自定义 {@link ResourceProvider}，刷新时跳过。
     */
    public boolean isEmpty() {
        return resourceId == ID_NULL && provider == null;
    }

    /**
     * 自定义 {@link ResourceProvider} 优先。没有时按 {@link #getResourceId()} 调用 {@link #load}。
     */
    @Nullable
    @Override
    public T provide(@NonNull ResourceResolver resolver) {
        if (provider != null) {
            return provider.provide(resolver);
        }
        if (resourceId == ID_NULL) {
            return null;
        }
        return load(resolver, resourceId);
    }

    @Nullable
    protected abstract T load(@NonNull ResourceResolver resolver, @AnyRes int resourceId);
}
