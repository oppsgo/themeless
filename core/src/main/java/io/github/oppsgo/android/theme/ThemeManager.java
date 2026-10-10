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

    private final ThemeBindingFactory registry = new ThemeBindingFactory();

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

    /**
     * 运行中换主题后，新 inflate 出来的 View 也要马上刷一遍。默认关闭。
     */
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

    /**
     * 刷该 Activity 的内容树，以及已登记的 Dialog / PopupWindow 内容根。
     * <p>
     * Activity 最多落到 {@code setContentView} 那一层（{@code android.R.id.content} 的直接子 View）；
     * 浮层只刷登记的内容根。更深层由 {@link IViewBinding#apply} 沿 Binding 树继续传递。
     */
    public void refresh(@Nullable Context context) {
        ThemeDelegate installed = findDelegate(context);
        if (installed == null) return;
        Activity activity = installed.activity;
        if (activity.isFinishing() || activity.isDestroyed()) return;
        ResourceResolver resolver = installed.resolver;
        if (resolver == null) return;
        // 必须用 Activity 上记下的 resolver。Dialog / PopupWindow 的 Context
        // 常常找不到 ThemeDelegate，按 View.getContext() 取会直接跳过，浮层就不变色。
        View content = activity.findViewById(Window.ID_ANDROID_CONTENT);
        if (content != null) {
            applyRoot(resolver, content);
        }
        for (View root : installed.copyWindowRoots()) {
            if (root.isAttachedToWindow()) {
                applyRoot(resolver, root);
            }
        }
    }

    /**
     * 只刷指定根：根自己有 Binding 就刷它，否则刷直接子 View 里已挂 Binding 的。
     * 更深层由各 Binding 的 {@link IViewBinding#apply} 继续传递。
     */
    public void refresh(@Nullable View root) {
        if (root == null) return;
        ResourceResolver resolver = getResolver(root.getContext());
        if (resolver != null) {
            applyRoot(resolver, root);
        }
    }

    private void applyRoot(@NonNull ResourceResolver resolver, @NonNull View root) {
        IViewBinding binding = registry.find(root);
        if (binding != null) {
            binding.apply(resolver);
            return;
        }
        if (root instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) root;
            int childCount = group.getChildCount();
            for (int i = 0; i < childCount; i++) {
                IViewBinding child = registry.find(group.getChildAt(i));
                if (child != null) {
                    child.apply(resolver);
                }
            }
        }
    }

    /**
     * Binding 注册表（按 View 类型登记 Creator）。
     */
    @NonNull
    public ThemeBindingFactory registry() {
        return registry;
    }

    /**
     * 取 View 上的 Binding：已挂载则复用，否则按注册表创建并挂到 tag 上。
     * 具体类型请用 {@link #of(View, Class)}，例如
     * {@code ThemeManager.of(textView, TextViewThemeBinding.class).setTextColor(...)}。
     */
    @NonNull
    public IViewBinding of(@NonNull View view) {
        IViewBinding existing = registry.find(view);
        if (existing != null) {
            return existing;
        }
        IViewBinding created = registry.create(view);
        registry.attach(view, created);
        return created;
    }

    /**
     * 同 {@link #of(View)}，并断言结果为 {@code type} 或其子类；不是则抛 {@link IllegalStateException}。
     */
    @NonNull
    public <T extends IViewBinding> T of(@NonNull View view, @NonNull Class<T> type) {
        IViewBinding binding = of(view);
        if (!type.isInstance(binding)) {
            throw new IllegalStateException("Expected " + type.getName() + " but was " + binding.getClass().getName());
        }
        return type.cast(binding);
    }

    /**
     * 只查已挂载的 Binding；没有则 {@code null}。
     */
    @Nullable
    public IViewBinding find(@Nullable View view) {
        return registry.find(view);
    }

    /**
     * 只查已挂载且类型匹配的 Binding；没有或不匹配则 {@code null}。
     */
    @Nullable
    public <T extends IViewBinding> T find(@Nullable View view, @NonNull Class<T> type) {
        IViewBinding binding = find(view);
        return type.isInstance(binding) ? type.cast(binding) : null;
    }

    /**
     * 卸下 View 上的 Binding（含容器钩子）。挂载过返回 {@code true}。
     */
    public boolean detach(@NonNull View view) {
        return registry.detach(view);
    }

    /**
     * Dialog、{@code ContextThemeWrapper} 上的 LayoutInflater 不一定带 Factory。
     * 沿 {@code baseContext} 找到 Activity 上那一个。
     */
    @Nullable
    ThemeDelegate findDelegate(@Nullable Context context) {
        Context slow = context;
        Context fast = context;
        // 防环检查
        while (slow != null) {
            LayoutInflater.Factory2 factory2 = LayoutInflater.from(slow).getFactory2();
            if (factory2 instanceof ThemeDelegate) {
                return (ThemeDelegate) factory2;
            }
            if (!(slow instanceof ContextWrapper)) {
                return null;
            }
            Context next = ((ContextWrapper) slow).getBaseContext();
            if (next == null || next == slow) {
                return null;
            }
            slow = next;

            // fast 每次走两步；追上 slow 说明有环
            for (int step = 0; step < 2 && fast != null; step++) {
                if (!(fast instanceof ContextWrapper)) {
                    fast = null;
                    break;
                }
                Context fNext = ((ContextWrapper) fast).getBaseContext();
                if (fNext == null || fNext == fast) {
                    fast = null;
                    break;
                }
                fast = fNext;
            }
            if (fast != null && fast == slow) {
                return null;
            }
        }
        return null;
    }
}
