# 依赖升级记录

本轮以 Maven Central、Google Maven 的 `maven-metadata.xml` 和官方发布说明核实版本。默认采用最新稳定版；经确认，Compose 与 Material 3 整套采用 `1.13.0-alpha01`。应用版本、Android 最低版本和业务行为不因依赖升级调整。

## 版本变化

| 依赖 | 升级前 | 升级后 |
| --- | --- | --- |
| Gradle Wrapper | 9.7.1 | 9.8.0 |
| Android Gradle Plugin | 9.3.2 | 9.4.1 |
| Kotlin / Compose Compiler / kotlin.test | 2.3.20 | 2.4.20 |
| KSP | 2.3.6 | 2.3.12 |
| Compose 插件、Runtime、UI、Foundation、Resources | 1.12.0 | 1.13.0-alpha01 |
| Compose Material 3 | 1.12.0-alpha03 | 1.13.0-alpha01 |
| AboutLibraries | 14.2.1 | 15.2.0 |
| AndroidX Core | 1.19.0 | 1.19.1 |
| Ktorfit | 2.7.2 | 2.7.5 |
| Ktor | 3.5.2 | 3.6.0 |
| Coil | 3.6.1 | 3.6.3 |
| Kermit | 2.1.0 | 2.2.0 |
| FileKit | 0.15.0 | 0.16.0 |
| Room | 2.8.4 | 2.8.5 |
| iOS SQLite framework | Room 传递依赖版本 | 显式使用 2.7.1 |

版本目录中其余库已是核实时的最新稳定版，保持不变。未将 Kotlin Beta、Serialization RC、Navigation Beta 或 AndroidX Activity Alpha 引入项目。

## 兼容处理

- Material 3 `1.13.0-alpha01` 的 Gradle metadata 要求 Compose Runtime/UI/Foundation `1.13.0-alpha01`，因此整套同步升级，避免表面固定稳定版、实际被传递依赖提升为 Alpha。
- Ktorfit `2.7.5` 会按 Kotlin 版本选择编译器插件；移除构建脚本中旧的 `compilerPluginVersion = 2.3.3` 覆盖。
- 使用 Gradle Wrapper 任务更新脚本和 JAR，并加入官方发行包 SHA-256。Wrapper JAR 的 SHA-256 也与官方值核对。
- iOS 模拟器 App 链接复现 `_sqlite3_load_extension` 未定义：Room 传递引入的旧 SQLite framework 存在已知问题，显式使用稳定版 `androidx.sqlite:sqlite-framework:2.7.1`。保留系统 SQLite 和现有数据库结构/迁移，不切换数据库驱动。
- 经确认，将 iOS App 的 Debug/Release 最低版本从 14.0 调整为 15.0，与 Kotlin/Native 产物一致。
- CI 的 `setup-android@v3` 默认安装已移除的 `tools` 包导致初始化失败，所有现有流程均显式改为安装 `platform-tools`。
- 保留现有 AGP 旧 DSL/KMP 兼容开关。本轮未拆分 Android 应用与 KMP 模块；弃用警告仍需后续迁移处理。
- Kotlin `2.4.20` 官方完整测试矩阵上限为 Gradle `9.7.0` / AGP `9.3.1`；本轮按要求采用更新的稳定版，通过项目构建验证，不宣称处于该官方完整支持矩阵内。

## 验证入口

Windows / JDK 21：

```powershell
.\gradlew.bat composeApp:testDebugUnitTest composeApp:assembleDebug
python scripts/verify_signing.py --debug
```

签名隔离脚本还要求 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`。依赖声明随构建重新生成，不提交被忽略的生成资源。

iOS 使用 `.github/workflows/check_ios.yaml`：

1. 在 macOS runner 上准备资源和版本配置。
2. 执行 `composeApp:linkReleaseFrameworkIosArm64`，验证真机架构的 Kotlin/Native Release 链接。
3. 使用 `xcodebuild` 构建 arm64 模拟器 Debug App，覆盖现有 framework Build Phase 和 Swift 宿主集成。
4. 仅本次验证命令禁用签名；不修改生产签名设置，不导入证书、不归档、不上传 TestFlight。

此检查覆盖编译和链接，不等同于真机运行、UI 回归或 App Store 签名验证。具体结果以对应提交的 Actions 运行记录为准。

## 核实来源

- [Gradle 当前版本及校验值](https://services.gradle.org/versions/current)
- [AGP 9.4 兼容说明](https://developer.android.com/build/releases/agp-9-4-0-release-notes)
- [Kotlin Gradle 兼容矩阵](https://kotlinlang.org/docs/gradle-configure-project.html)
- [Compose 1.13.0-alpha01](https://github.com/JetBrains/compose-multiplatform/releases/tag/v1.13.0-alpha01)
- [SQLite 发布说明：2.7.0-alpha02 修复 iOS 系统库符号缺失，稳定版 2.7.1 包含该修复](https://developer.android.com/jetpack/androidx/releases/sqlite)
- [KSP 2.3.12](https://github.com/google/ksp/releases/tag/2.3.12)
- [Ktorfit 2.7.5](https://github.com/Foso/Ktorfit/releases/tag/2.7.5)
- [AboutLibraries 15.2.0](https://github.com/mikepenz/AboutLibraries/releases/tag/15.2.0)
- [FileKit 0.16.0](https://github.com/vinceglb/FileKit/releases/tag/0.16.0)
- [Maven Central](https://repo.maven.apache.org/maven2/) 与 [Google Maven](https://dl.google.com/dl/android/maven2/)：具体坐标见 `gradle/libs.versions.toml`。
