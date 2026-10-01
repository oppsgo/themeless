package io.github.oppsgo.android.theme.androidx;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import io.github.oppsgo.android.theme.ResourceBindingFactory;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatCompoundButtonResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatImageButtonResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatImageViewResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatTextViewResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.RecyclerViewResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.SwitchCompatResourceBinding;
import io.github.oppsgo.android.theme.androidx.binding.TabLayoutResourceBinding;
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
     * ViewPager、ViewPager2、Material TabLayout。
     */
    public static void registerAvailable() {
        ResourceBindingFactory registry = ThemeManager.get().registry();
        tryRegister(() -> registry.register(AppCompatTextView.class, AppCompatTextViewResourceBinding::new));
        tryRegister(() -> registry.register(AppCompatImageView.class, AppCompatImageViewResourceBinding::new));
        tryRegister(() -> registry.register(AppCompatImageButton.class, AppCompatImageButtonResourceBinding::new));
        tryRegister(() -> {
            registry.register(AppCompatCheckBox.class, AppCompatCompoundButtonResourceBinding::new);
            registry.register(AppCompatRadioButton.class, AppCompatCompoundButtonResourceBinding::new);
            registry.register(AppCompatToggleButton.class, AppCompatCompoundButtonResourceBinding::new);
        });
        tryRegister(() -> registry.register(SwitchCompat.class, SwitchCompatResourceBinding::new));
        tryRegister(() -> registry.register(RecyclerView.class, RecyclerViewResourceBinding::new));
        tryRegister(() -> registry.register(ViewPager.class, ViewPagerResourceBinding::new));
        tryRegister(() -> registry.register(ViewPager2.class, ViewPager2ResourceBinding::new));
        tryRegister(() -> registry.register(TabLayout.class, TabLayoutResourceBinding::new));
    }

    private static void tryRegister(@NonNull Runnable register) {
        try {
            register.run();
        } catch (NoClassDefFoundError | ExceptionInInitializerError ignored) {
            // 控件 jar 不在运行时 classpath 时跳过。
        }
    }
}
