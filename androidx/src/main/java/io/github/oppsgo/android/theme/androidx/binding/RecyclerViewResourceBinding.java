package io.github.oppsgo.android.theme.androidx.binding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewGroupResourceBinding;

/**
 * RecyclerView 复用时，离屏缓存的条目不会出现在当前子 View 里。
 * 重新贴上窗口时整棵子树补刷。
 * <p>
 * 接入方有 RecyclerView 时，在 inflate 前调用 {@link #register()}。
 */
public class RecyclerViewResourceBinding extends BaseViewGroupResourceBinding<RecyclerView>
        implements RecyclerView.OnChildAttachStateChangeListener {

    public static void register() {
        ThemeManager.get().bindings().register(RecyclerView.class, RecyclerViewResourceBinding::new);
    }

    public RecyclerViewResourceBinding(@NonNull RecyclerView view) {
        super(view);
        view.removeOnChildAttachStateChangeListener(this);
        view.addOnChildAttachStateChangeListener(this);
    }

    @NonNull
    public static RecyclerViewResourceBinding of(@NonNull RecyclerView view) {
        return of(view, RecyclerViewResourceBinding.class, RecyclerViewResourceBinding::new);
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View child) {
        ThemeManager.get().refresh(child);
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
    }
}
