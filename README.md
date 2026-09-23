# Themeless

[![](https://jitpack.io/v/oppsgo/themeless.svg)](https://jitpack.io/#oppsgo/themeless)

Android 运行时换肤库：在 **不重建 Activity** 的前提下，按资源 id 绑定并刷新 View。  
支持普通 `Activity` / `FragmentActivity`，以及 AndroidX / Support 的 AppCompat。

文档暂时只提供中文。

## 版本与 JitPack

版本写在根目录 `gradle.properties`（单一来源，同常见 Android 库做法）：

```properties
GROUP=com.github.oppsgo
VERSION_NAME=0.1.1-SNAPSHOT
```

根 `build.gradle.kts` 读入后赋给所有子模块；库模块已配 `maven-publish`（JitPack 构建用），并有 `jitpack.yml`（JDK 17）。

**重要：JitPack 给别人用的依赖版本 ≠ 仓库里的 `VERSION_NAME`。**

| 用途 | 用什么 |
|------|--------|
| 写入 POM / 本地工程版本 | `VERSION_NAME`（SemVer，预发布用大写 `-SNAPSHOT`） |
| 别人 `implementation` 的版本 | **Git 标签**（如 `0.1.1`）、**`main-SNAPSHOT`**、或 commit |
| JitPack 徽章列表 | 来自 GitHub **Release / tag**，不是自动扫 `VERSION_NAME` |

发布正式版流程：

1. 把 `VERSION_NAME` 改成 `0.1.1`（去掉 `-SNAPSHOT`）
2. 打同名 git tag：`git tag 0.1.1 && git push --tags`
3. 依赖示例：

```kotlin
maven { url = uri("https://jitpack.io") }

implementation("com.github.oppsgo.Themeless:core:0.1.1")
implementation("com.github.oppsgo.Themeless:androidx:0.1.1")
// 或 Support：com.github.oppsgo.Themeless:appcompat:0.1.1
```

开发期未打 tag 时用分支快照，例如 `main-SNAPSHOT`，**不要**指望别人写 `0.1.1-SNAPSHOT` 就能从 JitPack 拉到（除非你真的打了叫 `0.1.1-SNAPSHOT` 的 tag）。

比较顺序（Maven）：`0.1.1-SNAPSHOT` &lt; `0.1.1` &lt; `0.1.2-SNAPSHOT`。

## 模块

| 模块 | 说明 |
|------|------|
| `:core` | 核心：`ThemeManager`、`ResourceBinding`、日夜与 Context 系 `ResourceResolver` |
| `:androidx` | AndroidX AppCompat / RecyclerView 扩展绑定与 Resolver |
| `:appcompat` | 旧版 Support Library（`appcompat-v7` / `recyclerview-v7`）扩展 |
| `:app` | Demo（依赖 `:core` + `:androidx`） |

- 已迁 AndroidX → 依赖 `:core` + `:androidx`
- 仍用 Support Library → 依赖 `:core` + `:appcompat`
- **不要**在同一个 APK 里同时引入 `:androidx` 与 `:appcompat`

## 快速接入

### 1. 依赖

```kotlin
dependencies {
    implementation(project(":core"))
    implementation(project(":androidx")) // 或 :appcompat
}
```

### 2. 在 `super.onCreate()` 之前 install

普通页面：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ThemeManager.get().install(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.xxx)
    // 再 apply 主题…
}
```

AppCompat 页面（把 `AppCompatDelegate` 当作 Factory2 传入）：

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    ThemeManager.get().install(this, delegate as? LayoutInflater.Factory2)
    // 如需 AppCompat 控件绑定：
    // AppCompatTextViewResourceBinding.register()
    // AppCompatImageViewResourceBinding.register()
    // RecyclerViewResourceBinding.register()  // 须在 setContentView 之前
    super.onCreate(savedInstanceState)
    …
}
```

### 3. 应用主题

```kotlin
// 跟随系统日夜
ThemeManager.get().apply(this, DayNightResourceResolver.followSystem(this))

// 强制亮 / 暗（与系统无关；系统已是暗色时仍可强制亮色）
ThemeManager.get().apply(this, DayNightResourceResolver.light(this))
ThemeManager.get().apply(this, DayNightResourceResolver.night(this))

// AppCompat 工程用对应类：
// AppCompatDayNightResourceResolver.light / night / followSystem / of
```

布局里使用资源引用（如 `@color/skin_page_bg`），inflate 时会记入 Binding；`apply` 后按当前 `ResourceResolver` 重新取值并设回 View。

## 日夜说明

`DayNightResourceResolver` 从 **Application** 创建 `createConfigurationContext`，与 Activity / AppCompat DayNight 状态隔离：

- 系统暗色时强制亮色 → 读 `values/`
- 强制暗色 → 读 `values-night/`
- 不会改写 Activity 自身的 `Configuration`

自定义固定色板（如 Demo「晴空蓝」）应使用亮色底座 Resolver + id 重映射（Demo 里是 `DayNightResourceResolver.light` + `MappedResourceResolver`），**不要**为自定义主题单独建 `values-night`。

### 系统夜间下的浅色肤（重要）

系统开着深色时，若把 Activity 切成 Light / `MODE_NIGHT_NO`，Android 10+ 的 **Force Dark**（部分厂商还会加一层）会在**送显前**把浅色像素反相成黑。表现是：日志里 View 颜色已是浅蓝，屏幕仍是黑——**用户什么都不用懂，也不用去关系统设置**。

接入方正确做法：

1. 主题里加 `android:forceDarkAllowed=false`
2. **展示浅色 / 自定义肤时，Activity 保持夜间宿主**，例如：
   - AppCompat：仅当 `localNightMode == MODE_NIGHT_NO` 时改成 `MODE_NIGHT_YES`（再 recreate）；已是 FOLLOW/YES 则直接 `apply`
   - 肤色只通过 `ThemeManager.apply(…, DayNightResourceResolver.light / Mapped…)` 画上去
3. **不要**为了「看起来像亮色页」去切 `MODE_NIGHT_NO` / `Theme.*.Light`

Demo 的亮色 / 晴空蓝已按上述方式处理（见 `ThemeDemoPage.syncActivityNightMode`）。

## 常用 API

| API | 作用 |
|-----|------|
| `ThemeManager.install` | 挂 LayoutInflater.Factory2，须在 `super.onCreate` 前 |
| `ThemeManager.apply` | 换 Resolver、递增代数、刷新内容树与已登记浮层 |
| `ThemeManager.setRefreshOnInflate` | 换肤后新 inflate 的 View 立刻刷一遍（Dialog / Popup 常用） |
| `ThemeManager.refresh` | 仅刷新，不换 Resolver |
| `TextViewResourceBinding.setTrackTextSize` | 全局：inflate 是否自动跟肤 `android:textSize`（默认关；须在 inflate 前设置） |
| `TextViewResourceBinding.of(view).setTextColor(…)` | 手动绑定后 `.refresh()` |

手动 `setTextSize(dimen)` 不受 `setTrackTextSize` 影响。

## 自定义 Binding

1. 继承对应 `*ResourceBinding`，在 `getViewStyleable()` 里声明要跟踪的 attr  
2. `ThemeManager.get().bindings().register(YourView.class, YourBinding::new)`  
3. 完全自写、不继承库内 Binding 时，属性列表与开关需自行实现（例如字号不会自动吃到 `setTrackTextSize`）

## Demo

- **ThemeDemoActivity** — `FragmentActivity`，不传 Factory2  
- **ThemeAppCompatDemoActivity** — `AppCompatActivity`，传入 `AppCompatDelegate`

两页均演示：沉浸式标题、左侧操作 + 右侧 RecyclerView、Dialog / Popup 跟肤、亮/暗/自定义主题。

## 构建

```bash
./gradlew :app:assembleDebug
```

需要 **JDK 17+**。
