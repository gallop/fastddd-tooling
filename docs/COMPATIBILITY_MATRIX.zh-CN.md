# FastDDD Tooling 兼容矩阵

[English](COMPATIBILITY_MATRIX.md) | **简体中文**

Tooling 自身版本与 Framework 版本独立演进。版本号相同只是当前认证组合，不是构建约束。

机器可读规范源：[`../fastddd-tooling/fastddd-tooling-api/src/main/resources/fastddd-compatibility-matrix.yaml`](../fastddd-tooling/fastddd-tooling-api/src/main/resources/fastddd-compatibility-matrix.yaml)。构建期由 `VersionGovernanceContractTest` 校验上游版本属性、Tooling 内部版本和当前认证元组。

## 当前认证组合

| Tooling | Framework | Java | Spring Boot | 状态 |
|---|---|---:|---|---|
| `1.0.2` | `1.0.0` | 21 | 3.5.16 | 已发布历史记录（2026-09-14 发布，2026-10-15 Tooling 1.0.5 发布时降格 released）：Tooling 1.0.2——逐表代码生成可编译性修复 D-2（DECIMAL/BigDecimal 缺 import、Swagger v2→v3 注解统一、ANEMIC+CQRS 缺 fast-ddd-application/MapStruct 依赖：pom 门控新增 richDependencies、rich 控制器随 enableCQRS 门控）+ 运行期装配修复 D-3/D-4（RICH MapperScan 注解过滤、混合形态双扫描）+ 可执行打包契约补齐（spring-boot-maven-plugin repackage）+ 真实生成编译与 MapperScan 边界门禁；验收链（导出→独立仓 clean verify→按生成 Dockerfile 契约 java -jar 启动→健康→创建—查询往返）在纯 RICH 与混合形态通过（更新/删除与符号链接专项未覆盖） |
| `1.0.1` | `1.0.0` | 21 | 3.5.16 | 已发布历史记录（released；2026-09-13 发布，2026-09-14 Tooling 1.0.2 发布时降格）：Tooling 1.0.1 补丁——TemplateRenderer 旧调用兜底、init-data.sql MySQL 1093 修复、单文件 API 文档补齐（含 doc 插件源码扫描 0 控制器缺陷修复）、文档布尔/UTF-8 编码/输出格式契约收紧、语言标识严格校验；Framework 保持 1.0.0 不变 |
| `1.0.0` | `1.0.0` | 21 | 3.5.16 | 首个 GA（内部 v1.0 正式版；2026-09-13 内部重发，取代已撤销的 2026-09-10 批次）——2026-09-13 Tooling 1.0.1 发布时降格 released；已发布兼容记录保留，不标失效 |
| `1.0.0-SNAPSHOT` | `1.0.2` | 21 | 3.5.16 | 开发线（POM 默认组合）：Tooling 快照消费 Framework 1.0.2（T8.55 起随 POM `fastddd-framework.version`；1.0.5 发布同步勘正本行——原行描述单元格错位至 1.0.3 行且 framework 轴漏更） |
| `1.0.5` | `1.0.2` | 21 | 3.5.16 | **已发布降格（2026-10-20 随 1.0.6 发布降格；2026-10-15 内部正式发布，双路径端到端验收链通过并经 Owner 授权升格——GA 工件含 framework 1.0.2 可解析闭包部署内部 Maven 仓，git 锚=tag `v1.0.5`）：安全查询模板反转转义修复——自定义安全五表路径（studio `SecuritySpiRenderer`）下模板 2 处 `WHERE u.id = …` 持反转转义形 `#${r"${userId}"}`（# 留 raw 串外）：渲染通过、生成项目带病交付，运行期 `#${userId}` 非 MyBatis `#{}` 绑定、`${userId}` 字符串替换后残留裸 `#`（MySQL 行注释）截断鉴权 SQL（先在 bf7eb2b；T8.63 push 前复审发现①）。两处改 raw 串完整包裹正形（=F-⑤ 同款）；新增五表 queryVars 直渲染回归测试两臂（12 键 Map 形=SecuritySpiRenderer 同款传参）。承接未发布的 1.0.3/1.0.4 候选全部内容。1.0.2 元组同日降格 released；未发布的 1.0.3/1.0.4 候选全部内容被本版承接发布（候选行随定稿移除）** |
| `1.0.6` | `1.0.2` | 21 | 3.5.16 | 已发布→current（2026-10-20 T8.83 断链修复批，内部正式发布 tag v1.0.6；验收=162 S3/S6 编译冒烟复跑断链归零）：安全 SPI 六模板 String 签名对齐（T8.81 S3 十错断链根修——getUserInfo/getPermissionsByUserId/getRolesByUserId 签名 Long→String〔框架 security-starter SPI 真值 String〕+mapper 调用点 Long.valueOf 适配〔Mapper 保持 Long〕+builder String.valueOf；六模板=RbacIdentity/RbacPermission/UserDetails/CustomIdentity/CustomPermission/multi-channel AppIdentityService）+MCB 双 pom 依赖补齐（T8.81 S6 十七错断链根修——common-shared 增 fast-ddd-crud/web、common-domain 增 fast-ddd-web，versionless parent dm 管版；anemic Controller/Entity 产物经 studio MCB 死配置路由恒落库模块）+渲染回归 8 测（正负锚）；批形=T8.82R 对抗核验收口版 |
| `1.0.6` | `1.1.0` | 21 | 3.5.16 | released（Framework 1.1.0 GA 窗登记，2026-10-24，T8.98 前置①；承接框架仓 matrix「GA 前经 Tooling 侧机读矩阵登记」门禁）：1.1.0=可选身份能力线（四个 `fast-ddd-identity-*` 模块+W2–W7 消费面成熟度）——W6 单轴化合流后单轴 GA（1.0.3 永久取消）。验收链=D0 delta 预扫四项+全量 surefire 双跑恒等 1061/0/0/10+consumer-tests 30 场景全绿（identity-*/security-* 必需面 skipped=0）+bom-revision-isolation 绿；13 核心模块 src/main 与 v1.0.2 字节一致+security-starter 5 文件身份桥面（M2 门禁已覆盖）；v3a `defaultValue` 提示词契约激活（Studio 消费链 T8.96R D14 坐实）。current 认证元组维持 `(1.0.6, 1.0.2)`——1.1.0 升格 current 属后续 Tooling 发版窗 |
| `1.0.7` | `1.1.3` | 21 | 3.5.16 | 候选（2026-11-14 Owner 拍板配套批；目标=首个 Maven Central 公开发布，对齐 Central 唯一在架 Framework 线）：配套实证=12 模块 reactor × fw 1.1.3 Central 直解〔BUILD SUCCESS，148/0/0/2〕+生成项目自动 pin 1.1.3 且 Central-only 编译；内容=公开发布元数据〔ECO-004 url/scm〕+README 开源三批+凭据扫描锚加固+内网叙事脱敏 |

