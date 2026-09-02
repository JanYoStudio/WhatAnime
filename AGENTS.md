# AGENTS.md

本文档面向在本仓库中工作的自动化编码代理。请优先遵循这里的项目约定；如与用户的明确指令冲突，以用户指令为准。

## 语言要求

- 与用户对话、代码注释、项目文档和 Git 提交信息默认使用中文。
- 技术术语、命令、包名、类名、函数名、变量名等代码标识符保持英文原形。
- 新增用户可见文案时，应同步考虑现有英文、简体中文和繁体中文资源，避免在共享 UI 中直接硬编码展示文本。

## 项目概览

- 项目名称：WhatAnime。
- 项目定位：通过动漫截图调用 trace.moe API 反向搜索动画来源。
- 技术栈：Kotlin Multiplatform、Compose Multiplatform、Android Application、iOS Framework。
- 主模块：`composeApp`。
- 包名：`pw.janyo.whatanime`。
- 构建系统：Gradle Kotlin DSL，依赖版本由 `gradle/libs.versions.toml` 统一管理。
- Java/Kotlin JVM 目标：JDK 21。
- Android 最低版本：API 24。
- 当前基础版本：见 `gradle/libs.versions.toml` 中的 `app-version`。

## 目录结构

- `composeApp/src/commonMain`：跨平台共享业务逻辑、Compose UI、ViewModel、Repository、Room、网络接口、资源与工具。
- `composeApp/src/androidMain`：Android 专属实现，包括 Activity、OkHttp、MMKV、Room Builder、文件与系统能力。
- `composeApp/src/iosMain`：iOS 专属实现，包括 Compose ViewController、Darwin、Settings、Room Builder、文件与系统能力。
- `composeApp/src/commonMain/composeResources`：Compose Multiplatform 图片、文件和多语言字符串资源。
- `composeApp/src/androidMain/res`：Android Manifest 相关资源、主题、图标和 locale 配置。
- `composeApp/schemas`：Room schema 输出目录。
- `iosApp`：iOS 宿主工程与 Xcode 配置。
- `.github/workflows`：Android Nightly、iOS TestFlight 和正式发布流程。
- `docs`：项目补充文档；新增长期有效的设计或维护说明时放在此目录。

## 开发原则

- 优先将业务逻辑、UI 状态和可复用 UI 放在 `commonMain`；只有平台能力确实不同才使用 `expect/actual` 放入平台 source set。
- 修改前先搜索现有实现、组件和工具函数，保持当前包结构、命名方式与 Compose 写法一致。
- 聚焦用户要求，不修改无关文件，不进行无关重构或全仓格式化。
- 工作区存在用户未提交改动时必须保留，不得覆盖、回滚或清理。
- UI 改动沿用 Compose Multiplatform + Material 3 风格，不引入第二套设计体系。
- 异步业务继续使用协程、`StateFlow` 和 ViewModel 状态，不以阻塞调用替代现有模式。
- 不要随意升级依赖。尤其注意 `gradle/libs.versions.toml` 中 Room 版本旁的 iOS 编译限制，以及 Kotlin、KSP、Compose、Ktorfit 之间的兼容关系。
- 不要为了通过本地验证而降低发布配置、删除签名流程、关闭混淆或绕过数据库迁移。

## 架构概览

项目采用 MVVM + Repository 分层：

```text
Screen (Compose)
    ↓ collect StateFlow / invoke actions
ViewModel (ComposeViewModel)
    ↓
AnimationRepository
    ├── SearchApi (Ktorfit / trace.moe)
    ├── HistoryService / HistoryDao (Room)
    └── PlatformFile cache (FileKit)
```

### 共享层核心目录

