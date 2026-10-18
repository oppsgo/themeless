# Themeless

[![](https://jitpack.io/v/oppsgo/themeless.svg)](https://jitpack.io/#oppsgo/themeless)

Android **运行时换肤**库：在不重建 Activity 的前提下，按资源 id 绑定并刷新 View。  
支持普通 `Activity` / `FragmentActivity`，以及 AndroidX 的 AppCompat / Material。

文档暂时只提供中文。

## 特性

- LayoutInflater.Factory2 拦截 inflate，自动记录布局里的资源引用
- `ThemeManager.apply` 切换 `ResourceResolver` 并刷新内容树与已登记浮层
- 日夜 Resolver 基于 Application `createConfigurationContext`，不改写 Activity Configuration
- AndroidX 栈扩展（Binding + Resolver）：AppCompat / RecyclerView / ViewPager2 / Material 组件
- 外部皮肤包 `SkinPackageResolver`：宿主资源按「同名同类型」映射到皮肤 APK
- Activity / View 两级主题更新回调（`OnThemeUpdateListener` / `OnViewThemeUpdateListener`）
- Demo：底部 Tab（首页 / 演示 / 设置），皮肤状态本地持久化

## 模块

| 模块 | 说明 |
|------|------|
| `:core` | 核心：`ThemeManager`、`IViewBinding` / `*Binding`、日夜 / Context / 皮肤包 `ResourceResolver` |
| `:androidx` | AndroidX AppCompat / RecyclerView / ViewPager2 / Material 扩展绑定与 Resolver |
| `:sample` | Demo（不发布） |
| `:skin:purple` | 外部皮肤包示例「紫晶」（不发布，只带 res 的 APK） |

接入约定：

- 依赖组合：`:core` + `:androidx`
- `:androidx` 以 `api` 暴露 `:core`；仍建议在应用里显式声明 `:core`
- `:androidx` 对 AppCompat / RecyclerView / ViewPager2 / Material 均为 `compileOnly`，控件 Binding 走 `ThemeAndroidX.registerAvailable()` 按 classpath 尽力登记，缺依赖自动跳过
- `ThemeAndroidX.registerAvailable()` 须在**首次 inflate 前**调用

## 安装（JitPack）

仓库内版本写在根目录 `build.gradle.kts`：

```kotlin
group = "com.github.oppsgo"
version = "1.0.5"
```

库模块经 convention 插件配置 `maven-publish`；`jitpack.yml` 使用 **JDK 17**。

**重要：别人 `implementation` 的版本 ≠ 仓库里的 `version` 字符串。**

| 用途 | 用什么 |
|------|--------|
| 写入 POM / 本地工程版本 | 根工程 `version`（SemVer，预发布用大写 `-SNAPSHOT`） |
| 别人依赖的版本 | **Git 标签**（如 `1.0.5`）、**`main-SNAPSHOT`**、或 commit |
| JitPack 徽章 | 来自 GitHub **Release / tag**，不会自动扫仓库 `version` |

仓库若没有 Git tag，JitPack 只会按 commit 构建，版本会变成类似 `-8381f653c8-1`。

发布正式版：

1. 把根 `build.gradle.kts` 的 `version` 改成正式 SemVer（去掉 `-SNAPSHOT`）
2. 打同名 tag：`git tag 1.0.5 && git push --tags`
3. 在 [JitPack](https://jitpack.io/#oppsgo/themeless) 选该 tag 触发构建

```kotlin
maven { url = uri("https://jitpack.io") }

implementation("com.github.oppsgo.themeless:core:1.0.5")
implementation("com.github.oppsgo.themeless:androidx:1.0.5")
```

开发期未打 tag 时用分支快照（如 `main-SNAPSHOT`）。不要指望别人写 `0.x.y-SNAPSHOT` 就能从 JitPack 拉到（除非打了同名 tag）。

## 快速接入

### 1. 依赖

```kotlin
dependencies {
    implementation(project(":core"))
    implementation(project(":androidx"))
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
    ThemeAndroidX.registerAvailable()
    ThemeManager.get().install(this, delegate as? LayoutInflater.Factory2)
    super.onCreate(savedInstanceState)
}
```

`installDayNight(activity)` 系列重载会在 install 的同时装上日夜 Resolver。  
若 LayoutInflater 上已有其它 Factory，install 会失败（打 error 日志）且不覆盖。

### 3. 应用主题

```kotlin
ThemeManager.get().applyDayNight(this, night = true)
// 或自行构造 Resolver：
ThemeManager.get().apply(this, DayNightResourceResolver.followSystem(this))
ThemeManager.get().apply(this, DayNightResourceResolver.light(this))
ThemeManager.get().apply(this, DayNightResourceResolver.night(this))
// AppCompat：AppCompatDayNightResourceResolver.light / night / followSystem / of
```

布局使用资源引用（如 `@color/skin_page_bg`），inflate 时记入 Binding；`apply` 后按当前 Resolver 写回。  
某个 View 不参与换肤时可写 `app:themeViewAttrEnable="false"`（对应 `IViewBinding.setEnabled(false)`；仍会记录属性，只是 `apply` / `refresh` 不写回）。

## 日夜与 Force Dark

`DayNightResourceResolver` 从 **Application** 创建 Configuration，与 Activity / AppCompat DayNight 隔离：

- 强制亮色 → `values/`；强制暗色 → `values-night/`；跟随 → 按 Application uiMode
- **不会**改写 Activity 自身 Configuration

自定义固定色板（如 Demo「晴空蓝」）用亮色底座 Resolver + id 重映射（示例见 `:sample` 的 `MappedResourceResolver`），不要为自定义主题单独建 `values-night`。嵌套 `@color` 不会自动跟映射，需额外映射内层 id；或直接用下面的外部皮肤包。

### 系统夜间下的浅色肤

系统深色时若把 Activity 切成 Light / `MODE_NIGHT_NO`，Android 10+ 的 **Force Dark** 可能在送显前反相浅色像素。正确做法：

1. 主题声明 `android:forceDarkAllowed=false`
2. 展示浅色 / 自定义肤时，Activity **保持夜间宿主**（已是 FOLLOW/YES 则直接 `apply`；仅当 `localNightMode == MODE_NIGHT_NO` 时改成 `MODE_NIGHT_YES`）
3. 肤色只通过 `ThemeManager.apply(…, DayNightResourceResolver.light / Mapped…)` 绘制
4. **不要**为了「看起来亮」去切 `MODE_NIGHT_NO`

## 外部皮肤包

`SkinPackageResolver` 把宿主资源按「同名同类型」映射到只带 res 的皮肤 APK；皮肤包没有的自动回退被包装的 Resolver：

```kotlin
val base = DayNightResourceResolver.light(this)
val skin = SkinPackageResolver.load(base, this, "/sdcard/skins/purple.apk")
if (skin != null) ThemeManager.get().apply(this, skin)
```

- 皮肤包内的 selector / vector 引用在皮肤包内闭环解析（`applicationId` 即资源包名）
- `load` 失败（apk 不存在 / 损坏 / 反射建 Resources 失败）返回 `null`，由调用方降级提示
- id 映射按宿主 id 缓存；换肤时整体替换 Resolver 实例即可

## 常用 API

| API | 作用 |
|-----|------|
| `ThemeManager.install` / `installDayNight` | 挂 Factory2，须在 `super.onCreate` 前 |
| `ThemeManager.apply` / `applyDayNight` | 换 Resolver、递增代数、刷新内容树与已登记浮层 |
| `ThemeManager.setRefreshOnInflate` | 新 inflate 的 View 立刻刷一遍（Dialog / Popup 常用） |
| `ThemeManager.refresh(Context)` / `refresh(View)` | 仅刷新，不换 Resolver |
| `ThemeManager.getModCount` / `MOD_COUNT_NONE` | 该页刷新代数；未 install 返回 `MOD_COUNT_NONE` |
| `ThemeManager.registry` | Binding 注册表（`register` / `resolve` / `isRegistered`） |
| `ThemeManager.of(view)` / `of(view, Class)` | 取 Binding：已挂载复用，否则按注册表创建并挂 tag |
| `ThemeManager.find(view)` / `detach(view)` | 只查已挂载的 Binding / 卸下（含容器钩子） |
| `ThemeManager.addOnThemeUpdateListener` | 整页刷新完成回调（可多个，随页面回收） |
| `IViewBinding.setOnViewThemeUpdateListener` | 单 View 刷新完成回调（后设覆盖先设） |
| `ViewBinding.setBackground / setBackgroundTint` | 手动绑定并立刻写 View（`ColorRef` / `DrawableRef` / 资源 id 重载） |
| `TextViewBinding.setTextColor / setCompoundDrawableTint / setCompoundDrawables…` | TextView 系手动入口 |
| `DrawableRef.of / tinted / none` | drawable 引用、一行式着色、显式清空某方向 |
| `ColorStateListRef.fromColors` | 状态表 + 各态 provider 拼 `ColorStateList` |
| `ViewGroupBinding.setTraverseDepth` | 级联子 View 的穿透层数（`TRAVERSE_INFINITE` / `0` / 正整数） |

## 自定义 Binding

1. 继承对应 `*Binding`（如 `TextViewBinding`），在构造器里用 `bindRaw`（常规属性）/ `bindSpecial`（整组写入等特殊处理）逐 attr 登记，`alias` 把 `app:` 兼容属性归一到平台同名 attr  
2. 登记集即布局读取集，无需另声明 styleable  
3. `ThemeManager.get().registry().register(YourView.class, YourBinding::new)`（须在首次 inflate 前）  
4. 完全自写、不继承 `ViewBinding` 时实现 `IViewBinding`，属性表与开关自行维护

## AbsListView 特殊情况

`ListView` / `GridView` / `ExpandableListView` 由 `AbsListViewBinding` 处理：换肤后 item 复用滑回来时补刷，避免仍是旧色。

**默认行为**：Binding 挂载（`ThemeManager.of` / inflate）后占用 `ViewGroup.setOnHierarchyChangeListener`，子 View 挂上时补刷该 child。`ViewGroup` 对该 listener 只有 setter、无 getter，直接对 View 再 `set` 会覆盖 Binding。

**外部也要 hierarchy 回调时**：用 Binding 代理，勿直接对 View 设置；子项挂上时**先回调外部，再补刷**：

```kotlin
ThemeManager.get().of(list, AbsListViewBinding::class.java)
    .setOnHierarchyChangeListener { parent, child -> /* 你的逻辑 */ }
```

**三方 SDK 已占用该槽位时**：注册不占用的构造：

```kotlin
ThemeManager.get().registry().register(AbsListView::class.java) { AbsListViewBinding(it, false) }
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

`:sample` 结构：

| 页面 | 内容 |
|------|------|
| 首页 | 说明、当前皮肤；入口进入二级演示；**右上角「设置」** |
| 设置 | 皮肤与宿主日夜；**仅此页会持久化并作用于全应用** |
| 二级演示 | 按 Tab 分控件 / 列表（含 ListView·GridView）/ 其它（ProgressBar·SeekBar）/ 浮层；页内切肤**只改当前页** |

冷启动由 `ThemelessApp` 恢复皮肤与宿主日夜。需要 **JDK 17+**：

```bash
./gradlew :sample:assembleDebug
```

## License

见仓库根目录 [LICENSE](LICENSE)。
