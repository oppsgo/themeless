package io.github.oppsgo.android.theme.binding;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.IViewBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;

/**
 * {@link ViewGroup} 绑定：基础传播在 {@link ViewBinding#applyChildren}
 * （把 resolver 传给已绑定的直接子 View），本类在其上加下探深度控制，并提供浮层补刷入口。
 * <p>
 * 是否启用由各 Binding 自己的 {@link IViewBinding#apply} 判断，
 * 上层只根据「有没有挂 Binding」决定传给谁。
 */
public class ViewGroupThemeBinding extends ViewBinding {

    /**
     * 遍历的深度
     */
    private int depth = 1;

    public ViewGroupThemeBinding(@NonNull ViewGroup view) {
        super(view);
    }

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

    /**
     * 深度受限的传播：已绑子 View 直接刷；未绑的 ViewGroup 以 {@code depth - 1} 下探。
     */
    @Override
    protected void applyChildren(@NonNull ResourceResolver resolver) {
        applyChildBindings(resolver, getTraverseDepth());
    }

    /**
     * 对已挂 Binding 的子树按当前深度做 {@link IViewBinding#apply}。
     */
    protected void applyChildBindings(@NonNull ResourceResolver resolver) {
        applyChildBindings(resolver, getTraverseDepth());
    }

    protected void applyChildBindings(@NonNull ResourceResolver resolver, int depth) {
        updateChildBindings(getView(), resolver, depth);
    }

    /**
     * 按 {@code depth} 对已挂 Binding 的子 View 做 {@link IViewBinding#apply}。
     */
    protected void updateChildBindings(
            @NonNull ViewGroup group,
            @NonNull ResourceResolver resolver,
            int depth
    ) {
        if (depth <= 0) return;
        ThemeManager manager = ThemeManager.get();
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            IViewBinding binding = manager.find(child);
            if (binding != null) {
                binding.apply(resolver);
            } else if (child instanceof ViewGroup) {
                updateChildBindings((ViewGroup) child, resolver, depth - 1);
            }
        }
    }

    protected void refreshTargetBindings(@Nullable View target) {
        refreshTargetBindings(target, 1);
    }

    /**
     * 自身有 Binding 则刷自身；仅当自身没有 Binding 时，才对直接子节点里已挂 Binding 的做 {@link IViewBinding#apply}。
     * 用当前安装的 Resolver；还没 {@link ThemeManager#apply(Context, ResourceResolver)} 时什么都不做。
     */
    protected void refreshTargetBindings(@Nullable View target, int depth) {
        if (target == null || depth <= 0) return;

        ThemeManager manager = ThemeManager.get();
        ResourceResolver resolver = manager.getResolver(getView().getContext());
        if (resolver == null) return;

        IViewBinding self = manager.find(target);
        if (self != null && target != getView()) {
            self.apply(resolver);
            return;
        }
        if (!(target instanceof ViewGroup)) return;

        ViewGroup group = (ViewGroup) target;
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            IViewBinding binding = manager.find(child);
            if (binding != null) {
                binding.apply(resolver);
            } else {
                refreshTargetBindings(child, depth - 1);
            }
        }
    }
}