ECO-003 已验证 Tooling 使用 `1.0.0-tooling-eco003-SNAPSHOT`、Framework 使用 `1.0.0-jdk21-SNAPSHOT` 时可成功构建，证明版本来源已解耦。

## 兼容规则

- Tooling 通过 `${fastddd-framework.version}` 选择上游 Framework，不得复用自身 `${revision}`；
- Tooling 内部模块统一使用本仓 `${revision}`；
- 稳定版 Tooling 只依赖已发布的 Framework GA/RC，不依赖相邻源码目录或本机被覆盖的 SNAPSHOT；
- 模板和生成结果必须记录目标 Framework 版本，不得把 Tooling 版本写成 Framework 版本；
- 数据库形态的生成项目必须在 pom 中显式携带连接池依赖：默认 HIKARI 渲染 `spring-boot-starter-jdbc`（版本经 Boot parent 受管），显式 Druid 渲染 `druid-spring-boot-3-starter` 并钉 `<druid.version>`（对齐 fastddd-bom 受管值）——不得依赖 fast-ddd-crud/event 等框架模块的偶然传递（C4 依赖边界整改后 crud 不再传递任何池实现；ECO-007A）；
- 无数据库形态（`databaseType=NONE`）不得被加入数据库运行栈（连接池依赖受 databaseType 守卫）；
- 生成配置的 `fastddd.monitor.sql.slow-query-threshold` 必须为纯数值毫秒字面量（框架 `SqlMonitoringProperties.slowQueryThreshold` 为 `long`，不接 Spring Duration 单位后缀，`1000ms` 会在绑定期抛 NumberFormatException；ECO-007A）；
- 新增认证组合必须同时更新 YAML、契约测试和发布说明。

## 发布前待办

公开项目 URL/SCM 建立后补齐 POM、README 和 issue 地址；ECO-005 在隔离 Maven 仓库中重放 Framework → Tooling 构建，ECO-007 再以 Framework GA 完成生成项目黄金测试。
