package io.github.oppsgo.android.theme.androidx.binding;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ViewGroupResourceBinding;

/**
 * {@link ViewPager}：page 多为直接子 View（或 Fragment 根）；decor 除外。
 * <p>
 * 子页数量受 {@code offscreenPageLimit} 约束（通常很少），换肤时刷<strong>全部已挂载</strong>
 * 非 decor 子页，避免 {@code getLocalVisibleRect} 在平滑切页 / 左右 peek 时漏刷一侧。
 * 滑页结束（{@link ViewPager#SCROLL_STATE_IDLE}）再补刷，覆盖
 * {@code onPageSelected} 在动画开始时就回调、此时目标页尚未可见的情况。
 * <p>
 * inflate 前调用 {@link #register()}。页变化监听在 {@link #attach()} 后安装。
 */
public class ViewPagerResourceBinding extends ViewGroupResourceBinding {

    private final ViewPager.OnPageChangeListener pageChangeListener =
            new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    refreshChildBindings();
                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state == ViewPager.SCROLL_STATE_IDLE) {
                        refreshChildBindings();
                    }
                }
            };

    public static void register() {
        ThemeManager.get().registry().register(ViewPager.class, ViewPagerResourceBinding::new);
    }

    public ViewPagerResourceBinding(@NonNull ViewPager view) {
        super(view);
    }

    @NonNull
    @Override
    public ViewPager getView() {
        return (ViewPager) view;
    }

    @NonNull
    public static ViewPagerResourceBinding of(@NonNull ViewPager view) {
        return of(view, ViewPagerResourceBinding.class, ViewPagerResourceBinding::new);
    }

    @Override
    protected void onAttached() {
        ViewPager pager = getView();
        pager.removeOnPageChangeListener(pageChangeListener);
        pager.addOnPageChangeListener(pageChangeListener);
    }

    @Override
    protected void onDetached() {
        getView().removeOnPageChangeListener(pageChangeListener);
    }

    private boolean isDecor(@NonNull View child) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        return lp instanceof ViewPager.LayoutParams && ((ViewPager.LayoutParams) lp).isDecor;
    }
}
