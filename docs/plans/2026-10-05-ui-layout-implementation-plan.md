# 界面布局优化实施计划

> 执行方式：主会话使用 `executing-plans` 逐任务直接实现，按 `test-driven-development` 先红后绿；不派发代码实现 worker。任务内步骤使用复选框跟踪。Gemini 在整体审查阶段检查 UI 交互，主会话独立进行整段 diff 的代码质量复核。

**目标：** 落实已确认的三 Tab、自适应布局与配套状态/导航/媒体/数据契约。

**架构：** 沿用 Screen → ViewModel → Repository → API/Room。业务状态由独立且可测试的状态类型表达，根导航图保存一级页面 VM；只有详情 VM 随详情路由销毁。复用现有 Material 3、FileKit、Coil 和播放器，不替换基础设施。

**技术栈：** Kotlin Multiplatform、Compose Multiplatform、Material 3、Koin、Navigation Compose、协程/StateFlow、Room、Ktorfit、kotlin.test。

**设计依据：** [整体设计](2026-10-05-ui-layout-design.md)、[UI 规范](2026-10-05-ui-layout-ui-spec.md)。用户已授权本计划编写后继续实现，不另行询问执行方式；不包含推送/合并/发布授权。

## 全局约束

- 25 MiB 客户端上限；不新增取消搜索、拍照、拖放、收藏、独立一键清空入口、历史强制重搜。
- 2026-10-06 交互增补按用户确认实施：选择模式中可全选并确认批量删除，其行为以整体设计及 UI 规范为准。Android 已验证选择、确认/取消、真实数据库删除、主题确认、滑动初期反馈及预见式返回推进/取消/完成；iOS 编译、实机侧滑与 VoiceOver 尚未验证，不沿用上一轮 CI 成功作为本轮证据。
- 保持 trace.moe HTTP 契约和 Room schema；缓存路径保持双平台兼容。
- 内容宽至少784dp且高度允许才开启双栏；低高度阈值480dp，常规rail切换宽度600dp；以扣除导航/安全区后的实际约束计算。
- 原图/候选/错误必须属于同一请求；过滤后空与真正零结果分别表达。
- Key 不进入路由、可保存UI状态、日志或非编辑态文案；调试复制必须消费安全文本。
- 三语资源、48dp热区、1.0/1.5/2.0字体、Android/iOS验收不可省略。
- 所有临时账本、测试输出、审查报告、截图在系统临时目录；不新增仓库内临时工具体系。
- 仅设计提交已明确授权并执行；实现过程中不自动提交产品代码，最终交付前保留可审阅的工作区差异。

## Review Focus

1. A搜索成功后B失败/空结果：不得显示B图片配A结果；任务1测试四种终态与迟到响应。
2. 请求中切Tab/重建/退出浮层：不能重复上传、重复弹播放器或提前释放其他页面资源；任务4、5测试并设备复验。
3. Key切换时旧额度迟到：不能覆盖新Key额度，短Key也不得明文展示；任务2测试generation与错误缓存。
4. iOS沙盒迁移、共享缓存、删除部分失败：不得删源图/其他记录图片或伪报数据库回滚；任务3测试规范化与删除结果。
5. 双向窄窗口、大字体、长标题：导航与FAB始终可达，菜单完整标题不省略；任务6测试布局决策边界，任务8实际渲染。

## 文件与接口约定

下列共享路径前缀 `K/` = `composeApp/src/commonMain/kotlin/pw/janyo/whatanime/`；测试前缀 `T/` = `composeApp/src/commonTest/kotlin/pw/janyo/whatanime/`。所有新状态/策略文件在对应现有包内，不新增通用框架。

### 任务1：搜索状态与可测试请求接线

**文件：** 新增 `K/model/SearchState.kt`、`K/viewmodel/SearchSession.kt`、`K/model/SearchPreferences.kt`；修改 `K/viewmodel/MainViewModel.kt`、`K/repository/AnimationRepository.kt`、相关Koin模块；新增 `T/viewmodel/SearchSessionTest.kt` 和 `T/viewmodel/MainViewModelTest.kt`。

