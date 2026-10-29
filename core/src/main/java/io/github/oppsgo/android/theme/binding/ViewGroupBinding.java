package io.github.oppsgo.android.theme.binding;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.IViewBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;

/**
 * {@link ViewGroup} 绑定：自身刷完后按深度级联子 View，并提供指定节点的补刷入口。
 * <p>
 * 下探时没有挂 Binding 的中间 ViewGroup 自动穿透，直到各分支遇到首个已挂 Binding 的子树
 * （由它的 {@link #applyDescendants} 接管自身子树），或 {@link IViewBinding#isEnabled()} 为 false
 * 的 Binding（{@link #apply} 早退，整枝停止不刷）。
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
     * {@link ViewGroup} 默认级联：深度 &gt; 0 时对自身做 {@link #applyChildTree}。
     * 深度为 0 时只刷过自身（{@link #applySelf} 已完成），不再往下。
     */
    @Override
    protected void applyDescendants(@NonNull ResourceResolver resolver) {
        int depth = getTraverseDepth();
        if (depth > 0) {
            applyChildTree(getView(), resolver, depth);
        }
    }

    /**
     * {@code getChildAt} 下探实现：已挂 Binding 的直接 {@link IViewBinding#apply}；
     * 没有 Binding 的 ViewGroup 继续下探，每下一层减 1，到 0 停止。
     * {@link #TRAVERSE_INFINITE} 自然不会触底。
     * 遇到 Binding 后由它自己的 {@link #applyDescendants} 接管子树；
     * 遇到 enabled=false 的 Binding 整枝停止。
     */
    protected void applyChildTree(
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
                applyChildTree((ViewGroup) child, resolver, depth - 1);
            }
        }
    }

    /**
     * 补刷指定节点：自己有 Binding 则 {@link IViewBinding#refresh()}，否则按深度下探子树。
     * 挂载 / 复用回调用这条路，不要走 {@link #apply}（避免 modCount 未变时也全量重写）。
     */
    protected void refreshTarget(@Nullable View target) {
        refreshTarget(target, getTraverseDepth());
    }

    /**
     * 深度 {@link #TRAVERSE_INFINITE} 无限穿透；{@code 0} 只刷自身；正整数限制中间 ViewGroup 穿透层数。
     * 用当前安装的 Resolver；还没 {@link ThemeManager#apply(Context, ResourceResolver)} 时什么都不做。
     */
    protected void refreshTarget(@Nullable View target, int depth) {
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
                refreshTarget(child, depth - 1);
            }
        }
    }
}
