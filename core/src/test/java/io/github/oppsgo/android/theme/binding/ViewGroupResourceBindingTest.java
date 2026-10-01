package io.github.oppsgo.android.theme.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeManager;
import io.github.oppsgo.android.theme.ThemeViewCompat;

/**
 * {@link ViewGroupResourceBinding} 的 self-or-children / depth 递归场景。
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ViewGroupResourceBindingTest {

    private Activity activity;
    private HostBinding host;
    private final ResourceResolver resolver = new StubResolver();

    @Before
    public void setUp() {
        ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();
        activity = controller.get();
        FrameLayout root = new FrameLayout(activity);
        activity.setContentView(root);
        host = new HostBinding(root);
    }

    @Test
    public void refresh_nullTarget_isNoOp() {
        host.refreshSelfOrBoundChildren(null);
        host.refreshSelfOrBoundChildren(null, 2);
    }

    @Test
    public void refresh_depthZero_isNoOpEvenWithBoundSelf() {
        CountingBinding self = attachCounting(new View(activity));
        host.refreshSelfOrBoundChildren(self.getView(), 0);
        assertEquals(0, self.refreshCount);
    }

    @Test
    public void refresh_boundSelf_refreshesSelfAndStops() {
        FrameLayout parent = new FrameLayout(activity);
        CountingBinding self = attachCounting(parent);
        CountingBinding child = attachCounting(new View(activity));
        parent.addView(child.getView());

        host.refreshSelfOrBoundChildren(parent);

        assertEquals(1, self.refreshCount);
        assertEquals(0, child.refreshCount);
    }

    @Test
    public void refresh_depth1_unboundSelf_refreshesDirectBoundChild() {
        FrameLayout parent = new FrameLayout(activity);
        CountingBinding child = attachCounting(new View(activity));
        parent.addView(child.getView());

        host.refreshSelfOrBoundChildren(parent, 1);

        assertEquals(1, child.refreshCount);
    }

    @Test
    public void refresh_depth1_doesNotReachGrandchildBehindUnboundChild() {
        FrameLayout parent = new FrameLayout(activity);
        FrameLayout mid = new FrameLayout(activity);
        CountingBinding leaf = attachCounting(new View(activity));
        mid.addView(leaf.getView());
        parent.addView(mid);

        host.refreshSelfOrBoundChildren(parent, 1);

        assertEquals(0, leaf.refreshCount);
    }

    @Test
    public void refresh_depth2_reachesGrandchildBehindOneUnboundLayer() {
        FrameLayout parent = new FrameLayout(activity);
        FrameLayout mid = new FrameLayout(activity);
        CountingBinding leaf = attachCounting(new View(activity));
        mid.addView(leaf.getView());
        parent.addView(mid);

        host.refreshSelfOrBoundChildren(parent, 2);

        assertEquals(1, leaf.refreshCount);
    }

    @Test
    public void refresh_depth2_doesNotReachBehindTwoUnboundLayers() {
        FrameLayout a = new FrameLayout(activity);
        FrameLayout b = new FrameLayout(activity);
        FrameLayout c = new FrameLayout(activity);
        CountingBinding leaf = attachCounting(new View(activity));
        c.addView(leaf.getView());
        b.addView(c);
        a.addView(b);

        host.refreshSelfOrBoundChildren(a, 2);

        assertEquals(0, leaf.refreshCount);
    }

    @Test
    public void refresh_depth3_reachesBehindTwoUnboundLayers() {
        FrameLayout a = new FrameLayout(activity);
        FrameLayout b = new FrameLayout(activity);
        FrameLayout c = new FrameLayout(activity);
        CountingBinding leaf = attachCounting(new View(activity));
        c.addView(leaf.getView());
        b.addView(c);
        a.addView(b);

        host.refreshSelfOrBoundChildren(a, 3);

        assertEquals(1, leaf.refreshCount);
    }

    @Test
    public void refresh_defaultDepth_equalsDepth1() {
        FrameLayout parent = new FrameLayout(activity);
        FrameLayout mid = new FrameLayout(activity);
        CountingBinding leaf = attachCounting(new View(activity));
        mid.addView(leaf.getView());
        parent.addView(mid);

        host.refreshSelfOrBoundChildren(parent);

        assertEquals(0, leaf.refreshCount);
    }

    @Test
    public void refresh_multipleChildren_onlyBoundOnes() {
        FrameLayout parent = new FrameLayout(activity);
        CountingBinding first = attachCounting(new View(activity));
        View unbound = new View(activity);
        CountingBinding third = attachCounting(new View(activity));
        parent.addView(first.getView());
        parent.addView(unbound);
        parent.addView(third.getView());

        host.refreshSelfOrBoundChildren(parent, 1);

        assertEquals(1, first.refreshCount);
        assertEquals(1, third.refreshCount);
    }

    @Test
    public void refresh_leafWithoutBinding_isNoOp() {
        View leaf = new View(activity);
        host.refreshSelfOrBoundChildren(leaf, 3);
    }

    @Test
    public void apply_boundSelf_appliesSelfAndStops() {
        FrameLayout parent = new FrameLayout(activity);
        CountingBinding self = attachCounting(parent);
        CountingBinding child = attachCounting(new View(activity));
        parent.addView(child.getView());

        HostBinding.applySelfOrBoundChildren(parent, resolver, 1);

        assertEquals(1, self.applyCount);
        assertEquals(0, child.applyCount);
    }

    @Test
    public void apply_depth2_reachesGrandchildBehindUnboundLayer() {
        FrameLayout parent = new FrameLayout(activity);
        FrameLayout mid = new FrameLayout(activity);
        CountingBinding leaf = attachCounting(new View(activity));
        mid.addView(leaf.getView());
        parent.addView(mid);

        HostBinding.applySelfOrBoundChildren(parent, resolver, 2);

        assertEquals(1, leaf.applyCount);
        assertEquals(0, leaf.refreshCount);
    }

    @Test
    public void apply_depth1_doesNotReachGrandchild() {
        FrameLayout parent = new FrameLayout(activity);
        FrameLayout mid = new FrameLayout(activity);
        CountingBinding leaf = attachCounting(new View(activity));
        mid.addView(leaf.getView());
        parent.addView(mid);

        HostBinding.applySelfOrBoundChildren(parent, resolver);

        assertEquals(0, leaf.applyCount);
    }

    @Test
    public void apply_nullOrNonPositiveDepth_isNoOp() {
        CountingBinding self = attachCounting(new View(activity));
        HostBinding.applySelfOrBoundChildren(null, resolver, 2);
        HostBinding.applySelfOrBoundChildren(self.getView(), resolver, 0);
        assertEquals(0, self.applyCount);
    }

    @Test
    public void attach_marksBindingFindable() {
        View view = new View(activity);
        CountingBinding binding = attachCounting(view);
        assertTrue(binding.isAttached());
    }

    @NonNull
    private CountingBinding attachCounting(@NonNull View view) {
        CountingBinding binding = new CountingBinding(view);
        binding.attach();
        return binding;
    }

    /** 暴露 protected 递归 API，供旧测试名调用。 */
    private static final class HostBinding extends ViewGroupResourceBinding {
        HostBinding(@NonNull ViewGroup view) {
            super(view);
        }

        void refreshSelfOrBoundChildren(@Nullable View target) {
            refreshTargetBindings(target);
        }

        void refreshSelfOrBoundChildren(@Nullable View target, int depth) {
            refreshTargetBindings(target, depth);
        }

        static void applySelfOrBoundChildren(@Nullable View target, ResourceResolver resolver) {
            applySelfOrBoundChildren(target, resolver, 1);
        }

        static void applySelfOrBoundChildren(
                @Nullable View target,
                ResourceResolver resolver,
                int depth
        ) {
            if (target == null || resolver == null || depth <= 0) {
                return;
            }
            ResourceBinding self = ThemeManager.get().find(target);
            if (self != null) {
                self.apply(resolver);
                return;
            }
            if (!(target instanceof ViewGroup)) {
                return;
            }
            ViewGroup group = (ViewGroup) target;
            int count = group.getChildCount();
            for (int i = 0; i < count; i++) {
                View child = group.getChildAt(i);
                ResourceBinding binding = ThemeManager.get().find(child);
                if (binding != null) {
                    binding.apply(resolver);
                } else {
                    applySelfOrBoundChildren(child, resolver, depth - 1);
                }
            }
        }
    }

    /** 鍙鏁帮紝涓嶇湡姝ｆ敼 View銆?*/
    private static final class CountingBinding extends ViewResourceBinding {
        int applyCount;
        int refreshCount;

        CountingBinding(@NonNull View view) {
            super(view);
        }

        @Override
        public void apply(@NonNull ResourceResolver resolver) {
            applyCount++;
        }

        @Override
        public void refresh() {
            refreshCount++;
        }
    }

    private static final class StubResolver implements ResourceResolver {
        @Override
        public int getColor(int id) {
            return 0;
        }

        @Nullable
        @Override
        public ColorStateList getColorStateList(int id) {
            return null;
        }

        @Nullable
        @Override
        public Drawable getDrawable(int id) {
            return null;
        }

        @Override
        public float getDimension(int id) {
            return 0f;
        }

        @Override
        public int getDimensionPixelSize(int id) {
            return 0;
        }

        @NonNull
        @Override
        public Resources getResources() {
            throw new UnsupportedOperationException();
        }

        @NonNull
        @Override
        public ThemeViewCompat getViewCompat() {
            throw new UnsupportedOperationException();
        }
    }
}
