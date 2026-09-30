package io.github.oppsgo.android.theme.binding;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;

/**
 * {@link ViewGroup} 绑定：刷新自己之后，把同一套 {@link ResourceResolver} 应用到已绑定的直接子 View。
 * 更深的子树由子 View 自己的 ViewGroup Binding 继续传递。
 * <p>
 * 是否启用由各 Binding 自己的 {@link ResourceBinding#apply} / {@link ResourceBinding#refresh} 判断，
 * 上层只根据「有没有挂 Binding」决定传给谁。
 */
public class ViewGroupResourceBinding extends ViewResourceBinding {

    public ViewGroupResourceBinding(@NonNull ViewGroup view) {
        super(view);
    }

    /**
     * 已有本类或子类就返回；否则给一个不挂到 View 上的实例。
     */
    @NonNull
    public static ViewGroupResourceBinding of(@NonNull ViewGroup view) {
        return of(view, ViewGroupResourceBinding.class, ViewGroupResourceBinding::new);
    }

    /**
     * 遍历的深度
     */
    private int depth = 1;

    public void setTraverseDepth(int depth) {
        this.depth = Math.max(1, depth);
    }

    public int getTraverseDepth() {
        return depth;
    }

    @NonNull
    @Override
    public ViewGroup getView() {
        return (ViewGroup) view;
    }

    @Override
    protected void invalidate(@NonNull ResourceResolver resolver) {
        super.invalidate(resolver);
        applyChildBindings(resolver);
    }

    /**
     * 对已挂 Binding 的直接子 View 逐个 {@link ResourceBinding#apply}。
     */
    protected void applyChildBindings(@NonNull ResourceResolver resolver) {
        applyChildBindings(resolver, getTraverseDepth());
    }

    protected void applyChildBindings(ResourceResolver resolver, int depth) {
        updateChildBindings(resolver, false, depth);
    }


    protected void refreshChildBindings() {
        refreshChildBindings(getTraverseDepth());
    }

    protected void refreshChildBindings(int depth) {
        ResourceResolver resolver = ThemeManager.get().getResolver(view.getContext());
        updateChildBindings(resolver, true, depth);
    }

    /**
     * 按 {@code refresh} 对已挂 Binding 的子 View 做 {@link ResourceBinding#apply}
     * 或 {@link ResourceBinding#refresh}；无 Binding 时下探到该子 ViewGroup，以 {@code depth - 1} 继续。
     */
    protected void updateChildBindings(ResourceResolver resolver, boolean refresh, int depth) {
        if (resolver == null || depth <= 0) return;
        updateChildBindings(getView(), resolver, refresh, depth);
    }

    protected void updateChildBindings(
            @NonNull ViewGroup group,
            ResourceResolver resolver,
            boolean refresh,
            int depth
    ) {
        if (resolver == null || depth <= 0) return;
        ThemeManager manager = ThemeManager.get();
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            ResourceBinding binding = manager.find(child);
            if (binding != null) {
                if (refresh) {
                    binding.refresh();
                } else {
                    binding.apply(resolver);
                }
            } else if (child instanceof ViewGroup) {
                updateChildBindings((ViewGroup) child, resolver, refresh, depth - 1);
            }
        }
    }


    protected void refreshTargetBindings(@Nullable View target) {
        refreshTargetBindings(target, 1);
    }

    /**
     * 自身有 Binding 则 {@link ResourceBinding#refresh}；
     * 仅当自身没有 Binding 时，才对直接子节点里已挂 Binding 的 {@link ResourceBinding#refresh}。
     */
    protected void refreshTargetBindings(@Nullable View target, int depth) {
        if (target == null || target == getView() || depth <= 0) return;

        ThemeManager manager = ThemeManager.get();
        ResourceBinding self = manager.find(target);
        if (self != null) {
            self.refresh();
            return;
        }
        if (!(target instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) target;
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            ResourceBinding binding = manager.find(child);
            if (binding != null) {
                binding.refresh();
            } else {
                refreshTargetBindings(child, depth - 1);
            }
        }
    }
}
