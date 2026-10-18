package io.github.oppsgo.android.theme.androidx.binding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.oppsgo.android.theme.binding.ViewGroupBinding;

/**
 * {@link RecyclerView}：item 复用后重新挂上时补刷，避免换肤后滑回来仍是旧色。
 * <p>
 * item 根没有 Binding 时（如 ViewPager2 内部是 FragmentViewHolder 容器）
 * 会自动穿透到 Fragment 根的 Binding，无需按容器层数垫深。
 */
public class RecyclerViewBinding extends ViewGroupBinding
        implements RecyclerView.OnChildAttachStateChangeListener {

    public RecyclerViewBinding(@NonNull RecyclerView view) {
        super(view);
    }

    @NonNull
    @Override
    public RecyclerView getView() {
        return (RecyclerView) view;
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
}
