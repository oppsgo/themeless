# Themeless

[![](https://jitpack.io/v/oppsgo/themeless.svg)](https://jitpack.io/#oppsgo/themeless)

Android **运行时换肤**库：在不重建 Activity 的前提下，按资源 id 绑定并刷新 View。  
支持普通 `Activity` / `FragmentActivity`，以及 AndroidX / Support 的 AppCompat。

文档暂时只提供中文。

## 特性

- LayoutInflater.Factory2 拦截 inflate，自动记录布局里的资源引用
- `ThemeManager.apply` 切换 `ResourceResolver` 并刷新内容树与已登记浮层
- 日夜 Resolver 基于 Application `createConfigurationContext`，不改写 Activity Configuration
- AndroidX / Support 双栈扩展（Binding + Resolver + Kotlin 扩展）
- Demo：底部 Tab（首页 / 演示 / 设置），皮肤状态本地持久化

## 模块

| 模块 | 说明 |
|------|------|
| `:core` | 核心：`ThemeManager`、`ResourceBinding`、日夜与 Context 系 `ResourceResolver` |
| `:core-ktx` | `:core` 的 Kotlin 扩展（`installTheme` / `edit` / `theme` 等） |
| `:androidx` | AndroidX AppCompat / RecyclerView 扩展绑定与 Resolver |
| `:androidx-ktx` | `:androidx` 的 Kotlin 扩展 |
| `:appcompat` | 旧版 Support Library（`appcompat-v7` / `recyclerview-v7` / `support-v4` ViewPager）扩展 |
| `:appcompat-ktx` | `:appcompat` 的 Kotlin 扩展 |
| `:app` | Demo（不发布） |

接入约定：

- 已迁 AndroidX → `:core` + `:androidx`（可选 ktx）
- 仍用 Support → `:core` + `:appcompat`（可选 ktx）
- ktx / 扩展模块对底层使用 `implementation`，**不会**把 `core` 等传递给消费者，请显式声明
- **不要**在同一个 APK 里同时引入 `:androidx` 与 `:appcompat`（及其 ktx）

## 安装（JitPack）

仓库内版本写在根目录 `build.gradle.kts`：

```kotlin
group = "com.github.oppsgo"
version = "0.2.1-SNAPSHOT"
```

库模块经 convention 插件配置 `maven-publish`；`jitpack.yml` 使用 **JDK 17**。

**重要：别人 `implementation` 的版本 ≠ 仓库里的 `version` 字符串。**

| 用途 | 用什么 |
|------|--------|
| 写入 POM / 本地工程版本 | 根工程 `version`（SemVer，预发布用大写 `-SNAPSHOT`） |
| 别人依赖的版本 | **Git 标签**（如 `0.2.1`）、**`main-SNAPSHOT`**、或 commit |
| JitPack 徽章 | 来自 GitHub **Release / tag**，不会自动扫仓库 `version` |

仓库若没有 Git tag，JitPack 只会按 commit 构建，版本会变成类似 `-8381f653c8-1`。

发布正式版：

1. 把根 `build.gradle.kts` 的 `version` 改成 `0.2.1`（去掉 `-SNAPSHOT`）
2. 打同名 tag：`git tag 0.2.1 && git push --tags`
3. 在 [JitPack](https://jitpack.io/#oppsgo/themeless) 选该 tag 触发构建

```kotlin
maven { url = uri("https://jitpack.io") }

implementation("com.github.oppsgo.themeless:core:0.2.1")
implementation("com.github.oppsgo.themeless:core-ktx:0.2.1")       // 可选
implementation("com.github.oppsgo.themeless:androidx:0.2.1")
implementation("com.github.oppsgo.themeless:androidx-ktx:0.2.1")   // 可选
// Support：
// implementation("com.github.oppsgo.themeless:appcompat:0.2.1")
// implementation("com.github.oppsgo.themeless:appcompat-ktx:0.2.1")
```

开发期未打 tag 时用分支快照（如 `main-SNAPSHOT`）。不要指望别人写 `0.2.1-SNAPSHOT` 就能从 JitPack 拉到（除非打了同名 tag）。

## 快速接入

### 1. 依赖

```kotlin
dependencies {
    implementation(project(":core"))
    implementation(project(":androidx")) // 或 :appcompat
    // implementation(project(":core-ktx"))
    // implementation(project(":androidx-ktx"))
}
```

### 2. 在 `super.onCreate()` 之前 install

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ThemeManager.get().install(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.xxx)
}
```

AppCompat（把 `AppCompatDelegate` 当作 Factory2 传入；控件 Binding 须在 inflate 前注册）：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ThemeManager.get().install(this, delegate as? LayoutInflater.Factory2)
    // 按 classpath 尽力登记（无 ViewPager2 / Material TabLayout 等依赖则自动跳过）
    ThemeAndroidX.registerAvailable()
    super.onCreate(savedInstanceState)
}
```

Support 栈用 `ThemeAppCompat.registerAvailable()`。

若 LayoutInflater 上已有其它 Factory，install 会失败（打 error 日志）且不覆盖。

### 3. 应用主题

```kotlin
ThemeManager.get().apply(this, DayNightResourceResolver.followSystem(this))
ThemeManager.get().apply(this, DayNightResourceResolver.light(this))
ThemeManager.get().apply(this, DayNightResourceResolver.night(this))
// AppCompat：AppCompatDayNightResourceResolver.light / night / followSystem / of
```

布局使用资源引用（如 `@color/skin_page_bg`），inflate 时记入 Binding；`apply` 后按当前 Resolver 写回。  
某个 View 不参与换肤时可写 `app:themeAttributeEnable="false"`（对应 `ResourceBinding.setEnable`；仍会记录属性，只是 `apply` / `refresh` 不写回）。

## 日夜与 Force Dark

`DayNightResourceResolver` 从 **Application** 创建 Configuration，与 Activity / AppCompat DayNight 隔离：

- 强制亮色 → `values/`；强制暗色 → `values-night/`；跟随 → 按 Application uiMode
- **不会**改写 Activity 自身 Configuration

自定义固定色板（如 Demo「晴空蓝」）用亮色底座 Resolver + id 重映射（`MappedResourceResolver`），不要为自定义主题单独建 `values-night`。嵌套 `@color` 不会自动跟映射，需额外映射内层 id。

### 系统夜间下的浅色肤

系统深色时若把 Activity 切成 Light / `MODE_NIGHT_NO`，Android 10+ 的 **Force Dark** 可能在送显前反相浅色像素。正确做法：

1. 主题声明 `android:forceDarkAllowed=false`
2. 展示浅色 / 自定义肤时，Activity **保持夜间宿主**（已是 FOLLOW/YES 则直接 `apply`；仅当 `localNightMode == MODE_NIGHT_NO` 时改成 `MODE_NIGHT_YES`）
3. 肤色只通过 `ThemeManager.apply(…, DayNightResourceResolver.light / Mapped…)` 绘制
4. **不要**为了「看起来亮」去切 `MODE_NIGHT_NO`

## 常用 API

| API | 作用 |
|-----|------|
| `ThemeManager.install` | 挂 Factory2，须在 `super.onCreate` 前 |
| `ThemeManager.apply` | 换 Resolver、递增代数、刷新内容树与已登记浮层 |
| `ThemeManager.setRefreshOnInflate` | 新 inflate 的 View 立刻刷一遍（Dialog / Popup 常用） |
| `ThemeManager.refresh` | 仅刷新，不换 Resolver |
| `ThemeManager.registry` | Binding 注册表 |
| `ThemeManager.edit` / `obtain` / `find` | 取 Binding（临时 / 取或建不挂 / 只查） |
| `ThemeManager.ensureAttach` | 有则复用，无则 create + `attach`（接近旧 obtain） |
| `ResourceBinding.attach` / `detach` | 挂载（可替换旧 Binding）/ 卸载容器钩子与 tag |
| `TextViewResourceBinding.setDefaultTrackTextSize` | **全局**：inflate 是否跟踪 `android:textSize`（默认关；须在 inflate 前设） |
| `TextViewResourceBinding.setTrackTextSize` | **实例**开关 |
| `TextViewResourceBinding.of(view).setTextColor(…)` | 手动绑定并立刻写 View |

### core-ktx

```kotlin
import io.github.oppsgo.android.theme.ktx.*

installTheme()
applyTheme(dayNightNight())
refreshTheme()
registry { register(MyView::class.java, ::MyBinding) }

textView.edit().setTextColor(R.color.skin_text_primary)
textView.theme { setTextColor(R.color.skin_text_primary) }

// 任意已 register 的类型（不必再写扩展）：
view.binding<MyBinding>().setBackground(R.color.skin_page_bg)
view.themeAs<MyBinding> { setBackground(R.color.skin_page_bg) }
```

AndroidX：Binding 登记在 `:androidx`，DayNight 扩展在 `:androidx-ktx`：

```kotlin
import io.github.oppsgo.android.theme.androidx.ThemeAndroidX
import io.github.oppsgo.android.theme.androidx.ktx.*
import io.github.oppsgo.android.theme.ktx.*

ThemeAndroidX.registerAvailable()
applyAppCompatDayNight(dark = true)
appCompatTextView.theme { setTextColor(R.color.skin_text_primary) } // 走 core-ktx TextView 重载
viewPager.themeAs<ViewPagerResourceBinding> { }
viewPager2.themeAs<ViewPager2ResourceBinding> { }
tabLayout.themeAs<TabLayoutResourceBinding> { }
```

Support：`ThemeAppCompat.registerAvailable()` + `:appcompat-ktx` DayNight 扩展（无 `AppCompatToggleButton`）。

## 自定义 Binding

1. 继承对应 `*ResourceBinding`，在 `getViewStyleable()` 声明要跟踪的 attr  
2. `ThemeManager.get().registry().register(YourView.class, YourBinding::new)`  
3. 完全自写、不继承库内 Binding 时，属性列表与开关需自行实现

## AbsListView 特殊情况

`ListView` / `GridView` / `ExpandableListView` 由 `AbsListViewResourceBinding` 处理：换肤后 item 复用滑回来时补刷，避免仍是旧色。

**默认行为**：inflate（`obtain` + `attach`）后占用 `ViewGroup.setOnHierarchyChangeListener`，子 View 挂上时补刷该 child。`ViewGroup` 对该 listener 只有 setter、无 getter，直接对 View 再 `set` 会覆盖 Binding。

**外部也要 hierarchy 回调时**：用 Binding 代理，勿直接对 View 设置；须先 `attach`；子项挂上时**先回调外部，再补刷**：

```kotlin
AbsListViewResourceBinding.of(list)
    .attach()
    .setOnHierarchyChangeListener { parent, child -> /* 你的逻辑 */ }
```

**三方 SDK 已占用该槽位时**：构造传入 `hierarchy = false` 不占用：

```kotlin
AbsListViewResourceBinding(list, false).attach()
// 或注册：registry.register(AbsListView::class.java) { AbsListViewResourceBinding(it, false) }
```

此时优先在 Adapter `getView` 里对 item / convertView 自行 `refresh()`（只碰正在绑定的那一项，最贴复用）。

**可选自动补刷**（默认关）：未走 hierarchy、又希望自动补时再开 `setAutoRefresh(true)`。会在 layout 变化时遍历**当前可见子项**（`getChildCount`，不是 adapter 全量）；`refresh()` 有 modCount 短路，已是本轮主题的会直接跳过。一般不必开。

| 方式 | 适用 |
|------|------|
| hierarchy（默认） | inflate 场景；子项挂上只刷一个 |
| Binding 代理 `setOnHierarchyChangeListener` | 自己也要 hierarchy 回调 |
| `hierarchy = false` + `getView` 里 `refresh` | 三方已占用槽位（推荐） |
| `setAutoRefresh(true)` | 兜底；layout 时刷可见 child |

## Demo

`:app` 结构：

| 页面 | 内容 |
|------|------|
| 首页 | 说明、当前皮肤；入口进入二级演示；**右上角「设置」** |
| 设置 | 皮肤与宿主日夜；**仅此页会持久化并作用于全应用** |
| 二级演示 | 按 Tab 分控件 / 列表（含 ListView·GridView）/ 其它（ProgressBar·SeekBar）/ 浮层；页内切肤**只改当前页** |

冷启动由 `ThemelessApp` 恢复皮肤与宿主日夜。需要 **JDK 17+**：

```bash
./gradlew :app:assembleDebug
```

## License

见仓库根目录 [LICENSE](LICENSE)。
