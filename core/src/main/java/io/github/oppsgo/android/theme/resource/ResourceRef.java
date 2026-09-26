package io.github.oppsgo.android.theme.resource;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;

/**
 * 绑在 View 上的资源引用。记下资源 id，主题变化时再取出具体值。
 * 自定义 {@link ResourceValue} 为空时从 {@link ResourceResolver} 读取；
 * 不为空时直接使用其返回值。
 */
public abstract class ResourceRef<T> implements ResourceValue<T> {

    @AnyRes
    private final int resourceId;

    @Nullable
    private final ResourceValue<T> custom;

    protected ResourceRef(@AnyRes int resourceId, @Nullable ResourceValue<T> custom) {
        this.resourceId = resourceId;
        this.custom = custom;
    }

    @AnyRes
    public int getResourceId() {
        return resourceId;
    }

    public boolean hasCustom() {
        return custom != null;
    }

    /**
     * 没有资源 id，也没有自定义 {@link ResourceValue}，刷新时跳过。
     */
    public boolean isEmpty() {
        return resourceId == ResourceBinding.ID_NULL && custom == null;
    }

    /**
     * 自定义 {@link ResourceValue} 优先。没有时按 {@link #getResourceId()} 调用 {@link #load}。
     */
    @Nullable
    @Override
    public T resolve(@NonNull ResourceResolver resolver) {
        if (custom != null) {
            return custom.resolve(resolver);
        }
        if (resourceId == ResourceBinding.ID_NULL) {
            return null;
        }
        return load(resolver, resourceId);
    }

    @Nullable
    protected abstract T load(@NonNull ResourceResolver resolver, @AnyRes int resourceId);
}
