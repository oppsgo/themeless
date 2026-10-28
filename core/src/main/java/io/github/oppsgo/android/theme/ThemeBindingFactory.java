package io.github.oppsgo.android.theme;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsSeekBar;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

import io.github.oppsgo.android.theme.binding.AbsListViewBinding;
import io.github.oppsgo.android.theme.binding.AbsSeekBarBinding;
import io.github.oppsgo.android.theme.binding.CompoundButtonBinding;
import io.github.oppsgo.android.theme.binding.ImageViewBinding;
import io.github.oppsgo.android.theme.binding.ProgressBarBinding;
import io.github.oppsgo.android.theme.binding.SwitchBinding;
import io.github.oppsgo.android.theme.binding.TextViewBinding;
import io.github.oppsgo.android.theme.binding.ViewBinding;
import io.github.oppsgo.android.theme.binding.ViewGroupBinding;

/**
 * 沿 View 继承链查找并创建最具体的已注册 Binding。
 * Binding 存在 View 的 {@link #TAG_BINDING} tag 上，同一个 View 只创建一次。
 */
public final class ThemeBindingFactory {

    private static final int TAG_BINDING = R2.id.theme_view_attr_binding_tag;

    private final ConcurrentHashMap<Class<? extends View>, Creator<? extends View>> registry = new ConcurrentHashMap<>();

    /**
     * {@link #resolve} 沿继承链解析的结果缓存；{@link #register} 改动注册表时整体失效。
     */
    private final ConcurrentHashMap<Class<? extends View>, Creator<? extends View>> resolved = new ConcurrentHashMap<>();

    public interface Creator<VIEW extends View> {
        @NonNull
        IViewBinding create(@NonNull VIEW view);
    }

    ThemeBindingFactory() {
        register(View.class, ViewBinding::new);
        register(ViewGroup.class, ViewGroupBinding::new);
        register(TextView.class, TextViewBinding::new);
        register(ImageView.class, ImageViewBinding::new);
        register(CompoundButton.class, CompoundButtonBinding::new);
        register(Switch.class, SwitchBinding::new);
        register(ProgressBar.class, ProgressBarBinding::new);
        register(AbsSeekBar.class, AbsSeekBarBinding::new);
        register(AbsListView.class, AbsListViewBinding::new);
    }

    public <V extends View> void register(@NonNull Class<V> viewType, @NonNull Creator<V> creator) {
        registry.put(viewType, creator);
        resolved.clear();
    }

    /**
     * 是否为该 View 类型登记过 Creator（不含沿继承链向上解析）。
     */
    public boolean isRegistered(@NonNull Class<? extends View> viewType) {
        return registry.containsKey(viewType);
    }

    /**
     * 返回该类型上直接注册的 Creator；未注册则 {@code null}。
     * 需要沿继承链匹配时用 {@link #resolve(Class)}。
     */
    @Nullable
    @SuppressWarnings("unchecked")
    public <V extends View> Creator<V> getCreator(@NonNull Class<V> viewType) {
        return (Creator<V>) registry.get(viewType);
    }

    /**
     * 从具体类往父类找，结果按类型记忆化（{@link #register} 时失效重建）。
     * {@link View} 的注册不能摘掉，否则没有匹配时无法兜底。
     */
    @NonNull
    public Creator<?> resolve(@NonNull Class<? extends View> viewClass) {
        Creator<? extends View> cached = resolved.get(viewClass);
        if (cached != null) {
            return cached;
        }
        Creator<? extends View> creator = null;
        for (Class<?> current = viewClass; current != null; current = current.getSuperclass()) {
            creator = registry.get(current);
            if (creator != null || current == View.class) {
                break;
            }
        }
        if (creator == null) {
            throw new IllegalStateException("View.class binding must remain registered.");
        }
        resolved.put(viewClass, creator);
        return creator;
    }

    /** 按注册表创建 Binding（不挂 tag）。 */
    @NonNull
    IViewBinding create(@NonNull View view) {
        @SuppressWarnings("unchecked")
        Creator<View> creator = (Creator<View>) resolve(view.getClass());
        return creator.create(view);
    }

    /** 只查已挂载的 Binding；没有则 {@code null}。 */
    @Nullable
    IViewBinding find(@Nullable View view) {
        Object tag = view == null ? null : view.getTag(TAG_BINDING);
        return tag instanceof IViewBinding ? (IViewBinding) tag : null;
    }

    /**
     * 把 Binding 挂到 View tag（参与 {@link #find} / 换肤级联）。
     * 不触发 {@link ViewBinding#onAttached()}；钩子由 {@link IViewBinding#bind(android.util.AttributeSet)} 安装。
     */
    void putTag(@NonNull View view, @NonNull IViewBinding binding) {
        view.setTag(TAG_BINDING, binding);
    }

    /**
     * 调用 {@link ViewBinding#onDetached()}（若有）并清 tag。未挂载返回 {@code false}。
     */
    boolean detach(@NonNull View view) {
        IViewBinding binding = find(view);
        if (binding == null) {
            return false;
        }
        if (binding instanceof ViewBinding) {
            ((ViewBinding) binding).onDetached();
        }
        view.setTag(TAG_BINDING, null);
        return true;
    }
}
