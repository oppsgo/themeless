package io.github.oppsgo.android.theme.resource;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link ResourceProvider#provide} 的结果包装，把「有值」和「取不到值」分开。
 * <p>
 * 两种情况原本都表现为 {@code null}，无法区分：
 * <ul>
 *   <li>{@link #isPresent()} 为 {@code true}：绑定有效且解析完成，{@link #get()} 就是要写入的值。
 *       值本身可为 {@code null}——表示「就是要设置成 null」（如清除背景 / 某方向 compound drawable），
 *       调用方应照常写回 View。</li>
 *   <li>{@link #isPresent()} 为 {@code false}（{@link #empty()}）：没有可写的值——
 *       资源没绑定（既无自定义 {@link ResourceProvider} 也无资源 id），
 *       或资源 id 有但当前资源表解析不到（如皮肤包缺该资源），
 *       调用方应跳过本次写入、保留 View 现状。</li>
 * </ul>
 * 判断「显式 null」必须先看 {@link #isPresent()}：没值时 {@link #get()} 也返回 {@code null}。
 */
public final class ResourceValue<T> {

    private static final ResourceValue<?> EMPTY = new ResourceValue<>(false, null);

    private final boolean present;

    @Nullable
    private final T value;

    private ResourceValue(boolean present, @Nullable T value) {
        this.present = present;
        this.value = value;
    }

    /**
     * 有值。{@code value} 为 {@code null} 表示显式设置成 null，会照常写回 View。
     */
    @NonNull
    public static <T> ResourceValue<T> of(@Nullable T value) {
        return new ResourceValue<>(true, value);
    }

    /**
     * 没值：未绑定或解析不到，调用方跳过写入。
     */
    @NonNull
    public static <T> ResourceValue<T> empty() {
        @SuppressWarnings("unchecked")
        ResourceValue<T> result = (ResourceValue<T>) EMPTY;
        return result;
    }

    public boolean isPresent() {
        return present;
    }

    /**
     * 解析出的值：有值时为结果（可能是显式 null），没值时恒为 null。
     */
    @Nullable
    public T get() {
        return value;
    }

    @NonNull
    @Override
    public String toString() {
        return present ? "ResourceValue[" + value + "]" : "ResourceValue.empty";
    }
}
