package io.github.oppsgo.android.theme.androidx.binding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ViewGroupResourceBinding;

/**
 * RecyclerView 复用时，离屏缓存的条目不会出现在当前子 View 里；
 * <p>
 * 接入方有 RecyclerView 时，在 inflate 前调用 {@link #register()}。
 */
public class RecyclerViewResourceBinding extends ViewGroupResourceBinding
        implements RecyclerView.OnChildAttachStateChangeListener {

    static final int TRAVERSE_DEPTH_IN_VIEW_PAGER2 = 2;

    public static void register() {
        ThemeManager.get().registry().register(RecyclerView.class, RecyclerViewResourceBinding::new);
    }

    public RecyclerViewResourceBinding(@NonNull RecyclerView view) {
        super(view);
        view.removeOnChildAttachStateChangeListener(this);
        view.addOnChildAttachStateChangeListener(this);
    }

    @NonNull
    @Override
    public RecyclerView getView() {
        return (RecyclerView) view;
    }

    @NonNull
    public static RecyclerViewResourceBinding of(@NonNull RecyclerView view) {
        return of(view, RecyclerViewResourceBinding.class, RecyclerViewResourceBinding::new);
    }

    @Override
    public int getTraverseDepth() {
        return isInsideViewPager2() ? TRAVERSE_DEPTH_IN_VIEW_PAGER2 : super.getTraverseDepth();
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
