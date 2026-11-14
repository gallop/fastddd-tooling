# Tooling 1.0.1 修复批次（未发布）

日期：2026-09-13。当前验证轴为 Tooling `1.0.0-SNAPSHOT` / Framework `1.0.0`，不是正式 1.0.1 验收。未改版本号、兼容矩阵、Git tag 或既有发布仓，未提交、推送或发布。

## 修复范围

| 问题 | 本批处置 |
| --- | --- |
| 旧 TemplateRenderer 调用未传 msg 导致失败 | 保留已有未提交兜底修复及 7 项测试，本批回归通过；不覆盖调用方或共享字典 |
| MySQL 菜单初始化同表子查询触发 1093 | 父菜单先插入，四个子菜单通过 INSERT SELECT 引用真实父 ID；保留重复执行语义 |
| 单文件文档缺 api-doc-all.md.ftl | 补模板，并将索引指向实际聚合文档 |
| 文档 false 被显示为 deprecated/required | 按布尔值判断；同时修复空 summary、可选字段条件及 LocalDateTime 渲染 |
| 文档及 CodeGenMojo 使用系统默认编码 | 明确 UTF-8 写入，避免平台编码差异 |
| HTML/JSON 格式选项实际静默输出 Markdown | 输出目录创建前明确报错；本批不新增格式实现，中英文 README 同步行为变化 |
| 非语言标识 english/zhgarbage 被接受 | 限定 zh/en 或相应带连字符的区域标识；新增原始字典键集合一致性检查 |

## 本轮实际验证

证据根目录：`D:\usr\workspace_2026\.fastddd-ver-work\tooling-101-review`。

- 最终 `install -Pconsumer-tests`：12 个 reactor 项目全部成功。主测试集 118 项：116 执行、0 failures、0 errors、2 skipped；FastDoc 消费者单独计数 11/11，Invoker 1 场景成功。
- 两项跳过为 FastDocExportPathGuardTest 的 symlinkEscapeIsRejected、symlinkToInsideRootIsAccepted，未将跳过计为通过，也不宣称本轮覆盖了这两种环境能力。
- 真实 MySQL 8.0.37：中文、英文完整初始化脚本各 15 条语句，分别执行两遍；四轮均得到 5 菜单、4 个正确父子关联。不是仅执行前 10 条的部分脚本验证。
- MySQL 验证采用本轮编译的模板资源，非正式 1.0.1 JAR。专用实例只监听本机 33419，已关闭；保留 datadir 和日志，未连接业务数据库。
- Maven 缓存由旧发布构建缓存复制至本批 D 盘隔离目录，属于预热缓存，不冒充全冷仓验证。既有 r2 制品与缓存源只读。
- `final-install-consumers.log` 是最终运行；`reports/` 按模块路径归档 XML，消费者与 reactor 分账。`mysql-probe.log` 记录四轮真实库结果。
- 初轮命令参数解析失败、模板条件语法导致的针对性测试失败分别保留于 `tests.log`、`tests-2.log`；修复后针对性验证见 `tests-3.log`。不以失败轮或不同轮 XML 累加最终总数。

## 发布边界与后续

1. 本批是源码修复与开发轴回归，不替代正式 1.0.1 坐标、发布材料、独立仓消费及跨仓生成运行门禁。
2. 建议仅 Tooling 发 1.0.1，Framework 保持 1.0.0；正式版本矩阵按既有流程登记，不能跳过治理测试。
3. Studio 跨仓运行、本轮候选生成应用启动及 MySQL 之外的数据库方言未在本批重验；不继承为新制品已通过。
4. 文档模块休眠的 spring.factories 自动配置、历史 @since、默认生成 groupId、生成 CLAUDE.md 及弃用注解未顺手改动；其中自动配置激活会改变装配范围，应独立评审，不混入补丁。
5. 本批没有做全量安全审计，不将已确认问题修复等同于 Tooling 不再存在其他缺陷。旧 Gitea 标签同步仍须另行授权处理。

## 2026-09-13 候选验收追加：doc 插件源码扫描存量缺陷（已修复）

候选验收（任务 4d）以候选制品运行 `fastddd-doc-maven-plugin:1.0.1:generate` 时发现：对含 2 个
`@RestController` 的输入工程生成 `Total Controllers: 0`，单文件 API 文档为空壳。根因不在本批
改动——`AnnotationParser.parseFile` 一直使用 `JavaParser.parse(String)`，`CompilationUnit` 无
storage，`getPrimaryType()` 恒空，`isRestController()` 恒 false（对照实验：`parse(Path)` 正常）。
该缺陷继承自 1.0.0，此前测试全部落在模型层，未覆盖"源文件能被扫出"这一层，故本批评审亦未发现。

处置（最小修复，不扩展功能）：`parseFile` 改经 `parse(Path)`（显式 UTF-8）；新增
`SourceScanContractTest` 钉住源文件→扫描器→模型链路并断言 boolean 标志语义；红灯验证（还原缺陷态
测试失败、恢复修复通过）；`fastddd-doc-generator` 1.0.1 重建重部署（模块 5/5 通过）。候选验收证据：
`.fastddd-releases/internal-tooling-1.0.1-candidate/evidence/verify-4d/`（含缺陷首跑日志与修复后
zh/en 双语验证）。
