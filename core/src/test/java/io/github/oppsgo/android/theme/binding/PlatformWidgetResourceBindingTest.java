package io.github.oppsgo.android.theme.binding;

import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.widget.AbsListView;
import android.widget.AbsSeekBar;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SeekBar;

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
public class PlatformWidgetResourceBindingTest {

    private Activity activity;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
    }

    @Test
    public void obtain_progressBar_createsProgressBarResourceBinding() {
        ProgressBar bar = new ProgressBar(activity);
        ResourceBinding binding = ThemeManager.get().obtain(bar);
        assertTrue(binding instanceof ProgressBarResourceBinding);
    }

    @Test
    public void obtain_seekBar_createsAbsSeekBarResourceBinding() {
        SeekBar bar = new SeekBar(activity);
        ResourceBinding binding = ThemeManager.get().obtain(bar);
        assertTrue(binding instanceof AbsSeekBarResourceBinding);
    }

    @Test
    public void obtain_listView_createsAbsListViewResourceBinding() {
        ListView list = new ListView(activity);
        ResourceBinding binding = ThemeManager.get().obtain(list);
        assertTrue(binding instanceof AbsListViewResourceBinding);
    }

    @Test
    public void obtain_gridView_createsAbsListViewResourceBinding() {
        GridView grid = new GridView(activity);
        ResourceBinding binding = ThemeManager.get().obtain(grid);
        assertTrue(binding instanceof AbsListViewResourceBinding);
    }

    @Test
    public void registry_platformWidgets_areRegistered() {
        assertTrue(ThemeManager.get().registry().isRegistered(AbsListView.class));
        assertTrue(ThemeManager.get().registry().isRegistered(ProgressBar.class));
        assertTrue(ThemeManager.get().registry().isRegistered(AbsSeekBar.class));
    }
}
