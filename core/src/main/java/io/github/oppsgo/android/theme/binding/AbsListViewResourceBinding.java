package io.github.oppsgo.android.theme.binding;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;

/**
 * {@link AbsListView}（ListView / GridView / ExpandableListView）：
 * item 复用后重新挂上时补刷，避免换肤后滑回来仍是旧色。
 * <p>
 * 通过 {@link ViewGroup.OnHierarchyChangeListener} 监听子 View 挂上；
 * 若宿主也设置了该 listener，会被本 Binding 覆盖。
 */
public class AbsListViewResourceBinding extends BaseViewGroupResourceBinding
        implements ViewGroup.OnHierarchyChangeListener {

    public AbsListViewResourceBinding(@NonNull AbsListView view) {
        super(view);
        view.setOnHierarchyChangeListener(this);
    }

    @NonNull
    @Override
    public AbsListView getView() {
        return (AbsListView) view;
    }

    @NonNull
    public static AbsListViewResourceBinding of(@NonNull AbsListView view) {
        return of(view, AbsListViewResourceBinding.class, AbsListViewResourceBinding::new);
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        refreshSelfOrBoundChildren(child);
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
    }
}
