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

import java.util.HashSet;
import java.util.Set;

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

    private final ResourceBindingFactory registry = new ResourceBindingFactory();

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
     * 浮层只刷登记的内容根。更深层由 {@link io.github.oppsgo.android.theme.binding.ViewGroupResourceBinding} 传递。
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
            refreshActivityContent(resolver, content);
        }
        for (View root : installed.copyWindowRoots()) {
            if (!root.isAttachedToWindow()) {
                continue;
            }
            apply(resolver, root);
        }
    }

    /**
     * 只刷指定 View 上的 Binding，不往子树遍历。
     * ViewGroup 若已挂 Binding，子树由其自身的 apply 继续传递。
     * <p>
     * 内部走 {@link ResourceBinding#refresh()}，已是本轮 modCount 则跳过
     * （RecyclerView / ViewPager 复用场景）。
     */
    public void refresh(@Nullable View view) {
        if (view == null) return;
        ResourceBinding binding = registry.find(view);
        if (binding != null) {
            binding.refresh();
        }
    }

    /**
     * {@code android.R.id.content} 是系统 FrameLayout，通常没有 Binding。
     * 只刷它的直接子 View（{@code setContentView} 根），不再往下 DFS。
     */
    private void refreshActivityContent(@NonNull ResourceResolver resolver, @NonNull View content) {
        if (registry.find(content) != null) {
            apply(resolver, content);
            return;
        }
        if (!(content instanceof ViewGroup)) {
            apply(resolver, content);
            return;
        }
        ViewGroup group = (ViewGroup) content;
        int childCount = group.getChildCount();
        for (int i = 0; i < childCount; i++) {
            apply(resolver, group.getChildAt(i));
        }
    }

    private void apply(@NonNull ResourceResolver resolver, @NonNull View view) {
        ResourceBinding binding = registry.find(view);
        if (binding != null) {
            binding.apply(resolver);
        }
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

    /**
     * Binding 注册表（按 View 类型登记 Creator）。
     */
    @NonNull
    public ResourceBindingFactory registry() {
        return registry;
    }

    /**
     * 取 View 上的 Binding：已挂载则复用，否则按注册表临时创建（不挂 tag）。
     * 具体类型的 {@code setXxx} 请用对应 Binding 的 {@code of()}，例如
     * {@code TextViewResourceBinding.of(textView).setTextColor(...)}。
     * <p>
     * 与 {@link #obtain} / {@link #ensureAttach} 的区别：三者都不强制替换已有 Binding；
     * edit / obtain 都不挂载，ensureAttach 会在尚无时 create + {@link ResourceBinding#attach()}。
     */
    @NonNull
    public ResourceBinding edit(@NonNull View view) {
        ResourceBinding existing = registry.find(view);
        if (existing != null) {
            return existing;
        }
        return registry.create(view);
    }

    /**
     * 已挂载则返回；否则按注册表创建（不挂 tag、不装钩子）。
     * inflate 路径为 {@code obtain(view).attach().bind(attrs)}；
     * 需要「有则复用、无则挂上」且不替换时用 {@link #ensureAttach(View)}。
     */
    @NonNull
    public ResourceBinding obtain(@NonNull View view) {
        return registry.obtain(view);
    }

    /**
     * 已挂载则返回已有 Binding（不替换）；否则 create 并 {@link ResourceBinding#attach()}。
     * 语义接近旧版会自动挂 tag 的 obtain。
     */
    @NonNull
    public ResourceBinding ensureAttach(@NonNull View view) {
        ResourceBinding existing = registry.find(view);
        if (existing != null) {
            return existing;
        }
        return registry.create(view).attach();
    }

    /**
     * 只查已挂载的 Binding；没有则 {@code null}。
     */
    @Nullable
    public ResourceBinding find(@Nullable View view) {
        return registry.find(view);
    }

    /**
     * 只查已挂载且类型匹配的 Binding；没有或不匹配则 {@code null}。
     */
    @Nullable
    public <T extends ResourceBinding> T find(@Nullable View view, @NonNull Class<T> clazz) {
        ResourceBinding binding = find(view);
        return clazz.isInstance(binding) ? clazz.cast(binding) : null;
    }
}
