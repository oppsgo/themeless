package io.github.oppsgo.android.theme.androidx.binding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.binding.ViewResourceBinding;
import io.github.oppsgo.android.theme.resolver.ContextResourceResolver;

/**
 * ViewPager：换肤应覆盖已挂载的左右邻页（含 banner peek），不依赖 LocalVisibleRect。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ViewPagerResourceBindingTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
        ViewPagerResourceBinding.register();
        ViewPager2ResourceBinding.register();
    }

    @Test
    public void register_viewPager_isRegistered() {
        assertTrue(ThemeManager.get().registry().isRegistered(ViewPager.class));
    }

    @Test
    public void obtain_viewPager_createsViewPagerResourceBinding() {
        ViewPager pager = new ViewPager(activity);
        ResourceBinding binding = ThemeManager.get().obtain(pager);
        assertTrue(binding instanceof ViewPagerResourceBinding);
    }

    @Test
    public void isDecor_falseForNormalPageLayoutParams() {
        ViewPager pager = new ViewPager(activity);
        View page = new View(activity);
        pager.addView(page, new ViewPager.LayoutParams());
        assertFalse(isDecor(page));
    }

    @Test
    public void apply_refreshesLeftAndRightAttachedPages() {
        ViewPager pager = new ViewPager(activity);
        activity.setContentView(pager);
        ThemeManager.get().obtain(pager).attach();

        CountingPage left = new CountingPage(activity);
        CountingPage center = new CountingPage(activity);
        CountingPage right = new CountingPage(activity);
        left.attach();
        center.attach();
        right.attach();

        // 直接挂三个子页，模拟 offscreen 邻页都在树上（含 banner peek）
        pager.addView(left.getView());
        pager.addView(center.getView());
        pager.addView(right.getView());

        ResourceResolver resolver = new ContextResourceResolver(activity);
        ThemeManager.get().obtain(pager).apply(resolver);

        assertTrue(left.applyCount >= 1);
        assertTrue(center.applyCount >= 1);
        assertTrue(right.applyCount >= 1);
    }

    @Test
    public void register_viewPager2_stillWorks() {
        assertTrue(ThemeManager.get().registry().isRegistered(androidx.viewpager2.widget.ViewPager2.class));
    }

    private static final class CountingPage extends ViewResourceBinding {
        int applyCount;

        CountingPage(Activity activity) {
            super(new FrameLayout(activity));
        }

        @Override
        public void apply(ResourceResolver resolver) {
            applyCount++;
        }
    }

    private boolean isDecor(@NonNull View child) {
        ViewGroup.LayoutParams lp = child.getLayoutParams();
        return lp instanceof ViewPager.LayoutParams && ((ViewPager.LayoutParams) lp).isDecor;
    }
}
