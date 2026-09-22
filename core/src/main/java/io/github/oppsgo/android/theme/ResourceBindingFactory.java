package io.github.oppsgo.android.theme;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.ConcurrentHashMap;

import io.github.oppsgo.android.theme.binding.ImageViewResourceBinding;
import io.github.oppsgo.android.theme.binding.TextViewResourceBinding;
import io.github.oppsgo.android.theme.binding.ViewGroupResourceBinding;
import io.github.oppsgo.android.theme.binding.ViewResourceBinding;
import io.github.oppsgo.theme.core.R;

/**
 * 沿 View 继承链查找并创建最具体的已注册 Binding。
 * Binding 存在 View 的 {@link #TAG_BINDING} 上，同一个 View 只创建一次。
 */
public final class ResourceBindingFactory {

    public static final int TAG_BINDING = R.id.theme_attribute_binding_tag;

    public interface Creator<VIEW extends View> {
        @NonNull
        ResourceBinding create(@NonNull VIEW view);
    }

    private final ConcurrentHashMap<Class<?>, Creator<?>> registry = new ConcurrentHashMap<>();

    ResourceBindingFactory() {
        register(View.class, ViewResourceBinding::new);
        register(ViewGroup.class, ViewGroupResourceBinding::new);
        register(TextView.class, TextViewResourceBinding::new);
        register(ImageView.class, ImageViewResourceBinding::new);
    }

    public <V extends View> void register(@NonNull Class<V> viewType, @NonNull Creator<V> creator) {
        registry.put(viewType, creator);
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public ResourceBinding create(@NonNull View view) {
        Creator<View> creator = (Creator<View>) resolve(view.getClass());
        return creator.create(view);
    }

    @Nullable
    public ResourceBinding maybe(@Nullable View view) {
        Object tag = view == null ? null : view.getTag(TAG_BINDING);
        if (tag instanceof ResourceBinding) {
            return (ResourceBinding) tag;
        }
        return null;
    }

    @NonNull
    public ResourceBinding obtain(@NonNull View view) {
        ResourceBinding existing = maybe(view);
        if (existing != null) {
            return existing;
        }
        ResourceBinding binding = create(view);
        view.setTag(TAG_BINDING, binding);
        return binding;
    }

    /** 从具体类往父类找。{@link View} 的注册不能摘掉，否则没有匹配时无法兜底。 */
    @NonNull
    Creator<?> resolve(@NonNull Class<? extends View> viewClass) {
        for (Class<?> current = viewClass; current != null; current = current.getSuperclass()) {
            Creator<?> creator = registry.get(current);
            if (creator != null) {
                return creator;
            }
            if (current == View.class) {
                break;
            }
        }
        Creator<?> fallback = registry.get(View.class);
        if (fallback == null) {
            throw new IllegalStateException("View.class binding must remain registered.");
        }
        return fallback;
    }
}
