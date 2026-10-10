package io.github.oppsgo.android.theme;


import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.util.LongSparseArray;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * 清掉 {@link Resources} 里缓存的 Drawable（反射，对齐 AppCompat 同名工具）。
 * <p>
 * AppCompat 只在对<strong>同一份</strong>宿主 Resources 调用 {@code updateConfiguration}
 * 切换日夜之后、且 API &lt; 26 时 flush，用来修框架缓存未失效的 bug；API 28+ 本类直接 no-op。
 * <p>
 * Themeless 日夜默认走 {@code createConfigurationContext}，每次是新的 Resources，
 * <strong>不会</strong>在 {@link ThemeManager#apply} 里自动 flush。若某处自己改了
 * Configuration / 复用同一 Resources 实例再换肤，再对该实例显式调用 {@link #flush}。
 */
@SuppressLint("PrivateApi")
public class ResourcesFlusher {
    private static final String TAG = "ResourcesFlusher";

    private static Field sDrawableCacheField;
    private static boolean sDrawableCacheFieldFetched;

    private static Class<?> sThemedResourceCacheClazz;
    private static boolean sThemedResourceCacheClazzFetched;

    private static Field sThemedResourceCache_mUnthemedEntriesField;
    private static boolean sThemedResourceCache_mUnthemedEntriesFieldFetched;

    private static Field sResourcesImplField;
    private static boolean sResourcesImplFieldFetched;

    public static void flush(@NonNull final Resources resources) {
        if (Build.VERSION.SDK_INT >= 28) {
            // no-op on P and above
            return;
        }

        if (Build.VERSION.SDK_INT >= 24) {
            flushNougats(resources);
        } else if (Build.VERSION.SDK_INT >= 23) {
            flushMarshmallows(resources);

        } else if (Build.VERSION.SDK_INT >= 21) {
            flushLollipops(resources);
        }
    }


    @RequiresApi(21)
    private static void flushLollipops(@NonNull final Resources resources) {
        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = Resources.class.getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mDrawableCache field", e);
            }
            sDrawableCacheFieldFetched = true;
        }
        if (sDrawableCacheField != null) {
            Map<?, ?> drawableCache = null;
            try {
                drawableCache = (Map<?, ?>) sDrawableCacheField.get(resources);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Could not retrieve value from Resources#mDrawableCache", e);
            }
            if (drawableCache != null) {
                drawableCache.clear();
            }
        }
    }

    @RequiresApi(23)
    private static void flushMarshmallows(@NonNull final Resources resources) {
        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = Resources.class.getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mDrawableCache field", e);
            }
            sDrawableCacheFieldFetched = true;
        }

        Object drawableCache = null;
        if (sDrawableCacheField != null) {
            try {
                drawableCache = sDrawableCacheField.get(resources);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Could not retrieve value from Resources#mDrawableCache", e);
            }
        }

        if (drawableCache == null) {
            // If there is no drawable cache, there's nothing to flush...
            return;
        }

        flushThemedResourcesCache(drawableCache);
    }

    @RequiresApi(24)
    private static void flushNougats(@NonNull final Resources resources) {
        if (!sResourcesImplFieldFetched) {
            try {
                sResourcesImplField = Resources.class.getDeclaredField("mResourcesImpl");
                sResourcesImplField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve Resources#mResourcesImpl field", e);
            }
            sResourcesImplFieldFetched = true;
        }

        if (sResourcesImplField == null) {
            // If the mResourcesImpl field isn't available, bail out now
            return;
        }

        Object resourcesImpl = null;
        try {
            resourcesImpl = sResourcesImplField.get(resources);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Could not retrieve value from Resources#mResourcesImpl", e);
        }

        if (resourcesImpl == null) {
            // If there is no impl instance, bail out now
            return;
        }

        if (!sDrawableCacheFieldFetched) {
            try {
                sDrawableCacheField = resourcesImpl.getClass().getDeclaredField("mDrawableCache");
                sDrawableCacheField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                Log.e(TAG, "Could not retrieve ResourcesImpl#mDrawableCache field", e);
            }
            sDrawableCacheFieldFetched = true;
        }

        Object drawableCache = null;
        if (sDrawableCacheField != null) {
            try {
                drawableCache = sDrawableCacheField.get(resourcesImpl);
            } catch (IllegalAccessException e) {
                Log.e(TAG, "Could not retrieve value from ResourcesImpl#mDrawableCache", e);
            }
        }

        if (drawableCache != null) {
            flushThemedResourcesCache(drawableCache);
        }
    }

    private static void flushThemedResourcesCache(@NonNull final Object cache) {
        if (!sThemedResourceCacheClazzFetched) {
            try {
                sThemedResourceCacheClazz = Class.forName("android.content.res.ThemedResourceCache");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "Could not find ThemedResourceCache class", e);
            }
            sThemedResourceCacheClazzFetched = true;
        }

        if (sThemedResourceCacheClazz == null) {
            // If the ThemedResourceCache class isn't available, bail out now
            return;
        }

        if (!sThemedResourceCache_mUnthemedEntriesFieldFetched) {
            try {
                sThemedResourceCache_mUnthemedEntriesField =
                        sThemedResourceCacheClazz.getDeclaredField("mUnthemedEntries");
                sThemedResourceCache_mUnthemedEntriesField.setAccessible(true);
            } catch (NoSuchFieldException ee) {
                Log.e(TAG, "Could not retrieve ThemedResourceCache#mUnthemedEntries field", ee);
            }
            sThemedResourceCache_mUnthemedEntriesFieldFetched = true;
        }

        if (sThemedResourceCache_mUnthemedEntriesField == null) {
            // Didn't get mUnthemedEntries field, bail out...
            return;
        }

        LongSparseArray<?> unthemedEntries = null;
        try {
            unthemedEntries = (LongSparseArray<?>)
                    sThemedResourceCache_mUnthemedEntriesField.get(cache);
        } catch (IllegalAccessException e) {
            Log.e(TAG, "Could not retrieve value from ThemedResourceCache#mUnthemedEntries", e);
        }

        if (unthemedEntries != null) {
            unthemedEntries.clear();
        }
    }

    private ResourcesFlusher() {
    }
}