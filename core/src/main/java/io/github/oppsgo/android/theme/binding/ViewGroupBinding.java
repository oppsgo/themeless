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
 * {@link ViewGroup} 绑定：自身刷新后级联子 View，并提供浮层补刷入口。
 * <p>
 * 下探时没有挂 Binding 的中间 ViewGroup 自动穿透，直到各分支遇到首个已挂 Binding 的子树
 * （由它的 {@link #applyContent} 接管自身子树），或 {@link IViewBinding#isEnabled()} 为 false
 * 的 Binding（apply 早退，整枝停止不刷）。
 * <p>
 * {@link #setTraverseDepth(int)} 控制下探层数：{@link #TRAVERSE_INFINITE} 无限穿透（默认），
 * {@code 0} 只刷自身，正整数为可穿透的中间 ViewGroup 层数上限。
 */
public class ViewGroupBinding extends ViewBinding {

    /**
     * 无限穿透：取一个远大于实际视图层级的上限值（Android 视图树极少超过 30 层），
     * 递归时逐层递减，正常情况下不会触底。
     */
    public static final int TRAVERSE_INFINITE = 4096;

    /**
     * 子树下探深度：{@link #TRAVERSE_INFINITE} 无限穿透（默认）；{@code 0} 只刷自身；
     * 正整数限制中间 ViewGroup 穿透层数。
     */
    private int depth = TRAVERSE_INFINITE;

    public ViewGroupBinding(@NonNull ViewGroup view) {
        super(view);
    }

    /**
     * @param depth {@link #TRAVERSE_INFINITE} 或负数：无限穿透；0：只刷自身；
     *              正整数：限制可穿透的中间 ViewGroup 层数。
     */
    public void setTraverseDepth(int depth) {
        this.depth = depth < 0 ? TRAVERSE_INFINITE : depth;
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
     * 遍历深度为 0 时只刷自身，不级联子 View。
     */
    @Override
    protected void applyContent(@NonNull ResourceResolver resolver) {
        super.applyContent(resolver);
        int depth = getTraverseDepth();
        if (depth > 0) {
            applyChildBindings(resolver, depth);
        }
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
     * 级联子 View：已挂 Binding 的直接 apply；没有 Binding 的 ViewGroup 按深度下探，
     * 每下一层减 1，到 0 不再处理该层子 View；{@link #TRAVERSE_INFINITE} 自然不会触底。
     * 遇到 Binding 后由它自身级联接管子树；遇到 enabled=false 的 Binding 整枝停止。
     */
    protected void updateChildBindings(
            @NonNull ViewGroup group,
            @NonNull ResourceResolver resolver,
            int depth
    ) {
        if (depth <= 0) return; // 深度耗尽，该层子 View 不再处理
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
        refreshTargetBindings(target, getTraverseDepth());
    }

    /**
     * 自身有 Binding 则刷自身；没有时对子树里已挂 Binding 的做 {@link IViewBinding#refresh}。
     * 深度 {@link #TRAVERSE_INFINITE} 无限穿透；{@code 0} 只刷自身；正整数限制中间 ViewGroup 穿透层数。
     * 用当前安装的 Resolver；还没 {@link ThemeManager#apply(Context, ResourceResolver)} 时什么都不做。
     * <p>
     * 走 {@link IViewBinding#refresh()} 而非 {@link IViewBinding#apply(ResourceResolver)}：
     * modCount 未变时跳过，避免 RecyclerView 复用等场景每次挂载都全量重写。
     */
    protected void refreshTargetBindings(@Nullable View target, int depth) {
        if (target == null) return;

        ThemeManager manager = ThemeManager.get();
        if (manager.getResolver(getView().getContext()) == null) return;

        IViewBinding self = manager.find(target);
        if (self != null && target != getView()) {
            self.refresh();
            return;
        }
        // depth <= 0：只刷自身（上面已处理），不下探
        if (depth <= 0 || !(target instanceof ViewGroup)) return;

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
