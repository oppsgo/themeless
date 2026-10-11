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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.ThemeBindingFactory;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatCompoundButtonThemeBinding;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatImageViewThemeBinding;
import io.github.oppsgo.android.theme.androidx.binding.AppCompatTextViewThemeBinding;
import io.github.oppsgo.android.theme.androidx.binding.RecyclerViewThemeBinding;
import io.github.oppsgo.android.theme.androidx.binding.SwitchCompatThemeBinding;
import io.github.oppsgo.android.theme.androidx.binding.TabLayoutThemeBinding;
import io.github.oppsgo.android.theme.androidx.binding.ViewPager2ThemeBinding;

/**
 * AppCompat / Material 组件的 Binding 登记入口。
 * <p>
 * Binding 对应的控件依赖均为可选（compileOnly）；用 {@link #registerAvailable()}
 * 按 classpath 尽力登记，缺依赖则跳过，宿主不必按控件逐个判断。
 * <p>
 * 同一 Binding 可挂多个具体类：{@link ThemeBindingFactory#resolve} 虽会沿继承链向上找，
 * 但子类若只命中平台 {@code TextView}/{@code ImageView} 会丢掉 app: tint 能力，
 * 因此 AppCompat / Material 具体类仍显式登记到对应 AppCompat Binding。
 */
public final class ThemeAndroidX {

    private ThemeAndroidX() {
    }

    /**
     * inflate 前调用：有对应控件依赖则 {@code register}，没有则静默跳过（幂等）。
     */
    public static void registerAvailable() {
        ThemeBindingFactory registry = ThemeManager.get().registry();

        // TextView 系：共用 AppCompatTextViewThemeBinding
        tryRegister(() -> {
            registry.register(AppCompatTextView.class, AppCompatTextViewThemeBinding::new);
            registry.register(AppCompatButton.class, AppCompatTextViewThemeBinding::new);
            registry.register(AppCompatEditText.class, AppCompatTextViewThemeBinding::new);
            registry.register(AppCompatMultiAutoCompleteTextView.class, AppCompatTextViewThemeBinding::new);
        });

        // ImageView 系
        tryRegister(() -> {
            registry.register(AppCompatImageView.class, AppCompatImageViewThemeBinding::new);
            registry.register(AppCompatImageButton.class, AppCompatImageViewThemeBinding::new);
        });

        // CompoundButton 系（不含 SwitchCompat）
        tryRegister(() -> {
            registry.register(AppCompatCheckBox.class, AppCompatCompoundButtonThemeBinding::new);
            registry.register(AppCompatRadioButton.class, AppCompatCompoundButtonThemeBinding::new);
            registry.register(AppCompatToggleButton.class, AppCompatCompoundButtonThemeBinding::new);
        });

        tryRegister(() -> registry.register(SwitchCompat.class, SwitchCompatThemeBinding::new));
        tryRegister(() -> registry.register(RecyclerView.class, RecyclerViewThemeBinding::new));
        tryRegister(() -> registry.register(ViewPager2.class, ViewPager2ThemeBinding::new));

        tryRegister(() -> registry.register(TabLayout.class, TabLayoutThemeBinding::new));
    }

    private static void tryRegister(@NonNull Runnable register) {
        try {
            register.run();
        } catch (NoClassDefFoundError | ExceptionInInitializerError ignored) {
            // 控件 jar 不在运行时 classpath 时跳过。
        }
    }
}
