package io.github.oppsgo.android.theme.appcompat;

import android.support.v4.view.ViewPager;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceBindingFactory;
import io.github.oppsgo.android.theme.ThemeManager;
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
     * <p>
     * 每条登记包在 lambda 里执行：缺控件 jar 时加载类抛出的
     * {@link NoClassDefFoundError} 才能被接住。不要改成在 try 外写
     * {@code XxxBinding::register} / 提前取 {@code SomeView.class}。
     */
    public static void registerAvailable() {
        ResourceBindingFactory registry = ThemeManager.get().registry();
        tryRegister(() -> registry.register(
                AppCompatTextView.class, AppCompatTextViewResourceBinding::new));
        tryRegister(() -> registry.register(
                AppCompatImageView.class, AppCompatImageViewResourceBinding::new));
        tryRegister(() -> {
            registry.register(AppCompatCheckBox.class, AppCompatCompoundButtonResourceBinding::new);
            registry.register(AppCompatRadioButton.class, AppCompatCompoundButtonResourceBinding::new);
        });
        tryRegister(() -> registry.register(
                SwitchCompat.class, SwitchCompatResourceBinding::new));
        tryRegister(() -> registry.register(
                RecyclerView.class, RecyclerViewResourceBinding::new));
        tryRegister(() -> registry.register(
                ViewPager.class, ViewPagerResourceBinding::new));
    }

    private static void tryRegister(@NonNull Runnable register) {
        try {
            register.run();
        } catch (NoClassDefFoundError | ExceptionInInitializerError ignored) {
            // 控件 jar 不在运行时 classpath 时跳过。
        }
    }
}