**接口：** `SearchPhase { Idle, Loading, Success, Empty, FilteredEmpty, Error }`；`SearchSession.state: StateFlow<SearchState>`；`begin(image: PlatformFile): Long?`、`complete(requestId: Long, image: PlatformFile, results: List<SearchAnimeResultItem>)`、`fail(requestId: Long, message: String)`、`setHideAdult(Boolean)`。同一session同步接纳请求并拒绝重入，原始结果只在session内部保存。MainViewModel调用这些接口，不维护第二套候选来源。

- [x] 写测试：成功A后开始B，断言image为B、results为空、phase=Loading；B空返回为Empty；全部adult返回为FilteredEmpty；B失败为Error且无A候选；begin第二次返回null；过时requestId提交无效；过滤切换即时重算。
- [x] 执行目标测试，确认未实现接口/行为导致红灯，记录失败原因。
- [x] 实现状态与requestId门控，再将真实搜索文件/网络路径接入；保留25MiB、缓存和历史查询，取消异常传播，错误本地化且不显示原始异常。通过共享偏好同步过滤。
- [x] 用可控Gateway、大小策略和协程测试覆盖重复提交、复制失败、25MiB边界、取消及重试；取消时清理Loading但不吞异常、不显示网络错误。版本目录添加与当前解析版本一致的 `kotlinx-coroutines-test:1.11.0`，不升级生产依赖。
- [x] 执行整个 `composeApp:testDebugUnitTest` 与Android编译；逐步迁移后已删除旧页面兼容状态。

### 任务2：设置、额度版本与敏感信息

**文件：** 新增 `K/model/QuotaState.kt`、`K/viewmodel/QuotaSession.kt`、`K/utils/DebugRedaction.kt`；修改 `K/viewmodel/SettingsViewModel.kt`、`K/module/NetworkModule.kt`；测试 `T/viewmodel/QuotaSessionTest.kt`、`T/utils/DebugRedactionTest.kt`。

**接口：** `QuotaPhase { NotLoaded, Loading, Ready, Error }`；`QuotaSession.begin(credentialChanged: Boolean): Long?`、`complete(requestId: Long, quota: SearchQuota)`、`fail(requestId: Long)`、只读state；`redactDebugResponse(body: String, apiKey: String): String`。SettingsViewModel成为唯一额度所有者，公开 `refreshQuota()` 并复用 `setCustomApiKey`。

- [x] 写失败测试：初始未知不等于Ready0/0；总量0不产生NaN；旧generation响应不覆盖新Key；同Key失败标记旧值；负数不能产生非法比例；响应包含假Key和URL token时输出不得包含它们。
- [x] 分别运行目标测试确认红灯，再实现上述状态/脱敏；Key保存和刷新失败分开，避免原始异常进入UI/日志。
- [x] 执行全部单测；调试缓存使用原子StateFlow保留最多5条，检查安全复制与关闭调试不新增响应。

### 任务3：历史/详情、缓存与恢复元数据

**文件：** 修改 `K/repository/AnimationRepository.kt`、`K/db/service/HistoryService.kt`、`K/db/service/HistoryServiceImpl.kt`、`K/db/dao/HistoryDao.kt`、History/Detail ViewModel；新增 `K/model/HistoryState.kt`、`K/utils/HistoryPolicy.kt`；测试 `T/utils/HistoryPolicyTest.kt` 及对应VM测试。

**接口：** `HistoryLookup(historyId: Int, cachePath: String, savedAt: Long, result: SearchAnimeResult)`，本地/在线查询返回或补充已有记录标识；`DeleteHistoryResult`明确DB失败/成功/缓存清理失败；`isVideoExpired(savedAt: Long, now: Long): Boolean`，严格超过600000ms才为过期。

