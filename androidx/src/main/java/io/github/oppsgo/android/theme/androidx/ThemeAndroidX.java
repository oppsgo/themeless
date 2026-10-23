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
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;

import io.github.oppsgo.android.theme.ThemeBindingFactory;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.appcompat.AppCompatCompoundButtonBinding;
import io.github.oppsgo.android.theme.binding.appcompat.AppCompatImageViewBinding;
import io.github.oppsgo.android.theme.binding.appcompat.AppCompatTextViewBinding;
import io.github.oppsgo.android.theme.binding.appcompat.SwitchCompatBinding;
import io.github.oppsgo.android.theme.binding.cardview.CardViewBinding;
import io.github.oppsgo.android.theme.binding.material.MaterialCardViewBinding;
import io.github.oppsgo.android.theme.binding.material.TabLayoutBinding;
import io.github.oppsgo.android.theme.binding.recyclerview.RecyclerViewBinding;
import io.github.oppsgo.android.theme.binding.viewpager2.ViewPager2Binding;

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

        // TextView 系：共用 AppCompatTextViewBinding
        tryRegister(() -> {
            registry.register(AppCompatTextView.class, AppCompatTextViewBinding::new);
            registry.register(AppCompatButton.class, AppCompatTextViewBinding::new);
            registry.register(AppCompatEditText.class, AppCompatTextViewBinding::new);
            registry.register(AppCompatAutoCompleteTextView.class, AppCompatTextViewBinding::new);
            registry.register(AppCompatMultiAutoCompleteTextView.class, AppCompatTextViewBinding::new);
        });

        // ImageView 系
        tryRegister(() -> {
            registry.register(AppCompatImageView.class, AppCompatImageViewBinding::new);
            registry.register(AppCompatImageButton.class, AppCompatImageViewBinding::new);
        });

        // CompoundButton 系（不含 SwitchCompat）
        tryRegister(() -> {
            registry.register(AppCompatCheckBox.class, AppCompatCompoundButtonBinding::new);
            registry.register(AppCompatRadioButton.class, AppCompatCompoundButtonBinding::new);
            registry.register(AppCompatToggleButton.class, AppCompatCompoundButtonBinding::new);
        });

        tryRegister(() -> registry.register(SwitchCompat.class, SwitchCompatBinding::new));

        // Material 系：文本/按钮等挂到对应 AppCompat Binding，卡片单独登记。
        // 没引 Material 依赖时整块跳过。
        tryRegister(() -> {
            registry.register(MaterialTextView.class, AppCompatTextViewBinding::new);
            registry.register(MaterialButton.class, AppCompatTextViewBinding::new);
            registry.register(MaterialCheckBox.class, AppCompatCompoundButtonBinding::new);
            registry.register(MaterialRadioButton.class, AppCompatCompoundButtonBinding::new);
            registry.register(ShapeableImageView.class, AppCompatImageViewBinding::new);
            registry.register(SwitchMaterial.class, SwitchCompatBinding::new);
            registry.register(MaterialCardView.class, MaterialCardViewBinding::new);
        });

        tryRegister(() -> registry.register(CardView.class, CardViewBinding::new));

        tryRegister(() -> registry.register(RecyclerView.class, RecyclerViewBinding::new));
        tryRegister(() -> registry.register(ViewPager2.class, ViewPager2Binding::new));

        tryRegister(() -> registry.register(TabLayout.class, TabLayoutBinding::new));
    }

    private static void tryRegister(@NonNull Runnable register) {
        try {
            register.run();
        } catch (NoClassDefFoundError | ExceptionInInitializerError ignored) {
            // 控件 jar 不在运行时 classpath 时跳过。
        }
    }
}
