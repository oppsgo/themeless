package io.github.oppsgo.android.theme;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * 主题属性标识：要么是布局 / 平台 {@code R.attr}，要么是业务自有 token。
 * <p>
 * 两种形态相等性互不重叠——{@link #of(int)} 永远不会 equals {@link #of(Object)}。
 * attr 形态可参与布局 {@link android.util.AttributeSet} 解析；token 形态仅走绑定表刷新。
 */
public final class ThemeKey {

    /** attr 形态时的 id；token 形态时无意义。 */
    @AttrRes
    private final int attr;

    /**
     * {@code null} 表示 attr 形态；非 null 表示业务 token 形态。
     */
    @Nullable
    private final Object token;

    private ThemeKey(@AttrRes int attr) {
        this.attr = attr;
        this.token = null;
    }

    private ThemeKey(@NonNull Object token) {
        this.attr = 0;
        this.token = token;
    }

    /**
     * 布局 / 标准属性（会进入 XML 读取）。
     */
    @NonNull
    public static ThemeKey of(@AttrRes int attr) {
        return new ThemeKey(attr);
    }

    /**
     * 业务自有 key：推荐 {@link String} 常量或 {@code enum}；也可以是 {@link Integer} 等任意对象。
     * 不参与 XML 读取。
     * <p>
     * 与 {@link #of(int)} 靠形态隔离：即便 token 是某个 attr id 的装箱值，
     * 也不会与 {@code ThemeKey.of(该 attr)} 相等或进 XML。
     * 注意重载：裸 {@code int} / 未转型的整型常量会走 {@link #of(int)}（attr 形态）；
     * 若确实要把整数当 token，请传入 {@link Integer} 或先赋给 {@code Object}。
     *
     * @throws NullPointerException {@code token} 为 null
     */
    @NonNull
    public static ThemeKey of(@NonNull Object token) {
        return new ThemeKey(Objects.requireNonNull(token, "token"));
    }

    /**
     * 是否为 {@code R.attr} 形态（可参与布局解析）。
     */
    public boolean isAttr() {
        return token == null;
    }

    /**
     * attr 形态时的资源 id。
     *
     * @throws IllegalStateException 当前为 token 形态
     */
    @AttrRes
    public int getAttr() {
        if (token != null) {
            throw new IllegalStateException("ThemeKey is token form, not R.attr");
        }
        return attr;
    }

    /**
     * token 形态时的业务对象；attr 形态返回 {@code null}。
     */
    @Nullable
    public Object getToken() {
        return token;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof ThemeKey)) return false;
        ThemeKey other = (ThemeKey) o;
        if (token == null) {
            return other.token == null && attr == other.attr;
        }
        return other.token != null && token.equals(other.token);
    }

    @Override
    public int hashCode() {
        return token == null ? attr : token.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        if (token == null) {
            return "ThemeKey[attr=0x" + Integer.toHexString(attr) + "]";
        }
        return "ThemeKey[token=" + token + "]";
    }
}
