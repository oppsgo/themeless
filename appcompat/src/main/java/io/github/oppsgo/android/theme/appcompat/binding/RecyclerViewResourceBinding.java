package io.github.oppsgo.android.theme.appcompat.binding;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import androidx.annotation.NonNull;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewGroupResourceBinding;

/**
 * Support Library（recyclerview-v7）版。
 * 可选能力：接入方有 RecyclerView 时再调用 {@link #register()}。
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
        ResourceBinding binding = ThemeManager.get().getResourceBinding(child);
        if (binding == null || !binding.isEnable()) return;
        Context context = view.getContext();
        int modCount = ThemeManager.get().getModCount(context);
        ResourceResolver resolver = ThemeManager.get().getResolver(context);
        if (modCount == ThemeManager.MOD_COUNT_NONE) {
            Context childContext = child.getContext();
            modCount = ThemeManager.get().getModCount(childContext);
            resolver = ThemeManager.get().getResolver(childContext);
        }
        if (resolver == null) return;
        if (modCount != ThemeManager.MOD_COUNT_NONE && modCount <= binding.getModCount()) return;
        binding.apply(resolver);
    }

    @Override
    public void onChildViewDetachedFromWindow(@NonNull View view) {
    }
}
