package io.github.oppsgo.android.theme.androidx;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatMultiAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.AppCompatToggleButton;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import io.github.oppsgo.android.theme.ResourceBindingFactory;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatCompoundButtonResourceBinding;
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
 * <p>
 * 同一 Binding 可挂多个具体类：{@link ResourceBindingFactory#resolve} 虽会沿继承链向上找，
 * 但子类若只命中平台 {@code TextView}/{@code ImageView} 会丢掉 app: tint 能力，
 * 因此 AppCompat / Material 具体类仍显式登记到对应 AppCompat Binding。
 */
public final class ThemeAndroidX {

    private ThemeAndroidX() {
    }

    /**
     * inflate 前调用：有对应控件依赖则 {@code register}，没有则静默跳过（幂等）。
     * <p>
     * 覆盖 AppCompat 文本/图片/CompoundButton、SwitchCompat、RecyclerView、
     * ViewPager、ViewPager2、部分 Material 控件（与 AppCompat Binding 共用）、TabLayout。
     */
    public static void registerAvailable() {
        ResourceBindingFactory registry = ThemeManager.get().registry();

        // TextView 系：共用 AppCompatTextViewResourceBinding（含 app:backgroundTint / drawableTint）
        tryRegister(() -> {
            registry.register(AppCompatTextView.class, AppCompatTextViewResourceBinding::new);
            registry.register(AppCompatButton.class, AppCompatTextViewResourceBinding::new);
            registry.register(AppCompatEditText.class, AppCompatTextViewResourceBinding::new);
            registry.register(AppCompatAutoCompleteTextView.class, AppCompatTextViewResourceBinding::new);
            registry.register(AppCompatMultiAutoCompleteTextView.class, AppCompatTextViewResourceBinding::new);
        });

        // ImageView 系
        tryRegister(() -> {
            registry.register(AppCompatImageView.class, AppCompatImageViewResourceBinding::new);
            registry.register(AppCompatImageButton.class, AppCompatImageViewResourceBinding::new);
        });

        // CompoundButton 系（不含 SwitchCompat）
        tryRegister(() -> {
            registry.register(AppCompatCheckBox.class, AppCompatCompoundButtonResourceBinding::new);
            registry.register(AppCompatRadioButton.class, AppCompatCompoundButtonResourceBinding::new);
            registry.register(AppCompatToggleButton.class, AppCompatCompoundButtonResourceBinding::new);
        });

        tryRegister(() -> registry.register(SwitchCompat.class, SwitchCompatResourceBinding::new));
        tryRegister(() -> registry.register(RecyclerView.class, RecyclerViewResourceBinding::new));
        tryRegister(() -> registry.register(ViewPager.class, ViewPagerResourceBinding::new));
        tryRegister(() -> registry.register(ViewPager2.class, ViewPager2ResourceBinding::new));

        // Material：与上面对应的 AppCompat Binding 共用（单独 try，缺 material 不拖垮 AppCompat）
        tryRegister(() -> {
            registry.register(MaterialTextView.class, AppCompatTextViewResourceBinding::new);
            registry.register(MaterialButton.class, AppCompatTextViewResourceBinding::new);
        });
        tryRegister(() -> registry.register(ShapeableImageView.class, AppCompatImageViewResourceBinding::new));
        tryRegister(() -> {
            registry.register(MaterialCheckBox.class, AppCompatCompoundButtonResourceBinding::new);
            registry.register(MaterialRadioButton.class, AppCompatCompoundButtonResourceBinding::new);
        });
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
