package io.github.oppsgo.android.theme.binding;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * {@link AbsListView}（ListView / GridView / ExpandableListView）：
 * item 复用后重新挂上时补刷，避免换肤后滑回来仍是旧色。
 * <p>
 * 默认通过 {@link ViewGroup#setOnHierarchyChangeListener} 监听子 View 挂上（inflate 场景）。
 * 外部若也需要该回调，请用 {@link #setOnHierarchyChangeListener} 代理，勿直接对 View 设置。
 * <p>
 * 构造时传入 {@code false} 可跳过占用该槽位；需要自动补刷时可再
 * {@link #setAutoRefresh(boolean)}（layout 变化时遍历当前子项，可覆盖 adapter 增删引起的重排）。
 * 多数情况在 Adapter {@code getView} 里自行 {@code refresh} 即可，不必开自动补刷。
 */
public class AbsListViewResourceBinding extends ViewGroupResourceBinding
        implements ViewGroup.OnHierarchyChangeListener, View.OnLayoutChangeListener {

    @Nullable
    private ViewGroup.OnHierarchyChangeListener onHierarchyChangeListener;

    private boolean autoRefresh;

    public AbsListViewResourceBinding(@NonNull AbsListView view) {
        this(view, true);
    }

    /**
     * @param hierarchy {@code true}（默认）时占用并监听 hierarchy 变化；
     *                  {@code false} 不占用，由外部自行处理或配合 {@link #setAutoRefresh}。
     */
    public AbsListViewResourceBinding(@NonNull AbsListView view, boolean hierarchy) {
        super(view);
        if (hierarchy) {
            view.setOnHierarchyChangeListener(this);
        }
    }

    @NonNull
    public static AbsListViewResourceBinding of(@NonNull AbsListView view) {
        return of(view, AbsListViewResourceBinding.class, AbsListViewResourceBinding::new);
    }

    @NonNull
    @Override
    public AbsListView getView() {
        return (AbsListView) view;
    }

    /**
     * 代理 {@link ViewGroup#setOnHierarchyChangeListener}：本 Binding 占用 View 槽位并转发。
     * 子项挂上时先回调外部，再补刷（外部可能要先做其它操作）。
     */
    public AbsListViewResourceBinding setOnHierarchyChangeListener(@Nullable ViewGroup.OnHierarchyChangeListener listener) {
        this.onHierarchyChangeListener = listener;
        getView().setOnHierarchyChangeListener(this);
        return this;
    }

    /**
     * 可选兜底：layout 变化时对当前子项补刷。默认关闭。
     * 未占用 hierarchy listener、又希望自动补刷时再打开；否则优先在 Adapter {@code getView} 里自行 refresh。
     */
    @NonNull
    public AbsListViewResourceBinding setAutoRefresh(boolean autoRefresh) {
        if (this.autoRefresh == autoRefresh) {
            return this;
        }
        this.autoRefresh = autoRefresh;
        AbsListView list = getView();
        list.removeOnLayoutChangeListener(this);
        if (autoRefresh) {
            list.addOnLayoutChangeListener(this);
        }
        return this;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    @Override
    public void onChildViewAdded(View parent, View child) {
        ViewGroup.OnHierarchyChangeListener listener = onHierarchyChangeListener;
        if (listener != null) {
            listener.onChildViewAdded(parent, child);
        }
        refreshTargetBindings(child);
    }

    @Override
    public void onChildViewRemoved(View parent, View child) {
        ViewGroup.OnHierarchyChangeListener listener = onHierarchyChangeListener;
        if (listener != null) {
            listener.onChildViewRemoved(parent, child);
        }
    }

    @Override
    public void onLayoutChange(
            View v,
            int left,
            int top,
            int right,
            int bottom,
            int oldLeft,
            int oldTop,
            int oldRight,
            int oldBottom
    ) {
        if (!autoRefresh) {
            return;
        }
        AbsListView list = getView();
        int count = list.getChildCount();
        for (int i = 0; i < count; i++) {
            refreshTargetBindings(list.getChildAt(i));
        }
    }
}
