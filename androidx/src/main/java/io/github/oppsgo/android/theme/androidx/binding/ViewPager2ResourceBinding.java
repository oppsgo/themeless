package io.github.oppsgo.android.theme.androidx.binding;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ViewGroupResourceBinding;

/**
 * {@link ViewPager2}：内部 RecyclerView 与 {@code FragmentViewHolder} 容器通常无 Binding，
 * Fragment 根在 page 容器下一层。
 * <p>
 * 通过 {@link #setTraverseDepth} 直接下探到 Fragment 根换肤 / 滑页补刷，
 * 无需再挂内部 RecyclerView 的 Binding。inflate 前调用 {@link #register()}。
 * 页变化回调在 {@link #attach()} 后注册。
 */
public class ViewPager2ResourceBinding extends ViewGroupResourceBinding {
    /**
     * ViewPager2 → 内部 RecyclerView → FragmentViewHolder 容器 → Fragment 根，共 3 层。
     */
    static final int TRAVERSE_DEPTH = 3;

    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    refreshChildBindings();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        refreshChildBindings();
                    }
                }
            };

    public static void register() {
        ThemeManager.get().registry().register(ViewPager2.class, ViewPager2ResourceBinding::new);
    }

    @NonNull
    public static ViewPager2ResourceBinding of(@NonNull ViewPager2 view) {
        return of(view, ViewPager2ResourceBinding.class, ViewPager2ResourceBinding::new);
    }

    public ViewPager2ResourceBinding(@NonNull ViewPager2 view) {
        super(view);
        setTraverseDepth(TRAVERSE_DEPTH);
    }

    @NonNull
    @Override
    public ViewPager2 getView() {
        return (ViewPager2) view;
    }

    @Override
    protected void onAttached() {
        ViewPager2 pager = getView();
        pager.unregisterOnPageChangeCallback(pageChangeCallback);
        pager.registerOnPageChangeCallback(pageChangeCallback);
    }

    @Override
    protected void onDetached() {
        getView().unregisterOnPageChangeCallback(pageChangeCallback);
    }
}
