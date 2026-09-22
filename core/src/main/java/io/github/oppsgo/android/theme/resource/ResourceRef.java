package io.github.oppsgo.android.theme.resource;

import androidx.annotation.AnyRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ResourceBinding;

/**
 * 绑在 View 上的资源引用。记下资源 id，主题变化时再取出具体值。
 * {@link Resolver} 为空时从 {@link ResourceResolver} 读取；
 * 不为空时直接使用 Resolver 的返回值。
 */
public abstract class ResourceRef<T> {

    /**
     * 自定义取值。{@code resourceId} 仍会传进来，方便基于当前主题资源做变换。
     */
    public interface Resolver<T> {
        @Nullable
        T resolve(@NonNull ResourceResolver resolver, @AnyRes int resourceId);
    }

    @AnyRes
    private final int resourceId;

    @Nullable
    private final Resolver<T> customResolver;

    protected ResourceRef(@AnyRes int resourceId, @Nullable Resolver<T> customResolver) {
        this.resourceId = resourceId;
        this.customResolver = customResolver;
    }

    @AnyRes
    public final int getResourceId() {
        return resourceId;
    }

    public final boolean hasResolver() {
        return customResolver != null;
    }

    /**
     * 没有资源 id，也没有 {@link Resolver}，刷新时跳过。
     */
    public final boolean isEmpty() {
        return resourceId == ResourceBinding.ID_NULL && customResolver == null;
    }

    /**
     * {@link Resolver} 优先。没有 Resolver 时按 {@link #getResourceId()} 调用 {@link #load}。
     */
    @Nullable
    public final T resolve(@NonNull ResourceResolver resolver) {
        if (customResolver != null) {
            return customResolver.resolve(resolver, resourceId);
        }
        if (resourceId == ResourceBinding.ID_NULL) {
            return null;
        }
        return load(resolver, resourceId);
    }

    @Nullable
    protected abstract T load(@NonNull ResourceResolver resolver, @AnyRes int resourceId);
}
