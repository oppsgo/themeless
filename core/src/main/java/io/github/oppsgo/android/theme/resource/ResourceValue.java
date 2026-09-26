package io.github.oppsgo.android.theme.resource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceResolver;

/**
 * 可随主题解析出具体值的资源。
 * {@link ResourceRef} 是带资源 id 的实现；也可直接实现本接口做自定义取值。
 */
@FunctionalInterface
public interface ResourceValue<T> {

    @Nullable
    T resolve(@NonNull ResourceResolver resolver);
}
