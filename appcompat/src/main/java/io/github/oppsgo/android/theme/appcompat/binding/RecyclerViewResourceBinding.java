package io.github.oppsgo.android.theme.appcompat.binding;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewGroupResourceBinding;

/**
 * Support Library（recyclerview-v7）版。
 * 接入方有 RecyclerView 时，在 inflate 前调用 {@link #register()}。
 */
public class RecyclerViewResourceBinding extends BaseViewGroupResourceBinding<RecyclerView>
        implements RecyclerView.OnChildAttachStateChangeListener {

    public static void register() {
        ThemeManager.get().registry().register(RecyclerView.class, RecyclerViewResourceBinding::new);
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
