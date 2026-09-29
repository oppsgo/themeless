package io.github.oppsgo.android.theme.androidx.binding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import io.github.oppsgo.android.theme.ResourceBindingFactory;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ViewGroupResourceBinding;

/**
 * {@link ViewPager2}：内部 RV 与 {@code FragmentViewHolder} 的 FrameLayout 无 Binding，
 * Fragment 根在 page 容器下一层。
 * <p>
 * 换肤时对内部 RV 刷局部可见 page；滑页时按 {@code position} 用
 * {@link RecyclerView.LayoutManager#findViewByPosition} 定点补刷（不依赖
 * {@code getLocalVisibleRect}，因此不会出现 ViewPager 那种左右漏刷）。
 * 复用挂上时由 {@link RecyclerViewResourceBinding} 的 attach 监听再补刷。
 * inflate 前调用 {@link #register()}。
 */
public class ViewPager2ResourceBinding extends ViewGroupResourceBinding {

    public static void register() {
        ResourceBindingFactory registry = ThemeManager.get().registry();
        if (!registry.isRegistered(RecyclerView.class)) {
            RecyclerViewResourceBinding.register();
        }
        registry.register(ViewPager2.class, ViewPager2ResourceBinding::new);
    }

    public ViewPager2ResourceBinding(@NonNull ViewPager2 view) {
        super(view);
        RecyclerView recyclerView = findRecyclerViewChild();
        if (recyclerView != null) {
            ThemeManager.get().obtain(recyclerView).attach();
        }
        view.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                ResourceResolver resolver = ThemeManager.get().getResolver(view.getContext());
                refreshInternal(resolver, true);
            }
        });
    }

    @NonNull
    @Override
    public ViewPager2 getView() {
        return (ViewPager2) view;
    }

    @NonNull
    public static ViewPager2ResourceBinding of(@NonNull ViewPager2 view) {
        return of(view, ViewPager2ResourceBinding.class, ViewPager2ResourceBinding::new);
    }

    @Override
    protected void invalidate(@NonNull ResourceResolver resolver) {
        refreshInternal(resolver, false);
    }

    private void refreshInternal(ResourceResolver resolver, boolean refresh) {
        RecyclerView recyclerView = findRecyclerViewChild();
        if (recyclerView == null || resolver == null) return;

        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) manager;
            int start = layoutManager.findFirstVisibleItemPosition();
            int end = layoutManager.findLastVisibleItemPosition();
            // 前后多刷新一个，防止滑动过程中展示的还是原来的颜色
            int min = Math.max(0, start - 1);
            int max = Math.min(layoutManager.getChildCount() - 1, end + 1);

            for (int i = min; i <= max; i++) {
                View child = manager.getChildAt(i);
                if (refresh) {
                    refreshSelfOrBoundChildren(child);
                } else {
                    applySelfOrBoundChildren(child, resolver);
                }
            }
        } else {
            if (refresh) {
                refreshSelfOrBoundChildren(recyclerView, 2);
            } else {
                applySelfOrBoundChildren(recyclerView, resolver, 2);
            }
        }
    }

    @Nullable
    private RecyclerView findRecyclerViewChild() {
        ViewPager2 pager = getView();
        int count = pager.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = pager.getChildAt(i);
            if (child instanceof RecyclerView) {
                return (RecyclerView) child;
            }
        }
        return null;
    }
}
