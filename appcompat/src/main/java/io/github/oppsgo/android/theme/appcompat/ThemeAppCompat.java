package io.github.oppsgo.android.theme.appcompat;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.appcompat.binding.AppCompatCompoundButtonResourceBinding;
import io.github.oppsgo.android.theme.appcompat.binding.AppCompatImageViewResourceBinding;
import io.github.oppsgo.android.theme.appcompat.binding.AppCompatTextViewResourceBinding;
import io.github.oppsgo.android.theme.appcompat.binding.RecyclerViewResourceBinding;
import io.github.oppsgo.android.theme.appcompat.binding.SwitchCompatResourceBinding;
import io.github.oppsgo.android.theme.appcompat.binding.ViewPagerResourceBinding;

/**
 * Support Library AppCompat 扩展入口。
 * <p>
 * Binding 对应的控件依赖均为可选（compileOnly）；用 {@link #registerAvailable()}
 * 按 classpath 尽力登记，缺依赖则跳过，宿主不必按控件逐个判断。
 */
public final class ThemeAppCompat {

    private ThemeAppCompat() {
    }

    /**
     * inflate 前调用：有对应控件依赖则 {@code register}，没有则静默跳过（幂等）。
     * <p>
     * 覆盖 AppCompat 文本/图片/CompoundButton、SwitchCompat、RecyclerView、ViewPager。
     * Support 栈无 ViewPager2 Binding。
     */
    public static void registerAvailable() {
        tryRegister(AppCompatTextViewResourceBinding::register);
        tryRegister(AppCompatImageViewResourceBinding::register);
        tryRegister(AppCompatCompoundButtonResourceBinding::register);
        tryRegister(SwitchCompatResourceBinding::register);
        tryRegister(RecyclerViewResourceBinding::register);
        tryRegister(ViewPagerResourceBinding::register);
    }

    @FunctionalInterface
    private interface RegisterAction {
        void run() throws ClassNotFoundException;
    }

    private static void tryRegister(@NonNull RegisterAction register) {
        try {
            register.run();
        } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            // 控件 jar 不在运行时 classpath 时跳过。
        }
    }
}