| 目录 | 说明 |
|---|---|
| `api/` | Ktorfit REST 接口，目前主要是 `SearchApi` |
| `base/` | 应用信息与 `ComposeViewModel` 等基础能力 |
| `db/` | Room 数据库、DAO、Service 和 schema 版本 |
| `model/` | 搜索结果、历史记录、API 额度、调试信息等模型 |
| `module/` | Koin 模块：platform、database、network、repository、viewModel、media |
| `repository/` | 搜索、本地命中、网络请求、历史保存和缓存删除 |
| `ui/navigation/` | 基于 Kotlin Serialization 的类型安全路由 |
| `ui/screen/` | Main、History、Detail、Settings、About 页面 |
| `ui/components/` | 搜索结果、底部菜单、视频播放器、删除容器等复用组件 |
| `ui/preference/` | 设置页面复用组件 |
| `ui/theme/` | 主题、深色模式和图标封装 |
| `utils/` | 文件、哈希、时间、字符串和系统能力工具 |
| `viewmodel/` | Main、History、Detail、Settings ViewModel |

### 页面与导航

路由定义在 `ui/navigation/Nav.kt`：

- `RouteMain`：截图搜索主页。
- `RouteHistory`：历史记录列表。
- `RouteDetail`：历史搜索详情，参数为 `historyId` 和 `cachePath`。
- `RouteSettings`：应用设置。
- `RouteAbout`：开源依赖信息。

导航通过 `LocalNavController` 获取 `NavController`，并在 `App.kt` 的 `NavHost` 中统一注册。新增页面时应沿用类型安全路由，不要混用字符串路由。

## 核心业务流程

### 图片搜索

入口为 `MainViewModel.searchImageFile()`：

1. FileKit 选择图片。
2. 检查文件大小，当前上限为 25 MiB。
3. 将图片复制到应用缓存目录，避免源文件被删除后历史记录失效。
4. `AnimationRepository.queryAnimationByImageLocal()` 先按原始路径查询 Room。
5. 未命中时进入在线流程，并尝试按文件 MD5 查询已有记录。
6. 根据 `Configure.cutBorders` 调用带黑边裁剪或不裁剪的 trace.moe 搜索接口。
7. 将 API 结果序列化后写入 Room，并保留缓存图片路径。
8. 根据 `Configure.hideSex` 过滤成人结果，再更新 UI 状态。

修改此流程时必须同时考虑：缓存图片生命周期、Room 历史记录、网络异常、文件大小、成人内容过滤和历史详情兼容。

### trace.moe API

- Base URL：`https://api.trace.moe/`。
- `POST search?cutBorders&anilistInfo`：服务端裁剪黑边后搜索。
- `POST search?anilistInfo`：不裁剪黑边搜索。
- `GET me`：查询 API 额度和优先级。
- 个人 API Key 使用 `x-trace-key` 请求头，通过 `Configure.apiKey` 保存。

网络客户端在 `module/NetworkModule.kt` 中统一配置。新增网络行为时优先复用现有 `HttpClient`、Ktorfit、Kotlinx Serialization 和 Kermit，不要在 UI 或 ViewModel 中直接创建客户端。

### 历史与文件缓存

- Room 数据库：`AppDatabase`，当前版本为 5。
- 历史表：`tb_animation_history`。
- 完整 API 结果以 JSON 保存在 `animation_result` 字段。
- 删除历史记录时，应同时删除对应缓存图片。
- iOS 沙盒路径可能在重启或安装后变化，读取历史时通过 `getCacheFilePathBySavedCacheFilePath()` 重建缓存路径。
- 修改实体或 DAO 时必须提供 Migration，并检查 `composeApp/schemas` 是否更新。

### 搜索结果与视频

搜索结果可执行：

- 复制标题、AniList ID、MyAnimeList ID。
- 分享标题。
- 打开 AniList 页面。
- 播放 trace.moe 预览视频。

历史详情会把超过 10 分钟的搜索结果视为视频 Token 过期。修改预览播放逻辑时，应保留 HTTP 错误提示和播放器释放逻辑。

## 状态管理

- ViewModel 继承 `ComposeViewModel`。
- UI 状态使用只读 `StateFlow` 暴露，对应可变状态保留在 ViewModel 内部。
- Composable 通过 `collectAsState()` 观察状态，通过 ViewModel 方法触发行为。
- 可恢复的业务错误应更新状态或显示 Snackbar/Toast，不应直接导致页面崩溃。
- 网络和文件操作应在 ViewModel/Repository 的协程中执行。
- ViewModel 清理时注意释放媒体播放器等持有资源。
- 新增状态时优先扩展现有页面 State 数据类，避免在 Composable 中散落相互依赖的业务状态。

