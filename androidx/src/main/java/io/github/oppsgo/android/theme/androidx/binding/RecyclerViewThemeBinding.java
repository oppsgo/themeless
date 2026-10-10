package io.github.oppsgo.android.theme.androidx.binding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import io.github.oppsgo.android.theme.binding.ViewGroupThemeBinding;

/**
 * {@link RecyclerView}：item 复用后重新挂上时补刷，避免换肤后滑回来仍是旧色。
 * <p>
 * 普通 RecyclerView 用默认遍历深度（item 根的直接子 View）；
 * 作为 ViewPager2 内部 RecyclerView 时，子 View 是 FragmentViewHolder 容器，
 * 需再下探一层到 Fragment 根。
 */
public class RecyclerViewThemeBinding extends ViewGroupThemeBinding
        implements RecyclerView.OnChildAttachStateChangeListener {

    /** 在 ViewPager2 内部时：RecyclerView → FragmentViewHolder 容器 → Fragment 根。 */
    static final int TRAVERSE_DEPTH_IN_VIEW_PAGER2 = 2;

    public RecyclerViewThemeBinding(@NonNull RecyclerView view) {
        super(view);
    }

    @NonNull
    @Override
    public RecyclerView getView() {
        return (RecyclerView) view;
    }

    @Override
    public int getTraverseDepth() {
        return isInsideViewPager2() ? TRAVERSE_DEPTH_IN_VIEW_PAGER2 : super.getTraverseDepth();
    }

    @Override
    public void onAttached() {
        super.onAttached();
        RecyclerView rv = getView();
        rv.removeOnChildAttachStateChangeListener(this);
        rv.addOnChildAttachStateChangeListener(this);
    }

    @Override
    public void onDetached() {
        getView().removeOnChildAttachStateChangeListener(this);
        super.onDetached();
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View child) {
        refreshTargetBindings(child, getTraverseDepth());
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
    }

    /** 是否作为 {@link ViewPager2} 内部的 RecyclerView。 */
    private boolean isInsideViewPager2() {
        try {
            return getView().getParent() instanceof ViewPager2;
        } catch (Throwable e) {
            return false;
        }
    }
}
