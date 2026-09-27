package io.github.oppsgo.android.theme.androidx;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.androidx.binding.AppCompatCompoundButtonResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatImageViewResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatTextViewResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.RecyclerViewResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.SwitchCompatResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.ViewPager2ResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.ViewPagerResourceBinding;

/**
 * AndroidX 扩展入口。
 * <p>
 * Binding 对应的控件依赖均为可选（compileOnly）；用 {@link #registerAvailable()}
 * 按 classpath 尽力登记，缺依赖则跳过，宿主不必按控件逐个判断。
 */
public final class ThemeAndroidX {

    private ThemeAndroidX() {
    }

    /**
     * inflate 前调用：有对应控件依赖则 {@code register}，没有则静默跳过（幂等）。
     * <p>
     * 覆盖 AppCompat 文本/图片/CompoundButton、SwitchCompat、RecyclerView、
     * ViewPager、ViewPager2。
     */
    public static void registerAvailable() {
        tryRegister(AppCompatTextViewResourceBinding::register);
        tryRegister(AppCompatImageViewResourceBinding::register);
        tryRegister(AppCompatCompoundButtonResourceBinding::register);
        tryRegister(SwitchCompatResourceBinding::register);
        tryRegister(RecyclerViewResourceBinding::register);
        tryRegister(ViewPagerResourceBinding::register);
        tryRegister(ViewPager2ResourceBinding::register);
    }

    private static void tryRegister(@NonNull Runnable register) {
        try {
            register.run();
        } catch (Throwable ignored) {
            // ClassNotFoundException / NoClassDefFoundError / ExceptionInInitializerError 等：
            // 控件 jar 不在运行时 classpath 时跳过。
        }
    }
}