## 依赖注入

- 使用 Koin，模块列表由 `module/Module.kt` 中的 `moduleList()` 统一管理。
- Repository、API、数据库和媒体组件通常注册为单例。
- ViewModel 在 `ViewModelModule.kt` 中注册，并在 Compose 中通过 `koinViewModel()` 获取。
- 新增服务后必须在合适的 Koin 模块中注册，不要在页面中手动构造复杂依赖。

## expect/actual 平台边界

当前通过 `expect/actual` 处理的能力包括：

- 配置读写：Android 使用 MMKV，iOS 使用 Multiplatform Settings/NSUserDefaults。
- HTTP 引擎：Android 使用 OkHttp，iOS 使用 Darwin。
- Room 数据库构建。
- 文件缓存路径和 iOS 沙盒路径重建。
- 网络状态检查。
- 剪贴板与系统分享。
- 应用信息、商店链接和设备标识。
- 主题能力与系统返回键。

新增平台能力时先在 `commonMain` 定义最小接口，再分别实现 `actual`。不要在共享代码中直接引用 Android 或 iOS 类型。

## 已知现状与注意事项

以下是当前代码现状，修改相关区域时应先验证，不要基于文档直接假设其行为完整：

- iOS `userAgent()` 当前返回 `"TODO"`。
- iOS `isOnline()` 当前固定返回 `true`，尚未执行真实网络状态检查。
- Repository 会按 MD5 查询 `origin_path`，但当前保存历史时主要写入原始路径；修改缓存去重逻辑前应核对实际数据库数据和预期策略。
- 调试 HTTP 响应保存在全局可变列表 `httpResponses` 中，当前最多保留 5 条。
- `HistoryDao.queryAllHistory()` 没有显式 `ORDER BY`；不要依赖数据库默认返回顺序。
- 当前未发现自动化 Kotlin 测试。新增复杂业务逻辑时应根据任务需求补充测试或至少执行针对性的构建验证。

## 代码约定

- Kotlin 包路径保持在 `pw.janyo.whatanime` 下。
- 遵循现有 Kotlin 格式和命名习惯，不为了个人偏好重排整个文件。
- Composable 页面放在 `ui/screen`，可复用组件放在 `ui/components` 或 `ui/preference`。
- UI 不直接访问 DAO 或 API；通过 ViewModel 和 Repository 分层调用。
- 网络模型使用 `@Serializable`，JSON 字段差异使用 `@SerialName`。
- 日志优先使用 Kermit，避免 `println` 或平台专属日志散落在 `commonMain`。
- 图片加载优先复用 Coil 3；平台文件选择和操作优先复用 FileKit。
- 结构化数据持久化优先复用 Room，简单配置继续通过 `Configure` 的跨平台键值存储处理。
- 新增依赖先在 `gradle/libs.versions.toml` 声明，再通过 `libs.xxx` 引用，禁止在模块构建脚本中直接硬编码版本号。
- 不要在源码中硬编码凭据、Token、签名密码或本机绝对路径。

## 资源与本地化

Compose Multiplatform 文案位于：

- `composeApp/src/commonMain/composeResources/values/strings.xml`：英文默认资源。
- `composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml`：简体中文。
- `composeApp/src/commonMain/composeResources/values-zh-rTW/strings.xml`：繁体中文。

Android locale filter 当前包含：

- `en`
- `zh-rCN`
- `zh-rTW`

新增或修改用户可见文案时：

1. 优先使用 `stringResource()` 或 `getString()`。
2. 同步维护三套字符串资源，除非明确标记为 `translatable="false"`。
3. 保持占位符数量和类型一致。
4. 避免在公共 UI 中新增中文或英文硬编码文本。

## 数据库规范

- Room schema 输出目录为 `composeApp/schemas`。
- 修改 `AnimationHistory`、`HistoryDao`、`AppDatabase` 或数据库构建配置时，必须检查 schema 和 KSP 生成结果。
- 数据库版本升级必须提供从当前版本到新版本的 Migration。
- Migration 应尽量保留用户历史数据，不得以破坏性迁移作为默认方案。
- 修改缓存路径字段时，同时验证 Android 固定路径和 iOS 沙盒路径变化场景。

