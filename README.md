# Themeless

Android 运行时换肤库：在 **不重建 Activity** 的前提下，按资源 id 绑定并刷新 View。  
支持普通 `Activity` / `FragmentActivity`，以及 AndroidX / Support 的 AppCompat。

文档暂时只提供中文。

## 版本

根 `build.gradle.kts` 里定死一套坐标，子模块继承（同 [json-kit](https://github.com/oppsgo/json-kit)）：

```kotlin
group = "io.github.oppsgo"
version = "0.1.1-SNAPSHOT"
```

当前：**`0.1.1-SNAPSHOT`**

| 规则 | 说明 |
|------|------|
| 格式 | `MAJOR.MINOR.PATCH`，遵循 [SemVer](https://semver.org/) |
| 开发中 | 后缀 **`-SNAPSHOT`（必须大写）**，不要写成 `-snapshot` |
| 正式版 | 去掉后缀，例如 `0.1.1` |
| 比较顺序（Maven / Gradle） | `0.1.0` &lt; `0.1.1-SNAPSHOT` &lt; `0.1.1` &lt; `0.1.2-SNAPSHOT` &lt; `0.1.2` |
| Demo APK | `versionName` = 上式；`versionCode` 在 `gradle.properties` 的 `VERSION_CODE`，与库坐标分开 |

升级时只改根 `version`（以及需要上架 Demo 时再加 `VERSION_CODE`）。  
不要用 `0.1.1.1`、`0.1.1-snapshot`、日期串当主版本号，否则依赖解析和「谁更新」会乱。

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

自定义固定色板（如 Demo「晴空蓝」）应使用 `ContextResourceResolver`（或 AppCompat 版）+ id 重映射，**不要**再包一层 DayNight，也不要为自定义主题单独建 `values-night`。

AppCompat Demo 里切换亮/暗时会同步 `AppCompatDelegate.localNightMode`，让未托管的主题属性也一致；这与 `skin_*` 跟肤是两条线。

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
