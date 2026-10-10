package io.github.oppsgo.android.theme.androidx;

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
 * 应用启动时调用一次 {@link #registerAvailable()}，inflate 出的 AppCompat 控件
 * 就能自动挂上带 {@code app:*} 别名的 Binding。
 */
public final class ThemeAndroidX {

    private ThemeAndroidX() {
    }

    public static void registerAvailable() {
        ThemeBindingFactory registry = ThemeManager.get().registry();

        // TextView 系（含 Button / EditText / Material 文本）
        registry.register(AppCompatTextView.class, AppCompatTextViewThemeBinding::new);
        registry.register(AppCompatButton.class, AppCompatTextViewThemeBinding::new);
        registry.register(AppCompatEditText.class, AppCompatTextViewThemeBinding::new);
        registry.register(AppCompatAutoCompleteTextView.class, AppCompatTextViewThemeBinding::new);
        registry.register(AppCompatMultiAutoCompleteTextView.class, AppCompatTextViewThemeBinding::new);
        registry.register(MaterialTextView.class, AppCompatTextViewThemeBinding::new);
        registry.register(MaterialButton.class, AppCompatTextViewThemeBinding::new);

        // ImageView 系
        registry.register(AppCompatImageView.class, AppCompatImageViewThemeBinding::new);
        registry.register(AppCompatImageButton.class, AppCompatImageViewThemeBinding::new);
        registry.register(ShapeableImageView.class, AppCompatImageViewThemeBinding::new);

        // CompoundButton 系（CheckBox / RadioButton / ToggleButton）
        registry.register(AppCompatCheckBox.class, AppCompatCompoundButtonThemeBinding::new);
        registry.register(AppCompatRadioButton.class, AppCompatCompoundButtonThemeBinding::new);
        registry.register(AppCompatToggleButton.class, AppCompatCompoundButtonThemeBinding::new);
        registry.register(MaterialCheckBox.class, AppCompatCompoundButtonThemeBinding::new);
        registry.register(MaterialRadioButton.class, AppCompatCompoundButtonThemeBinding::new);

        // Switch 系
        registry.register(SwitchCompat.class, SwitchCompatThemeBinding::new);
        registry.register(SwitchMaterial.class, SwitchCompatThemeBinding::new);

        // 容器 / 复杂控件
        registry.register(RecyclerView.class, RecyclerViewThemeBinding::new);
        registry.register(TabLayout.class, TabLayoutThemeBinding::new);
        registry.register(ViewPager2.class, ViewPager2ThemeBinding::new);
    }
}
