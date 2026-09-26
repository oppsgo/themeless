package io.github.oppsgo.android.theme.binding;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;

/**
 * 刷新自己之后，把同一套 {@link ResourceResolver} 应用到已绑定的直接子 View。
 * 更深的子树由子 View 自己的 ViewGroup Binding 继续传递。
 */
public class BaseViewGroupResourceBinding extends ViewResourceBinding {

    public BaseViewGroupResourceBinding(@NonNull ViewGroup view) {
        super(view);
    }

    @NonNull
    @Override
    public ViewGroup getView() {
        return (ViewGroup) view;
    }

    @Override
    protected void invalidate(@NonNull ResourceResolver resolver) {
        super.invalidate(resolver);

        ThemeManager manager = ThemeManager.get();
        ViewGroup group = getView();
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            ResourceBinding binding = manager.find(child);
            if (binding != null && binding.isEnable()) {
                binding.apply(resolver);
            }
        }
    }
}
