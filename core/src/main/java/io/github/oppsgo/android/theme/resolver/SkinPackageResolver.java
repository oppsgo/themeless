package io.github.oppsgo.android.theme.resolver;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.SparseIntArray;

import androidx.annotation.AnyRes;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.lang.reflect.Method;

import io.github.oppsgo.android.theme.ResourceResolver;
import io.github.oppsgo.android.theme.ThemeViewCompat;
import io.github.oppsgo.android.theme.resource.ResourceRef;

/**
 * 外部皮肤包：宿主资源按「同名同类型」映射到皮肤 APK 里的资源，皮肤包没有的回退 {@code delegate}。
 * <p>
 * 皮肤包是一个只带 res 的 APK，applicationId 即资源包名。从皮肤包 Resources 加载
 * selector / vector 时，其内部引用的 {@code @color/xxx} 也在皮肤包内闭环解析，
 * 不需要像 id 重映射那样逐层嵌套资源。
 * <p>
 * 用 {@link #load} 构造；实例持有皮肤 Resources，换肤时整体替换即可。
 */
public class SkinPackageResolver implements ResourceResolver {

    private final ResourceResolver delegate;
    private final Resources skinResources;
    private final String skinPackage;

    private final String themeId;

    /**
     * 宿主 id → 皮肤 id 的缓存；{@link ResourceRef#ID_NULL} 表示皮肤包没有该资源，回退 delegate。
     */
    private final SparseIntArray idCache = new SparseIntArray();

    public SkinPackageResolver(
            @NonNull ResourceResolver delegate,
            @NonNull Resources skinResources,
            @NonNull String skinPackage
    ) {
        this("", delegate, skinResources, skinPackage);
    }

    public SkinPackageResolver(String themeId, ResourceResolver delegate, Resources skinResources, String skinPackage) {
        this.delegate = delegate;
        this.skinResources = skinResources;
        this.skinPackage = skinPackage;
        this.themeId = themeId;
    }

    /**
     * 读皮肤 APK 建 Resolver；apk 不存在或解析失败返回 {@code null}（由调用方降级/提示）。
     * {@link #getThemeId()} 为空字符串。
     */
    @Nullable
    public static SkinPackageResolver load(
            @NonNull ResourceResolver delegate,
            @NonNull Context context,
            @NonNull String apkPath
    ) {
        return load("", delegate, context, apkPath);
    }

    /**
     * 同 {@link #load(ResourceResolver, Context, String)}，{@code themeId} 写入 {@link #getThemeId()}。
     */
    @Nullable
    public static SkinPackageResolver load(
            @NonNull String themeId,
            @NonNull ResourceResolver delegate,
            @NonNull Context context,
            @NonNull String apkPath
    ) {
        PackageInfo info = getPackageArchiveInfo(context.getPackageManager(), apkPath);
        if (info == null) {
            return null;
        }
        try {
            AssetManager assets = AssetManager.class.getDeclaredConstructor().newInstance();
            // 公开 SDK 里没有 addAssetPath，IDE 解析不了这个反射方法名。
            @SuppressWarnings("JavaReflectionMemberAccess")
            Method addAssetPath = AssetManager.class.getMethod("addAssetPath", String.class);
            Object cookie = addAssetPath.invoke(assets, apkPath);
            // 返回 0 表示路径没进 AssetManager（如 apk 损坏）。此时 skin Resources 照样能建，
            // 但 getIdentifier 全部落空、静默回退宿主主题——按「解析失败」处理。
            if (!(cookie instanceof Integer) || (Integer) cookie == 0) {
                return null;
            }
            Resources skin = createSkinResources(assets, context.getResources());
            return new SkinPackageResolver(themeId, delegate, skin, info.packageName);
        } catch (Exception e) {
            // minSdk 16 没有 ReflectiveOperationException（API 19），只能宽接：反射建 Resources 失败即降级。
            return null;
        }
    }

    /**
     * 独立 Resources 只有这个构造器（{@code ResourcesLoader} 只能往已有实例追加，建不了空实例）；
     * 皮肤 Resources 用宿主的 DisplayMetrics / Configuration，尺寸换算与屏幕一致。
     */
    @SuppressWarnings("deprecation")
    private static Resources createSkinResources(@NonNull AssetManager assets, @NonNull Resources host) {
        return new Resources(assets, host.getDisplayMetrics(), host.getConfiguration());
    }

