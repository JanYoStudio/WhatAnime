# 开发构建与测试

## 环境

- JDK 21，通过 `JAVA_HOME` 指向安装目录。
- Android SDK，通过 `ANDROID_HOME` / `ANDROID_SDK_ROOT` 或未提交的 `local.properties` 中的 `sdk.dir` 配置；SDK 版本见 `gradle/libs.versions.toml`。
- 使用仓库 Gradle Wrapper。以下命令为 PowerShell；macOS/Linux 将 `.\gradlew.bat` 换为 `./gradlew`。
- iOS 构建需要 macOS/Xcode，Android 测试成功不能代替 iOS 验证。

## 无发布密钥的日常开发

```powershell
.\gradlew.bat composeApp:testDebugUnitTest composeApp:assembleDebug
```

无需设置任何 `SIGN_KEY_*`。debug 使用 Android 默认调试签名；Compose 资源复制自动依赖 `exportLibraryDefinitions`，首次构建无需手动生成依赖声明。

共享单元测试位于 `composeApp/src/commonTest/kotlin`，使用 `kotlin.test`。目前覆盖集数显示解析和字符串哈希标准向量；在 Android JVM 上运行，不需要模拟器、真实设备、网络 API 或发布凭据。首次构建下载 Gradle/依赖仍需要网络。

- HTML 报告：`composeApp/build/reports/tests/testDebugUnitTest/index.html`。
- JUnit XML：`composeApp/build/test-results/testDebugUnitTest/`。
- 新增纯逻辑测试优先放在 `commonTest`；平台测试按需要放在平台测试 source set，不为测试方便重构无关业务。

## Release 签名契约

以下四项必须完整且有效：

```properties
SIGN_KEY_STORE_FILE=<keystore 文件路径>
SIGN_KEY_STORE_PASSWORD=<keystore 密码>
SIGN_KEY_ALIAS=<私钥 alias>
SIGN_KEY_PASSWORD=<私钥密码>
```

配置逐字段读取：优先使用根目录 `local.properties` 的非空白值，否则回退同名环境变量。密码内容不做 trim；空白仅用于判断是否缺项。存在只包含 SDK 路径的 `local.properties` 不会屏蔽环境变量。

keystore 相对路径沿用原行为，相对 `composeApp/` 解析；推荐使用绝对路径。Windows 的 `.properties` 路径可使用正斜杠，避免反斜杠转义问题。不要提交实际配置、密钥或密码。

```powershell
# 只校验配置、keystore、私钥密码及证书，不生成发布产物
.\gradlew.bat composeApp:verifyReleaseSigning

# 需要有效发布签名
.\gradlew.bat composeApp:assembleRelease
.\gradlew.bat composeApp:bundleRelease
```

校验挂在 release 任务依赖中，直接打包或通过聚合任务执行也受保护：

- `[RELEASE_SIGNING_REQUIRED]`：缺少必需配置，错误只列字段名。
- `[RELEASE_SIGNING_INVALID]`：文件不存在、keystore 无法读取、密码/alias 无效或缺少私钥证书。

不会在缺失密钥时生成未签名 release，也不会自动使用 debug key 或新建临时 key。配置正确只表明密钥可用于签名，不代表已验证商店认可的签名身份；CI 必须提供正确的发布密钥。

## 签名行为回归验证

需要 Python 3.10+，并显式设置 `ANDROID_HOME` 或 `ANDROID_SDK_ROOT`。脚本不读取本机 `local.properties`。

```powershell
# 完整验证：无 key 的 debug APK + 单元测试 + 签名边界
python scripts/verify_signing.py --debug

# 仅验证签名边界（不重新打包 debug）
python scripts/verify_signing.py
```

脚本复制当前工作区源码到临时目录，剔除本机签名配置及 keystore 文件，清除子进程中的四个签名环境变量。副本通过只读 Git 版本查询复用原仓库元数据，不执行 Git 写操作。

验证包括：

- 无 `local.properties` 和空配置文件两种情况下均可配置项目；iOS 版本配置生成不要求 Android key。
- 无 key 时 `assembleRelease`、`bundleRelease`、`packageRelease`、`packageReleaseBundle` 明确失败。
- 配置逐项回退、本地优先、空白回退、完整本地配置与相对路径、四种缺项和错误路径/密码/alias。
- 完整模式必须生成 debug APK，并实际执行非零数量的单元测试，不能以 `NO-SOURCE` 冒充通过。

正向校验使用脚本显式生成的隔离测试密钥，仅执行签名校验，不构建或发布 release。结束后删除临时副本与测试密钥。Gradle 日志和测试报告保存在 `build/reports/signing/`；运行失败时查看对应日志。

## CI

- `.github/workflows/check.yaml`：PR、`dev` 推送及手动触发，JDK 21，无发布 secrets，执行完整隔离验证并上传报告；不发布产物。
- `.github/workflows/build_android.yaml`、`release.yaml`：必须提供 `SIGN_KEY_ALIAS`、`SIGN_KEY_STORE_PASSWORD`、`SIGN_KEY_PASSWORD` 和 `SIGN_KEY_BASE64` 四个 secrets；`SIGN_KEY_STORE_FILE` 由 workflow 指定。缺项直接失败，不生成替代密钥。
- `.github/workflows/check_ios.yaml`：PR、`dev` 推送及手动触发，在 macOS 上验证真机 Release framework 链接和模拟器 App 编译，不需要签名或发布凭据。
- `.github/workflows/build_ios.yml`：不再准备 Android keystore；Apple 签名、证书和上传要求保持不变。

现有 AGP/Kotlin Multiplatform 兼容开关与弃用警告仍存在；本轮没有迁移 Android/KMP 模块结构。不要为了消除警告擅自移除兼容开关。
