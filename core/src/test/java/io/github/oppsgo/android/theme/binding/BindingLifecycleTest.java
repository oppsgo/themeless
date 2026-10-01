package io.github.oppsgo.android.theme.binding;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import io.github.oppsgo.android.theme.ResourceBinding;
import io.github.oppsgo.android.theme.ThemeManager;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class BindingLifecycleTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
    }

    @Test
    public void obtain_doesNotAttach() {
        View view = new View(activity);
        ResourceBinding binding = ThemeManager.get().obtain(view);
        assertFalse(binding.isAttached());
        assertNull(ThemeManager.get().find(view));
    }

    @Test
    public void attach_isIdempotent() {
        View view = new View(activity);
        HookBinding binding = new HookBinding(view);
        binding.attach();
        assertEqualsHooks(1, 0, binding);
        binding.attach();
        assertEqualsHooks(1, 0, binding);
        assertTrue(binding.isAttached());
    }

    @Test
    public void attach_replacesExistingViaDetach() {
        View view = new View(activity);
        HookBinding first = new HookBinding(view);
        first.attach();
        assertEqualsHooks(1, 0, first);

        HookBinding second = new HookBinding(view);
        second.attach();

        assertFalse(first.isAttached());
        assertEqualsHooks(1, 1, first);
        assertTrue(second.isAttached());
        assertEqualsHooks(1, 0, second);
        assertSame(second, ThemeManager.get().find(view));
    }

    @Test
    public void detach_clearsTagAndHooks() {
        TrackingListView list = new TrackingListView(activity);
        AbsListViewResourceBinding binding = new AbsListViewResourceBinding(list);
        binding.attach();
        assertSame(binding, list.installed);

        binding.detach();
        assertFalse(binding.isAttached());
        assertNull(ThemeManager.get().find(list));
        assertNull(list.installed);
    }

    @Test
    public void ensureAttach_createsAndAttaches_withoutReplacing() {
        View view = new View(activity);
        ResourceBinding first = ThemeManager.get().ensureAttach(view);
        assertTrue(first.isAttached());

        ResourceBinding again = ThemeManager.get().ensureAttach(view);
        assertSame(first, again);

        HookBinding other = new HookBinding(view);
        ResourceBinding ensured = ThemeManager.get().ensureAttach(view);
        assertSame(first, ensured);
        assertFalse(other.isAttached());
        assertEqualsHooks(0, 0, other);
    }

    @Test
    public void of_withoutAttach_doesNotInstallAbsListHierarchyListener() {
        TrackingListView list = new TrackingListView(activity);
        AbsListViewResourceBinding.of(list);
        AbsListViewResourceBinding.of(list);
        AbsListViewResourceBinding.of(list);
        assertNull(list.installed);
    }

    @Test
    public void inflate_obtainAttachBind_leavesBindingAttached() {
        ThemeManager.get().install(activity);
        TextView textView = new TextView(activity);
        ResourceBinding binding = ThemeManager.get().obtain(textView);
        binding.attach();
        binding.bind(null);
        assertTrue(binding.isAttached());
        assertSame(binding, ThemeManager.get().find(textView));
    }

    private static void assertEqualsHooks(int attached, int detached, HookBinding binding) {
        assertTrue(
                "attached=" + binding.attachedCount + " detached=" + binding.detachedCount,
                binding.attachedCount == attached && binding.detachedCount == detached
        );
    }

    private static final class HookBinding extends ViewResourceBinding {
        int attachedCount;
        int detachedCount;

        HookBinding(View view) {
            super(view);
        }

        @Override
        protected void onAttached() {
            attachedCount++;
        }

        @Override
        protected void onDetached() {
            detachedCount++;
        }
    }

    private static final class TrackingListView extends ListView {
        ViewGroup.OnHierarchyChangeListener installed;

        TrackingListView(Activity activity) {
            super(activity);
        }

        @Override
        public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
            installed = listener;
            super.setOnHierarchyChangeListener(listener);
        }
    }
}