    private static PackageInfo getPackageArchiveInfo(@NonNull PackageManager pm, @NonNull String apkPath) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return pm.getPackageArchiveInfo(apkPath, PackageManager.PackageInfoFlags.of(0));
        }
        return getPackageArchiveInfoCompat(pm, apkPath);
    }

    private static PackageInfo getPackageArchiveInfoCompat(@NonNull PackageManager pm, @NonNull String apkPath) {
        return pm.getPackageArchiveInfo(apkPath, 0);
    }

    @Override
    public int getColor(@ColorRes int id) {
        int skinId = skinIdOf(id);
        return skinId == ResourceRef.ID_NULL ? delegate.getColor(id) : loadColor(skinResources, skinId);
    }

    @Nullable
    @Override
    public ColorStateList getColorStateList(@ColorRes int id) {
        int skinId = skinIdOf(id);
        return skinId == ResourceRef.ID_NULL ? delegate.getColorStateList(id) : loadColorStateList(skinResources, skinId);
    }

    @Nullable
    @Override
    public Drawable getDrawable(@AnyRes int id) {
        int skinId = skinIdOf(id);
        return skinId == ResourceRef.ID_NULL ? delegate.getDrawable(id) : loadDrawable(skinResources, skinId);
    }

    @Override
    public float getDimension(@DimenRes int id) {
        int skinId = skinIdOf(id);
        return skinId == ResourceRef.ID_NULL ? delegate.getDimension(id) : skinResources.getDimension(skinId);
    }

    @Override
    public int getDimensionPixelSize(@DimenRes int id) {
        int skinId = skinIdOf(id);
        return skinId == ResourceRef.ID_NULL ? delegate.getDimensionPixelSize(id) : skinResources.getDimensionPixelSize(skinId);
    }

    @Override
    public Resources getResources() {
        return delegate.getResources();
    }

    @NonNull
    @Override
    public ThemeViewCompat getViewCompat() {
        return delegate.getViewCompat();
    }

    /**
     * 释放皮肤包资源：清空 id 映射缓存；API 31+ 显式 {@code AssetManager.close()} 归还 native 内存，
     * 低版本只断引用、靠 GC。整体替换皮肤包 Resolver 后，宿主应对旧实例调用，避免多套皮肤 assets 滞留。
     */
    public void close() {
        idCache.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            try {
                skinResources.getAssets().close();
            } catch (Exception ignored) {
                // 关闭失败 / 已关闭：静默，兜底路径本来就是交给 GC。
            }
        }
    }

    /**
     * 宿主 id 查皮肤 id；皮肤包没有返回 {@link ResourceRef#ID_NULL}。
     */
    @SuppressLint("DiscouragedApi") // 按名映射正是本类职责；结果已缓存，refresh 期内不重复查。
    @AnyRes
    private int skinIdOf(@AnyRes int id) {
        int cached = idCache.get(id, -1);
        if (cached != -1) {
            return cached;
        }
        Resources host = delegate.getResources();
        int skinId = skinResources.getIdentifier(
                host.getResourceEntryName(id),
                host.getResourceTypeName(id),
                skinPackage);
        idCache.put(id, skinId);
        return skinId;
    }

    // ---- 皮肤包资源读取（theme 传 null：皮肤值是自包含的） ----

    private static int loadColor(@NonNull Resources res, @ColorRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColor(id, null);
        }
        return getColorCompat(res, id);
    }

    @SuppressWarnings("deprecation")
    private static int getColorCompat(@NonNull Resources res, @ColorRes int id) {
        return res.getColor(id);
    }

    private static ColorStateList loadColorStateList(@NonNull Resources res, @ColorRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return res.getColorStateList(id, null);
        }
        return getColorStateListCompat(res, id);
    }

    @SuppressWarnings("deprecation")
    private static ColorStateList getColorStateListCompat(@NonNull Resources res, @ColorRes int id) {
        return res.getColorStateList(id);
    }

    @Nullable
    private static Drawable loadDrawable(@NonNull Resources res, @AnyRes int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return res.getDrawable(id, null);
        }
        return getDrawableCompat(res, id);
    }

    @SuppressWarnings("deprecation")
    private static Drawable getDrawableCompat(@NonNull Resources res, @AnyRes int id) {
        return res.getDrawable(id);
    }

    @Override
    public String getThemeId() {
        return themeId;
    }
}