## 构建与验证

Windows 环境使用 `gradlew.bat`，CI/macOS/Linux 使用 `./gradlew`。

常用命令：

```shell
# 快速验证共享代码和 Android 编译
./gradlew composeApp:compileKotlinAndroid

# Android 调试 APK
./gradlew composeApp:assembleDebug

# Android 发布 APK
./gradlew composeApp:assembleRelease

# Google Play AAB
./gradlew composeApp:bundleRelease

# 生成开源依赖声明
./gradlew composeApp:exportLibraryDefinitions

# 更新 iOS 版本配置
./gradlew composeApp:updateAppleBuildVersion

# 清理
./gradlew clean
```

验证建议：

- 纯文档改动：检查 Markdown 和 Git diff，不需要运行 Gradle。
- 普通 Kotlin 或共享逻辑改动：至少运行 `composeApp:compileKotlinAndroid`。
- Android UI、资源、Manifest、BuildConfig 或依赖改动：运行 `composeApp:assembleDebug`。
- Room 实体、DAO、Migration 或 KSP 改动：运行相关编译任务并检查 `composeApp/schemas`。
- 依赖或开源声明改动：运行 `composeApp:exportLibraryDefinitions`，确认生成文件变化合理。
- 发布配置改动：尽量按对应 GitHub Actions 的步骤验证。
- iOS 相关改动需要 Xcode/Kotlin Native 环境；无法验证时必须在最终说明中明确指出。

Release 构建依赖 `local.properties` 中的签名配置：

```properties
SIGN_KEY_STORE_FILE=<JKS路径>
SIGN_KEY_STORE_PASSWORD=<密码>
SIGN_KEY_ALIAS=<别名>
SIGN_KEY_PASSWORD=<密码>
```

缺少签名信息时，优先使用 debug 或 compile 任务验证，不要修改签名脚本规避问题。

## 版本号与发布

基础版本来自 `gradle/libs.versions.toml` 的 `app-version`，Git 提交数量作为 `versionCode`。

版本名规则：

- Debug：`{appVersionName}.d{gitCommitCount}.{shortHash}`。
- Nightly：`{appVersionName}.n{gitCommitCount}.nightly`。
- Release：`{appVersionName}.r{gitCommitCount}.{shortHash}`。

相关 GitHub Actions：

- `.github/workflows/build_android.yaml`：`dev` 分支 Android Nightly APK 和 GitHub Release。
- `.github/workflows/build_ios.yml`：`dev` 分支 iOS 构建并上传 TestFlight。
- `.github/workflows/release.yaml`：手动构建正式 AAB 和发布产物。

Commit message 包含 `ci skip` 时可能跳过自动构建。不要无理由加入该标记。

## 修改前检查清单

1. 明确改动属于 `commonMain` 还是平台 source set。
2. 搜索现有同类实现，复用已有组件、工具和 Koin 模块。
3. 检查是否影响缓存文件、Room schema、API Key、视频 Token 或历史兼容。
4. 如果有用户可见文案，检查三套本地化资源。
5. 如果有依赖变更，检查 Kotlin、KSP、Room、Compose 和 iOS 兼容性。
6. 选择最小但足够的验证命令。
7. 最终回复中说明修改文件、验证结果和未覆盖风险。

## 禁止事项

- 不要提交 `local.properties`、签名文件、密码、API Key、Token、Apple 凭据或其他敏感信息。
- 不要提交 `build/`、`.gradle/`、`.kotlin/`、IDE 临时文件等生成内容。
- 不要为了让本地构建通过而删除签名、混淆、CI 或发布步骤。
- 不要绕过版本目录直接硬编码依赖版本。
- 不要在没有需求时大规模重排目录、重命名包或统一格式化全仓库。
- 不要在共享代码中引入 Android/iOS 专属 API。
- 不要在未提供 Migration 的情况下修改 Room 数据库结构。
- 不要覆盖或回滚用户已有的未提交改动。
- 不要自动执行提交、推送、发布、上传 TestFlight 或创建 GitHub Release，除非用户明确要求。