- [ ] 写失败测试：600000ms不提前过期、600001ms过期；两次播放中时钟推进；详情缺失与JSON失败不混为Empty；共享规范缓存不能删除；旧记录禁止进入详情。
- [x] 实现DAO稳定倒序、受控目录与共享缓存检查、iOS重建、部分失败反馈；不改schema/缓存命名/去重算法。搜索缓存区间与删除共享事务锁，清理后确认文件消失。
- [x] 完成刷新/删除请求序列门控、取消传播和旧列表保留；详情携带时间/原图并监听过滤偏好。
- [ ] 全部单测与Android编译通过；核对KSP/schema无结构变化，测试记录覆盖数据兼容。

### 任务4：单一播放协调器与平台生命周期

**文件：** 新增 `K/viewmodel/PlaybackCoordinator.kt`、`K/model/PlaybackState.kt`；修改 `K/module/MediaModule.kt`、`K/ui/components/MediaPlayer.kt`、`K/ui/components/VideoDialog.kt` 及两端必要宿主适配；测试 `T/viewmodel/PlaybackCoordinatorTest.kt`。

**接口：** `play(url: String, savedAt: Long?)`、`close()`、`onBackground()`、`dispose()`；state包含会话ID和Closed/Loading/Playing/Paused/Ended/Error；`previewUrl(url: String): String`保留现有Token并正确加入size参数。

- [x] 先只读核实已安装播放器1.0.53的事件/暂停/清引用/销毁API；确认原生资源在Composable的onDispose释放，不假设host有release接口。
- [x] 写失败测试：已知过期不加载URL；无query/有query均正确拼接；关闭后迟到回调不重开；一次关闭释放一次拥有资源，不释放其他会话。
- [x] 实现媒体适配与协调器，App只挂一个Dialog；关闭/后台进入统一关闭流程，不自动继续播放；当前库没有结构化HTTP码，使用通用错误。
- [x] 单测与Android编译通过；Android模拟器确认本应用音轨从播放中到关闭/后台后移除，回前台不自动续播。此证据不等同物理扬声器及所有音频中断场景；iOS仍未验证。

### 任务5：根导航图、Tab与恢复

**文件：** 修改 `K/App.kt`、`K/ui/navigation/Nav.kt`、四个Screen参数与Koin owner接线；新增 `K/ui/navigation/NavigationPolicy.kt`；测试 `T/ui/navigation/NavigationPolicyTest.kt`，必要Android UI测试置于 `composeApp/src/androidInstrumentedTest/`。

**接口：** 类型安全根图 `RouteRoot`；`TopLevelDestination { Search, History, Settings }`；根图持有Main/History/Settings VM；Detail依historyId持有独立VM。导航与滚动由对应保存机制管理；搜索跨进程仅保存工作区存在标记，恢复时明确要求重新选图，不自动上传，也不承诺恢复完整候选。不保存PlatformFile、Key或Job。

- [x] 返回策略先红后绿并接入App返回判断；重复当前Tab直接返回，Detail/About采用singleTop，完整设备返回回归仍由后续步骤验收。
- [x] 使用已有Navigation/Koin版本实现根owner、singleTop/saveState/restoreState；各一级Screen无多余返回箭头，Detail/About覆盖底栏。
- [ ] 实测请求中切Tab和Activity重建，证明VM与滚动分别保留；进程重启不自动重放上传，通过已保存historyId恢复或诚实提示中断。已验证请求中切Tab、非零滚动返回，以及后台am kill后的Tab/详情恢复和搜索重新选图提示；真正的Activity重建仍待验证，窗口重排不能代替。
- [ ] Android编译与导航回归通过；iOS显式返回不依赖当前空BackHandler。Android已验证浮层→一级页面→Search→系统返回，以及矮屏大字号下Detail→History滚动恢复；iOS宿主/手势仍待验证。

### 任务6：Gemini布局与交互组件

**文件：** 新增 `K/ui/components/AdaptiveLayout.kt`、`SearchWorkspace.kt`；原图/状态组件合并在SearchWorkspace，QuotaCard保留在SettingsScreen，不为文件数拆分组件。修改Main/History/Detail/Settings/About Screen、SearchResultItem、VideoDialog；测试 `T/ui/components/AdaptiveLayoutTest.kt`。

