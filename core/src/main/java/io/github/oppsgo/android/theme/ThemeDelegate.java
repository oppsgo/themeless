package io.github.oppsgo.android.theme;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * 挂到一个 Activity 上的主题委托，对应 {@link androidx.appcompat.app.AppCompatDelegate} 的角色。
 * 既创建 View，也保管这一页的 {@link ResourceResolver} 和刷新代数，不另建表。
 * 实现 {@link LayoutInflater.Factory2} 是为了挂进 LayoutInflater，方便按 Context 找回这一份。
 */
public class ThemeDelegate implements LayoutInflater.Factory2 {

    private static final String[] VIEW_PREFIXES = {
            "android.widget.",
            "android.view.",
            "android.webkit."
    };

    @NonNull
    final Activity activity;

    /**
     * 优先用来创建 View。AppCompat 传入的是 {@code AppCompatDelegate}，它的实现类本身就是 Factory2。
     * 不叫 delegate，避免和本类 {@link ThemeDelegate} 撞名。
     */
    @Nullable
    private final LayoutInflater.Factory2 viewFactory;

    @Nullable
    ResourceResolver resolver;

    volatile int modCount;

    /**
     * 创建完 View 是否需要立即刷新。默认不需要，运行中换主题时需要。
     */
    boolean refreshOnInflate;

    /**
     * Dialog、PopupWindow 等独立窗口的根（DecorView / PopupDecorView）。
     * 只记窗口根，子 View 由刷新自己往下走。按对象身份去重。
     */
    private final Set<View> windowRoots = Collections.newSetFromMap(new IdentityHashMap<>());

    private final View.OnAttachStateChangeListener windowWatcher = new View.OnAttachStateChangeListener() {
        @Override
        public void onViewAttachedToWindow(@NonNull View v) {
            trackWindowRoot(v.getRootView());
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull View v) {
            View root = v.getRootView();
            windowRoots.remove(root);
        }
    };

    public ThemeDelegate(@NonNull Activity activity) {
        this(activity, null);
    }

    public ThemeDelegate(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 viewFactory) {
        this.activity = activity;
        this.viewFactory = viewFactory;
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = null;
        // 先交给外部 Factory2（例如 AppCompatDelegate）。它没创建出来再走系统 View。
        if (viewFactory != null) {
            view = viewFactory.onCreateView(parent, name, context, attrs);
        }
        if (view == null) {
            view = tryCreateView(LayoutInflater.from(context), name, attrs);
        }
        attachViewBind(view, attrs);
        if (parent == null) {
            watchForeignWindow(view);
        }
        return view;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    protected void attachViewBind(@Nullable View view, @Nullable AttributeSet attrs) {
        if (view == null) return;
        ResourceBinding<?> binding = ThemeManager.get().obtain(view);
        binding.bind(attrs);
        if (refreshOnInflate) {
            binding.refresh();
        }
    }

    @NonNull
    List<View> copyWindowRoots() {
        return new ArrayList<>(windowRoots);
    }

    /**
     * 只有 {@code inflate(..., null)} 的 XML 根会走到这里（Dialog.setView、PopupWindow 等）。
     * 这时还没进窗口，不能用 {@link View#getRootView()} 判断属于哪一扇窗；挂上之后再记窗口根。
     */
    private void watchForeignWindow(@Nullable View view) {
        if (view == null) return;
        view.removeOnAttachStateChangeListener(windowWatcher);
        view.addOnAttachStateChangeListener(windowWatcher);
    }

    private void trackWindowRoot(@Nullable View root) {
        if (root == null || isActivityDecor(root)) {
            return;
        }
        windowRoots.add(root);
    }

    private boolean isActivityDecor(@Nullable View view) {
        View decor = peekActivityDecor();
        return decor != null && view == decor;
    }

    @Nullable
    private View peekActivityDecor() {
        Window window = activity.getWindow();
        return window == null ? null : window.peekDecorView();
    }

    @Nullable
    protected View tryCreateView(@NonNull LayoutInflater inflater, @NonNull String name, @NonNull AttributeSet attrs) {
        try {
            if (name.indexOf('.') == -1) {
                for (String prefix : VIEW_PREFIXES) {
                    try {
                        return inflater.createView(name, prefix, attrs);
                    } catch (ClassNotFoundException ignored) {
                        // 继续尝试下一个系统 View 前缀。
                    }
                }
                return null;
            }
            return inflater.createView(name, null, attrs);
        } catch (ClassNotFoundException ignored) {
            return null;
        }
    }
}
