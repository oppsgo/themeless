package io.github.oppsgo.android.theme.binding;

import android.graphics.Rect;
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

    /** 已有本类或子类就返回；否则给一个不挂到 View 上的实例。 */
    @NonNull
    public static ViewGroupResourceBinding of(@NonNull ViewGroup view) {
        return of(view, ViewGroupResourceBinding.class, ViewGroupResourceBinding::new);
    }

    @NonNull
    @Override
    public ViewGroup getView() {
        return (ViewGroup) view;
    }

    @Override
    protected void invalidate(@NonNull ResourceResolver resolver) {
        super.invalidate(resolver);
        applyBoundChildren(resolver);
    }

    /**
     * 对已挂 Binding 的直接子 View 逐个 {@link ResourceBinding#apply}。
     */
    protected void applyBoundChildren(@NonNull ResourceResolver resolver) {
        ThemeManager manager = ThemeManager.get();
        ViewGroup group = getView();
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            ResourceBinding binding = manager.find(group.getChildAt(i));
            if (binding != null) {
                binding.apply(resolver);
            }
        }
    }

    /**
     * 自身有 Binding 则 apply（含禁用，由 Binding 自己决定）；
     * 仅当自身没有 Binding 时，才下探直接子节点里已挂 Binding 的节点。
     * 等价于 {@link #applySelfOrBoundChildren(View, ResourceResolver, int) depth = 1}。
     */
    protected static void applySelfOrBoundChildren(@Nullable View target, @NonNull ResourceResolver resolver) {
        applySelfOrBoundChildren(target, resolver, 1);
    }

    /**
     * @param depth 还可下探的层数：自身有 Binding 则 apply 并停止；
     *              否则在直接子节点上查找 Binding；子节点无 Binding 时以 {@code depth - 1} 递归。
     *              {@code depth <= 0} 时直接返回。
     */
    protected static void applySelfOrBoundChildren(@Nullable View target, @NonNull ResourceResolver resolver, int depth) {
        if (target == null || depth <= 0) {
            return;
        }
        ThemeManager manager = ThemeManager.get();
        ResourceBinding self = manager.find(target);
        if (self != null) {
            self.apply(resolver);
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
                binding.apply(resolver);
            } else {
                applySelfOrBoundChildren(child, resolver, depth - 1);
            }
        }
    }

    /**
     * 自身有 Binding 则 {@link ResourceBinding#refresh}；
     * 仅当自身没有 Binding 时，才对直接子节点里已挂 Binding 的 {@link ResourceBinding#refresh}。
     * 等价于 {@link #refreshSelfOrBoundChildren(View, int) depth = 1}。
     */
    protected void refreshSelfOrBoundChildren(@Nullable View target) {
        refreshSelfOrBoundChildren(target, 1);
    }

    /**
     * @param depth 还可下探的层数：自身有 Binding 则 refresh 并停止；
     *              否则在直接子节点上查找 Binding；子节点无 Binding 时以 {@code depth - 1} 递归。
     *              {@code depth <= 0} 时直接返回。
     */
    protected void refreshSelfOrBoundChildren(@Nullable View target, int depth) {
        if (target == null || depth <= 0) {
            return;
        }
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
                refreshSelfOrBoundChildren(child, depth - 1);
            }
        }
    }

    /**
     * 遍历 {@code parent} 的直接子 View，只对局部可见的调用
     * {@link #applySelfOrBoundChildren}。
     */
    protected void applyVisibleSelfOrBoundChildren(@NonNull ViewGroup parent, @NonNull ResourceResolver resolver) {
        Rect visible = new Rect();
        int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = parent.getChildAt(i);
            if (child.getLocalVisibleRect(visible)) {
                applySelfOrBoundChildren(child, resolver);
            }
        }
    }
}
