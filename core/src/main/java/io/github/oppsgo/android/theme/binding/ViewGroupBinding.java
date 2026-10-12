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
public class ViewGroupBinding extends ViewBinding {

    /**
     * 遍历的深度
     */
    private int depth = 1;

    public ViewGroupBinding(@NonNull ViewGroup view) {
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
     * 先写自身属性，再按深度级联子 View；回调由 final 的 {@link #apply} 在级联完成后触发。
     * 被 {@link #setEnabled(boolean)} 关闭时 {@link #apply} 直接早退，不会走到这里。
     */
    @Override
    protected void applyContent(@NonNull ResourceResolver resolver) {
        super.applyContent(resolver);
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
     * 自身有 Binding 则刷自身；仅当自身没有 Binding 时，才对直接子节点里已挂 Binding 的做 {@link IViewBinding#refresh}。
     * 用当前安装的 Resolver；还没 {@link ThemeManager#apply(Context, ResourceResolver)} 时什么都不做。
     * <p>
     * 走 {@link IViewBinding#refresh()} 而非 {@link IViewBinding#apply(ResourceResolver)}：
     * modCount 未变时跳过，避免 RecyclerView 复用等场景每次挂载都全量重写。
     */
    protected void refreshTargetBindings(@Nullable View target, int depth) {
        if (target == null || depth <= 0) return;

        ThemeManager manager = ThemeManager.get();
        if (manager.getResolver(getView().getContext()) == null) return;

        IViewBinding self = manager.find(target);
        if (self != null && target != getView()) {
            self.refresh();
            return;
        }
        if (!(target instanceof ViewGroup)) return;

        ViewGroup group = (ViewGroup) target;
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = group.getChildAt(i);
            IViewBinding binding = manager.find(child);
            if (binding != null) {
                binding.refresh();
            } else {
                refreshTargetBindings(child, depth - 1);
            }
        }
    }
}
