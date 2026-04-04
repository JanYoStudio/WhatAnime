# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

WhatAnime 是一个 Kotlin Multiplatform Mobile (KMM) 应用，支持 Android 和 iOS 双平台。功能是通过图片反向搜索动漫来源（调用 trace.moe API）。

**包名**: `pw.janyo.whatanime`  
**最低支持**: Android 7.0 (API 24)，iOS 对应版本  
**当前版本**: 见 `composeApp/build.gradle.kts` 中的 `appVersionName`

## 构建命令

```bash
# 调试版 APK
./gradlew composeApp:assembleDebug

# 发布版 APK
./gradlew composeApp:assembleRelease

# 发布版 Bundle (用于 Google Play)
./gradlew composeApp:bundleRelease

# 更新 iOS 版本号（提交到 Xcode 前需要运行）
./gradlew composeApp:updateAppleBuildVersion

# 生成开源库声明（更新依赖后运行）
./gradlew composeApp:exportLibraryDefinitions

# 清理
./gradlew clean
```

本地签名配置在 `local.properties`（不提交到 git）：
```properties
SIGN_KEY_STORE_FILE=<JKS路径>
SIGN_KEY_STORE_PASSWORD=<密码>
SIGN_KEY_ALIAS=<别名>
SIGN_KEY_PASSWORD=<密码>
```

## 架构

采用 **MVVM + Repository** 分层架构，数据流：

```
UI（Compose Screen）→ ViewModel（StateFlow）→ Repository → API / Room 数据库
```

**代码分布**：
- `commonMain/` — 约 90% 代码，Android 与 iOS 共享
- `androidMain/` — Android 平台特定实现（OkHttp 引擎、MMKV 存储、Activity）
- `iosMain/` — iOS 平台特定实现（Darwin 引擎、Settings 存储、ViewController）

平台差异通过 `expect/actual` 机制处理，涉及：HTTP 客户端引擎、文件系统访问、数据库初始化、主题适配。

### 核心模块

| 包 | 职责 |
|---|---|
| `api/` | Ktorfit 定义的 REST 接口（trace.moe API） |
| `db/` | Room 数据库（v5）+ DAO + 业务 Service |
| `model/` | 数据类（Animation、AnimationHistory、SearchAnimeResult 等） |
| `repository/` | `AnimationRepository` 统一数据访问，含本地缓存、MD5 去重、在线搜索 |
| `viewmodel/` | Main、Detail、History、Settings 四个 ViewModel |
| `ui/screen/` | 四个屏幕（Main、Detail、History、Settings、About） |
| `ui/components/` | 可复用组件（SearchResultItem、VideoDialog、MediaPlayer 等） |
| `module/` | Koin 依赖注入模块 |
| `utils/` | 加密（MD5/SHA）、文件、字符串、时间工具 |

### 关键设计

**离线优先**：搜索前先按文件路径查本地缓存 → 再按 MD5 查去重缓存 → 最后才发起网络请求，结果写入 Room 数据库和本地文件。

**搜索入口** (`AnimationRepository.queryAnimationByImageLocal`): 同时支持带边框裁剪和不裁剪两种模式，对应 `SearchApi.search()` / `SearchApi.searchNoCut()`。

**状态管理**：ViewModel 继承 `ComposeViewModel`，使用 `StateFlow` 暴露 UI 状态，通过 `CoroutineExceptionHandler` 统一处理异常。

## 技术栈

| 分类 | 库 |
|---|---|
| UI | Compose Multiplatform + Material 3 |
| 网络 | Ktor Client + Ktorfit |
| 图片加载 | Coil 3 |
| 数据库 | Room (KMP) |
| 序列化 | Kotlinx Serialization JSON |
| 依赖注入 | Koin 4 |
| 键值存储 | MMKV（Android）/ Multiplatform Settings（iOS） |
| 日志 | Kermit |
| 文件操作 | FileKit |

## 依赖管理

所有依赖版本集中在 `gradle/libs.versions.toml`，使用 Gradle Version Catalog 管理。新增依赖先在此文件声明版本和库定义，再在构建脚本中引用 `libs.xxx`。

KSP 版本需与 Kotlin 版本严格匹配（格式：`kotlinVersion-kspVersion`），Room 使用 KSP 生成代码。

## CI/CD

GitHub Actions 工作流在 `.github/workflows/`：
- `build_android.yaml` — push 到 dev 分支自动构建并创建 GitHub Release（Nightly 版本）
- `build_ios.yml` — push 到 dev 分支自动构建并上传 TestFlight
- `release.yaml` — 手动触发，构建 AAB 正式版本

commit message 包含 `ci skip` 可跳过自动构建。

版本号规则：
- Debug: `{appVersionName}.d{gitCommitCount}.{shortHash}`
- Nightly: `{appVersionName}.n{gitCommitCount}.nightly`
- Release: `{appVersionName}.r{gitCommitCount}.{shortHash}`

## 语言与注释规范

- 代码注释、文档、git 提交信息均使用**中文**
- 技术术语和代码标识符保持英文原形
