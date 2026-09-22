package io.github.oppsgo.android.theme;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 主题入口，全局一个实例。
 * 每个 Activity 在 {@code super.onCreate()} 之前 {@link #install}。
 * 该 Activity 的 {@link ResourceResolver}、刷新代数挂在它的 {@link ThemeDelegate} 上，不再另建 Session。
 */
public class ThemeManager {

    private static final String TAG = "ThemeManager";

    /**
     * 这个 Context 上没有已安装的 {@link ThemeDelegate}。
     * 不能用 {@code 0}：{@code 0} 是合法代数，表示装过但还没 {@link #apply}。
     */
    public static final int MOD_COUNT_NONE = -1;

    private static final ThemeManager INSTANCE = new ThemeManager();

    private final ResourceBindingFactory bindings = new ResourceBindingFactory();

    protected ThemeManager() {
    }

    @NonNull
    public static ThemeManager get() {
        return INSTANCE;
    }

    /**
     * 必须在 {@code super.onCreate()} 之前调用，才能挂上 LayoutInflater.Factory2。
     */
    public void install(@NonNull Activity activity) {
        install(activity, null);
    }

    /**
     * @param viewFactory 创建 View 时优先使用。为 null，或没有创建出 View 时，走默认的系统 View。
     *                    AppCompatActivity 多写这一步即可：把它的 {@code AppCompatDelegate} 传进来，实现类本身就是 Factory2。
     */
    public void install(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 viewFactory) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        if (layoutInflater.getFactory2() instanceof ThemeDelegate) {
            return;
        }
        if (layoutInflater.getFactory() != null) {
            Log.e(TAG, "LayoutInflater already has a Factory. Call ThemeManager.install() before super.onCreate().");
            return;
        }
        layoutInflater.setFactory2(createDelegate(activity, viewFactory));
    }

    @NonNull
    protected ThemeDelegate createDelegate(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 viewFactory) {
        return new ThemeDelegate(activity, viewFactory);
    }

    /**
     * 换一套资源实现，递增刷新代数，并立刻刷新该 Activity 的内容树。
     * 必须先 {@link #install}，否则这里直接返回。
     */
    public void apply(@NonNull Context context, @NonNull ResourceResolver resolver) {
        ThemeDelegate installed = findDelegate(context);
        if (installed == null) return;
        installed.resolver = resolver;
        installed.modCount++;
        refresh(installed.activity);
    }

    @Nullable
    public ResourceResolver getResolver(@Nullable Context context) {
        ThemeDelegate installed = findDelegate(context);
        return installed == null ? null : installed.resolver;
    }

    /**
     * @return 刷新代数；找不到已安装的 Factory 时返回 {@link #MOD_COUNT_NONE}，不要把它当成 {@code 0}。
     */
    public int getModCount(@Nullable Context context) {
        ThemeDelegate installed = findDelegate(context);
        return installed == null ? MOD_COUNT_NONE : installed.modCount;
    }

    /** 运行中换主题后，新 inflate 出来的 View 也要马上刷一遍。默认关闭。 */
    public void setRefreshOnInflate(@NonNull Context context, boolean refresh) {
        ThemeDelegate installed = findDelegate(context);
        if (installed != null) {
            installed.refreshOnInflate = refresh;
        }
    }

    public boolean shouldRefreshOnInflate(@Nullable Context context) {
        ThemeDelegate installed = findDelegate(context);
        return installed != null && installed.refreshOnInflate;
    }

    public void refresh(@Nullable Context context) {
        ThemeDelegate installed = findDelegate(context);
        if (installed == null) return;
        Activity activity = installed.activity;
        if (activity.isFinishing() || activity.isDestroyed()) return;
        ResourceResolver resolver = installed.resolver;
        if (resolver == null) return;
        // 必须用 Activity 上记下的 resolver。Dialog / PopupWindow 的 DecorView Context
        // 常常找不到 ThemeDelegate，按 View.getContext() 取会直接跳过，浮层就不变色。
        View content = activity.findViewById(Window.ID_ANDROID_CONTENT);
        if (content != null) {
            refresh(resolver, content);
        }
        View activityDecor = activity.getWindow() == null ? null : activity.getWindow().peekDecorView();
        for (View root : installed.copyWindowRoots()) {
            if (!root.isAttachedToWindow() || root == activityDecor) {
                continue;
            }
            refresh(resolver, root);
        }
    }

    public void refresh(@Nullable View view) {
        if (view == null) return;
        ResourceResolver resolver = getResolver(view.getContext());
        if (resolver == null) {
            // 浮层根 View 的 Context 可能不是 Activity 链，回退到已安装的 delegate。
            ThemeDelegate installed = findDelegate(view.getContext());
            if (installed == null) {
                Activity host = findActivity(view.getContext());
                if (host != null) {
                    installed = findDelegate(host);
                }
            }
            resolver = installed == null ? null : installed.resolver;
        }
        if (resolver == null) return;
        refresh(resolver, view);
    }

    @Nullable
    private static Activity findActivity(@Nullable Context context) {
        Context current = context;
        while (current instanceof ContextWrapper) {
            if (current instanceof Activity) {
                return (Activity) current;
            }
            Context base = ((ContextWrapper) current).getBaseContext();
            if (base == null || base == current) {
                return null;
            }
            current = base;
        }
        return null;
    }

    /**
     * ViewGroup 自己有 Binding 时只刷它，子树由 {@link io.github.oppsgo.android.theme.binding.BaseViewGroupResourceBinding} 继续传递。
     * 没有 Binding 才往下找，避免同一棵子树刷两次。
     */
    private void refresh(@NonNull ResourceResolver resolver, @NonNull View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            if (bindings.maybe(group) != null) {
                apply(resolver, group);
                return;
            }
            int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                refresh(resolver, group.getChildAt(i));
            }
            return;
        }
        apply(resolver, view);
    }

    private void apply(@NonNull ResourceResolver resolver, @NonNull View view) {
        ResourceBinding binding = bindings.maybe(view);
        if (binding != null && binding.isEnable()) {
            binding.apply(resolver);
        }
    }

    /**
     * Dialog、{@code ContextThemeWrapper} 上的 LayoutInflater 不一定带 Factory。
     * 沿 {@code baseContext} 找到 Activity 上那一个。
     */
    @Nullable
    ThemeDelegate findDelegate(@Nullable Context context) {
        Context current = context;
        while (current != null) {
            LayoutInflater.Factory2 factory2 = LayoutInflater.from(current).getFactory2();
            if (factory2 instanceof ThemeDelegate) {
                return (ThemeDelegate) factory2;
            }
            if (!(current instanceof ContextWrapper)) {
                return null;
            }
            Context base = ((ContextWrapper) current).getBaseContext();
            if (base == null || base == current) {
                return null;
            }
            current = base;
        }
        return null;
    }

    @NonNull
    public ResourceBindingFactory bindings() {
        return bindings;
    }

    @NonNull
    public ResourceBinding obtainBinding(@NonNull View view) {
        return bindings.obtain(view);
    }

    @Nullable
    public ResourceBinding getResourceBinding(@Nullable View view) {
        return bindings.maybe(view);
    }

    @Nullable
    public <T extends ResourceBinding> T getResourceBinding(@Nullable View view, @NonNull Class<T> clazz) {
        ResourceBinding binding = getResourceBinding(view);
        return clazz.isInstance(binding) ? clazz.cast(binding) : null;
    }
}
