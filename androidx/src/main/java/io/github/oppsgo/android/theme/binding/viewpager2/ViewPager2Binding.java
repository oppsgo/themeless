package io.github.oppsgo.android.theme.binding.viewpager2;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.binding.ViewGroupBinding;

/**
 * {@link ViewPager2}：内部 RecyclerView 与 FragmentViewHolder 容器通常无 Binding，
 * 下探时自动穿透，到 Fragment 根的 Binding 为止。
 * <p>
 * 路径：ViewPager2 → 内部 RecyclerView → FragmentViewHolder 容器 → Fragment 根。
 * 滑页时通过 {@link ViewPager2.OnPageChangeCallback} 补刷。
 */
public class ViewPager2Binding extends ViewGroupBinding {

    private final ViewPager2.OnPageChangeCallback pageChangeCallback =
            new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    applyCurrent();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        applyCurrent();
                    }
                }
            };

    public ViewPager2Binding(@NonNull ViewPager2 view) {
        super(view);
    }

    @NonNull
    @Override
    public ViewPager2 getView() {
        return (ViewPager2) view;
    }

    @Override
    public void onAttached() {
        super.onAttached();
        ViewPager2 pager = getView();
        pager.unregisterOnPageChangeCallback(pageChangeCallback);
        pager.registerOnPageChangeCallback(pageChangeCallback);
    }

    @Override
    public void onDetached() {
        getView().unregisterOnPageChangeCallback(pageChangeCallback);
        super.onDetached();
    }

    /** 用当前 Resolver 刷一遍子树（切页 / 滑动停止时补刷）。 */
    private void applyCurrent() {
        ResourceResolver resolver = currentResolver();
        if (resolver != null) {
            applyDescendants(resolver);
        }
    }
}
