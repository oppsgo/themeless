package io.github.oppsgo.android.theme.androidx.binding;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewGroupResourceBinding;

/**
 * RecyclerView 复用时，离屏缓存的条目不会出现在当前子 View 里。
 * 重新贴上窗口时，用刷新代数判断要不要再刷一次。
 * <p>
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

    /**
     * 已有本类或子类就返回；否则给一个不挂到 View 上的实例。
     */
    @NonNull
    public static RecyclerViewResourceBinding of(@NonNull RecyclerView view) {
        return of(view, RecyclerViewResourceBinding.class, RecyclerViewResourceBinding::new);
    }

    @Override
    public void onChildViewAttachedToWindow(@NonNull View child) {
        ResourceBinding binding = ThemeManager.get().getResourceBinding(child);
        if (binding == null || !binding.isEnable()) return;
        // 优先用列表自己的 Context。取不到再看条目，不能把“没取到”当成代数 0。
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
