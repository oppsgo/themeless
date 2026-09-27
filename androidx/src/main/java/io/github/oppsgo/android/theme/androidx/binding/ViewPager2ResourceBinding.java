package io.github.oppsgo.android.theme.androidx.binding;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.BaseViewGroupResourceBinding;

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
public class ViewPager2ResourceBinding extends BaseViewGroupResourceBinding {

    public static void register() {
        ThemeManager.get().registry().register(ViewPager2.class, ViewPager2ResourceBinding::new);
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
                refreshPageAt(position);
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
        super.invalidate(resolver);
        RecyclerView recyclerView = findRecyclerViewChild();
        if (recyclerView != null) {
            applyVisibleSelfOrBoundChildren(recyclerView, resolver);
        }
    }

    private void refreshPageAt(int position) {
        RecyclerView recyclerView = findRecyclerViewChild();
        if (recyclerView == null) {
            return;
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager == null) {
            return;
        }
        refreshSelfOrBoundChildren(layoutManager.findViewByPosition(position));
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
