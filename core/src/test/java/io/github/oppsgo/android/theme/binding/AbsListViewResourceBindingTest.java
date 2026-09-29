package io.github.oppsgo.android.theme.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;
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

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class AbsListViewResourceBindingTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
    }

    @Test
    public void defaultConstructor_setsHierarchyListener() {
        TrackingListView list = new TrackingListView(activity);
        AbsListViewResourceBinding binding = new AbsListViewResourceBinding(list);
        assertEquals(binding, list.installed);
    }

    @Test
    public void constructorFalse_doesNotSetHierarchyListener() {
        TrackingListView list = new TrackingListView(activity);
        new AbsListViewResourceBinding(list, false);
        assertEquals(null, list.installed);
    }

    @Test
    public void setOnHierarchyChangeListener_forwardsBeforeRefresh() {
        ListView list = new ListView(activity);
        AbsListViewResourceBinding binding = new AbsListViewResourceBinding(list);
        AtomicInteger order = new AtomicInteger();
        AtomicInteger externalAt = new AtomicInteger();
        AtomicInteger refreshAt = new AtomicInteger();

        TextView child = new TextView(activity);
        CountingBinding childBinding = new CountingBinding(child);
        childBinding.attach();
        childBinding.onRefresh = () -> refreshAt.set(order.incrementAndGet());

        binding.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener() {
            @Override
            public void onChildViewAdded(View parent, View c) {
                externalAt.set(order.incrementAndGet());
            }

            @Override
            public void onChildViewRemoved(View parent, View c) {
            }
        });

        binding.onChildViewAdded(list, child);

        assertEquals(1, externalAt.get());
        assertEquals(2, refreshAt.get());
        assertEquals(1, childBinding.refreshCount);
    }

    @Test
    public void setAutoRefresh_defaultsOff() {
        ListView list = new ListView(activity);
        AbsListViewResourceBinding binding = new AbsListViewResourceBinding(list, false);
        assertFalse(binding.isAutoRefresh());
        binding.setAutoRefresh(true);
        assertTrue(binding.isAutoRefresh());
    }

    private static final class TrackingListView extends ListView {
        ViewGroup.OnHierarchyChangeListener installed;

        TrackingListView(Context context) {
            super(context);
        }

        @Override
        public void setOnHierarchyChangeListener(OnHierarchyChangeListener listener) {
            installed = listener;
            super.setOnHierarchyChangeListener(listener);
        }
    }

    private static final class CountingBinding extends ViewResourceBinding {
        int refreshCount;
        Runnable onRefresh;

        CountingBinding(View view) {
            super(view);
        }

        @Override
        public void refresh() {
            refreshCount++;
            if (onRefresh != null) {
                onRefresh.run();
            }
        }
    }
}
