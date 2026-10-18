package io.github.oppsgo.android.theme;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.github.oppsgo.android.theme.resolver.DayNightResourceResolver;

/**
 * 主题入口，全局一个实例。
 * 每个 Activity 在 {@code super.onCreate()} 之前 {@link #install}。
 * 该 Activity 的 {@link ResourceResolver}、刷新代数挂在它的 {@link ThemeDelegate} 上，不再另建 Session。
 * <p>
 * 仅主线程使用：Binding 表、委托与浮层登记均无并发保护（{@link ViewBinding} 相应字段已按此契约降级），
 * View 树操作本身也只能在主线程。
 */
@MainThread
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
        prepareDelegate(activity, viewFactory);
    }

    public void installDayNight(@NonNull Activity activity) {
        installDayNight(activity, null);
    }

    public void installDayNight(@NonNull Activity activity, boolean night) {
        installDayNight(activity, null, night);
    }

    public void installDayNight(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 viewFactory) {
        boolean night = DayNightResourceResolver.isSystemNight(activity);
        installDayNight(activity, viewFactory, night);
    }


    public void installDayNight(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 viewFactory, boolean night) {
        ThemeDelegate factory = prepareDelegate(activity, viewFactory);
        if (factory != null) {
            factory.resolver = DayNightResourceResolver.of(activity, night);
        }
    }

    /**
     * install / installDayNight 共用：Factory 槽位可用则创建委托并挂上；
     * 已安装或被占用（打 error 日志）时返回 {@code null}。
     */
    @Nullable
    private ThemeDelegate prepareDelegate(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 viewFactory) {
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        if (layoutInflater.getFactory2() instanceof ThemeDelegate) {
            return null;
        }
        if (layoutInflater.getFactory() != null) {
            Log.e(TAG, "LayoutInflater already has a Factory. Call ThemeManager.install() before super.onCreate().");
            return null;
        }
        ThemeDelegate factory = createDelegate(activity, viewFactory);
        layoutInflater.setFactory2(factory);
        return factory;
    }

    @NonNull
    protected ThemeDelegate createDelegate(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 viewFactory) {
        return new ThemeDelegate(activity, viewFactory);
    }

    /**
     * 该 Context（含其包装链）上是否已安装 {@link ThemeDelegate}。
     * {@link #install} 遇到 Factory 槽位被占用时只打 error 日志、不抛异常，可用本方法排查这类静默失败。
     */
    public boolean isInstalled(@Nullable Context context) {
        return findDelegate(context) != null;
    }

    /**
     * 换一套资源实现，递增刷新代数，并立刻刷新该 Activity 的内容树。
     * 必须先 {@link #install}，否则这里直接返回。
     */
    public void apply(Context context, @NonNull ResourceResolver resolver) {
        ThemeDelegate installed = findDelegate(context);
        if (installed == null) return;
        installed.resolver = resolver;
        installed.modCount.incrementAndGet();
        // 委托刚查到，直接复用，不再按 Activity 重复 findDelegate。
        refreshDelegate(installed);
    }

    public void applyDayNight(Context context, boolean night) {
        if (context != null)
            apply(context, DayNightResourceResolver.of(context, night));
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
        return installed == null ? MOD_COUNT_NONE : installed.modCount.get();
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
     * 登记整页主题刷新完成的回调，可登记多个（按对象去重）。
     * 回调挂在该页的 {@link ThemeDelegate} 上，随页面回收；
     * 只有回调实例还要跨页复用时才需要 {@link #removeOnThemeUpdateListener}。
     */
    public void addOnThemeUpdateListener(Context context, @NonNull OnThemeUpdateListener listener) {
        ThemeDelegate installed = findDelegate(context);
        if (installed != null) {
            installed.addOnThemeUpdateListener(listener);
        }
    }

    public void removeOnThemeUpdateListener(@NonNull Context context, @NonNull OnThemeUpdateListener listener) {
        ThemeDelegate installed = findDelegate(context);
        if (installed != null) {
            installed.removeOnThemeUpdateListener(listener);
        }
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
        refreshDelegate(installed);
    }

    /**
     * 刷已定位委托对应的那一页：内容树 + 已登记浮层根 + 整页回调。
     */
    private void refreshDelegate(@NonNull ThemeDelegate installed) {
        Activity activity = installed.activity;
        // isDestroyed() 是 API 17；低版本只靠 isFinishing() 判断。
        if (activity.isFinishing()
                || (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && activity.isDestroyed())) {
            return;
        }
        ResourceResolver resolver = installed.resolver;
        if (resolver == null) return;
        // 必须用 Activity 上记下的 resolver。Dialog / PopupWindow 的 Context
        // 常常找不到 ThemeDelegate，按 View.getContext() 取会直接跳过，浮层就不变色。
        View content = activity.findViewById(Window.ID_ANDROID_CONTENT);
        if (content != null) {
            applyRoot(resolver, content);
        }
        for (View root : installed.copyWindowRoots()) {
            // isAttachedToWindow() 是 API 19；低版本用 getWindowToken() != null 近似判断。
            boolean attached = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    ? root.isAttachedToWindow()
                    : root.getWindowToken() != null;
            if (attached) {
                applyRoot(resolver, root);
            }
        }
        installed.notifyThemeUpdate(resolver);
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
     * {@code ThemeManager.of(textView, TextViewBinding.class).setTextColor(...)}。
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
     * <p>
     * {@code ContextWrapper} 链按构造方式天然无环，深度上限只是防自引用等异常拓扑
     * 的兜底；正常链（Activity / Dialog / {@code ContextThemeWrapper} 包装）远达不到，
     * 因此不再做防环双指针遍历。
     */
    @Nullable
    ThemeDelegate findDelegate(@Nullable Context context) {
        final int maxHops = 64;
        Context current = context;
        for (int hop = 0; current != null && hop < maxHops; hop++) {
            LayoutInflater.Factory2 factory2 = LayoutInflater.from(current).getFactory2();
            if (factory2 instanceof ThemeDelegate) {
                return (ThemeDelegate) factory2;
            }
            if (!(current instanceof ContextWrapper)) {
                return null;
            }
            Context next = ((ContextWrapper) current).getBaseContext();
            if (next == null || next == current) {
                return null;
            }
            current = next;
        }
        return null;
    }
}
