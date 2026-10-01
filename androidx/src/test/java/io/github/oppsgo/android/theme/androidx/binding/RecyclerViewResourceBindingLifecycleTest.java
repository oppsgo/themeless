package io.github.oppsgo.android.theme.androidx.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class RecyclerViewResourceBindingLifecycleTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
        RecyclerViewResourceBinding.register();
    }

    @Test
    public void of_withoutAttach_doesNotAddChildAttachListener() {
        TrackingRecyclerView rv = new TrackingRecyclerView(activity);
        RecyclerViewResourceBinding.of(rv);
        RecyclerViewResourceBinding.of(rv);
        assertEquals(0, rv.addCount);
    }

    @Test
    public void attach_addsListenerOnce_detachRemoves() {
        TrackingRecyclerView rv = new TrackingRecyclerView(activity);
        RecyclerViewResourceBinding binding = RecyclerViewResourceBinding.of(rv);
        binding.attach();
        assertEquals(1, rv.addCount);
        binding.attach();
        assertEquals(1, rv.addCount);

        binding.detach();
        assertEquals(1, rv.removeCount);
        assertTrue(rv.listeners.isEmpty());
    }

    private static final class TrackingRecyclerView extends RecyclerView {
        final List<OnChildAttachStateChangeListener> listeners = new ArrayList<>();
        int addCount;
        int removeCount;

        TrackingRecyclerView(Context context) {
            super(context);
        }

        @Override
        public void addOnChildAttachStateChangeListener(
                @NonNull OnChildAttachStateChangeListener listener
        ) {
            addCount++;
            listeners.add(listener);
            super.addOnChildAttachStateChangeListener(listener);
        }

        @Override
        public void removeOnChildAttachStateChangeListener(
                @NonNull OnChildAttachStateChangeListener listener
        ) {
            if (listeners.remove(listener)) {
                removeCount++;
            }
            super.removeOnChildAttachStateChangeListener(listener);
        }
    }
}