**接口：** `layoutFor(widthDp: Float, heightDp: Float, fontScale: Float): LayoutSpec`产生导航载体/rail宽/双栏/卡片垂直/原图吸顶决策；真实组件使用此策略和实际测量约束，不硬编码屏幕像素。

- [ ] 写失败测试：320/360/600/840/896dp，479/480dp高度边界；784dp内容宽门槛；fontScale1/1.5/2；短高关闭吸顶/双栏但不删除导航。
- [x] 实现并通过策略测试；再接入真实BoxWithConstraints/Scaffold与UI规范的FAB、图卡、独立点击分区、6项菜单、骨架加载。
- [x] 完整迁移设置入口和额度/Key管理，保留所有原外链/赞助/调试/许可；图片缺失与各种业务状态逐个绑定。
- [ ] `composeApp:assembleDebug`；用安装到模拟器的真实App检查FAB与最后列表项不互相遮挡，不能只用策略单测作为布局证据。

### 任务7：三语、无障碍与运动偏好

**文件：** 三套 `composeResources/values*/strings.xml`、`K/ui/theme/`、`androidMain`/`iosMain`必要运动偏好actual；修改范围内集数/相对时间/错误/无障碍文案；相对时间测试置于 `T/utils/TimeUtilTest.kt`。

**接口：** 默认采用静态骨架、无可选导航位移或图片淡入、持续展开FAB，因此不新增平台运动监听。相对时间纯函数输出类别和数量，由UI选择三语资源；候选时间单位通过资源注入既有分解逻辑。平台实际无障碍能力仍须分别验证。

- [x] 写失败测试并实现分钟/小时/天边界、未来时间和候选时间单位注入；英文相对时间使用min/h/d缩写，保留一致数量参数。
- [ ] 三语同键同占位符，Key默认遮挡且非编辑态只显示设置状态；读屏聚焦与返回触发项符合UI规范。资源一致性、真实Key编辑遮罩与取消后重开为空已验证；TalkBack仅验证服务就绪及本应用窗口获得焦点，完整朗读/逐节点顺序/浮层焦点返回未闭环。
- [ ] 关闭可选微光/位移/图片渐变时保留静态状态反馈；两端实际能力分别验证。
- [ ] 全部单测、资源编译、debug构建通过；设备检查1.5/2倍字与TalkBack，iOS VoiceOver待任务8。

### 任务8：集成实测、Gemini交互审查与代码复核

**文件：** 仅必要修复及回归测试；所有审查与设备输出在系统临时目录。

- [x] 运行 `composeApp:testDebugUnitTest composeApp:assembleDebug`，执行依赖声明导出并确认生产声明没有额外变化。
- [ ] 按整体设计S/Q/H/D/V/P/L/N/K/E验收矩阵执行Android真实操作与窗口/字体/三语/主题测试；记录快照和具体环境。
- [ ] 在macOS/Xcode完成既有无签名iOS构建及宿主运行/VoiceOver/返回/播放检查；没有可用环境时标记阻塞与负责人，不以Android结果替代。
- [x] 冻结范围和快照，复用本批次Gemini设计会话（未参与代码实现）进行只读UI复核；本地化与删除文案修正后增量静态UI审查通过。此结论不覆盖未实测平台与完整运行路径，不等于整体验收。
- [x] 主会话复核请求竞态、异常/取消、生命周期、数据兼容、凭据、可测试性和重复实现；修复取消后Loading滞留等问题并补充回归，必要交互修复已交回Gemini复核。
- [x] 记录实际通过/未验证/阻塞项，更新设计状态；必要验收未完成不写整体完成，不推送/合并/发布。Android追加验证包含25MiB超限、断网额度旧值与重试、真实记录/缓存删除及取消不保存凭据；详尽矩阵和证据保留在系统临时目录。

## 进度与提交规则

详细执行账本与红绿证据在系统临时目录，不把任务流水写入正式设计。每个任务只有测试/编译/相关契约满足后才勾选完成；环境阻塞不能算通过。设计基线提交为 `42fd476`；后续代码保留未提交差异供审查，除非用户另行授权提交。
