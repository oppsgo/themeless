package io.github.oppsgo.android.theme.resource;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceResolver;

/**
 * 可随主题解析出具体值的资源。
 * {@link ResourceRef} 是带资源 id 的实现；也可直接实现本接口做自定义取值。
 */
@FunctionalInterface
public interface ResourceProvider<T> {

    /**
     * 解析出要写入的值，用 {@link ResourceValue} 区分三种情况：
     * <ul>
     *   <li>{@link ResourceValue#of(Object) of(value)}：有值，照常写回；</li>
     *   <li>{@link ResourceValue#of(Object) of(null)}：显式设置成 null（如清除 drawable）；</li>
     *   <li>{@link ResourceValue#empty() empty()}：取不到值，调用方跳过本次写入。</li>
     * </ul>
     */
    @NonNull
    ResourceValue<T> provide(@NonNull ResourceResolver resolver);
}
