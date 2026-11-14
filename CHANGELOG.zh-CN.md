# Changelog

[English](CHANGELOG.md) | **简体中文**

本文件记录 FastDDD 所有显著变更，格式遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

完整版本基线与兼容线见 [docs/COMPATIBILITY_MATRIX.zh-CN.md](docs/COMPATIBILITY_MATRIX.zh-CN.md)；
业务系统升级步骤以所用 Framework Community 版本的升级指南为准。

---

## [1.0.7] - 2026-11-14

首个 Maven Central 公开发布候选，配套 Framework 1.1.3（Central 当前唯一在架 Framework 线）。
配套实证：全 reactor 以 Central 直解的 framework 1.1.3 构建（12/12 模块 BUILD SUCCESS、
148/0/0/2 测试），生成项目自动 pin 1.1.3 且 Central-only 编译通过；唯一红测为版本治理契约
对「本登记」的拦截要求。

### Added
- 公开发布元数据：root POM 补 `<url>` + `<scm>`（兑现 ECO-004 占位；Sonatype Central 校验前置）——flattened POM 全 reactor 携带。
- 兼容矩阵候选元组 `(tooling 1.0.7, framework 1.1.3)`。

### Changed
- README（双语）对齐发布现实：版本/状态、快速开始按 Central 发布事实改写、新增功能特性节、
  生成项目结构示例（真实渲染产物）、`generate-code` / `generate-doc` 演示。

### Security
- `CredentialScanTest` AI Key 锚拆分为编译期常量折叠段（检测语义不变——变异验证）；开源前对
  CHANGELOG 与兼容矩阵叙事中的内部仓库地址做脱敏。

## [1.0.6] - 2026-10-20

内部正式发布（2026-10-20，T8.83 断链修复批，tag v1.0.6；验收链=162 活栈 S3 复跑 exit0/56 类零错〔F2' 十错断链归零〕+S6 复跑 exit0/46 类全 7 模块 reactor 绿〔F3' 十七错断链归零〕+发版前置复审通过）。

### Fixed

- **安全 SPI 签名漂移**（F2'，六模板；T8.81 S3 编译冒烟十错断链）：框架 security-starter
  SPI 为 String 真值（`getUserInfo(String)`/`getPermissionsByUserId(String)`/
  `getRolesByUserId(String)`/`SecurityUserDetails.userId`/`SecurityUserInfo.id`），而六模板持
  Long 签名/直传/强转形——渲染通过、每个生成项目编译必炸。按 Demo 典范形修复（String 签名+
  mapper 调用点 `Long.valueOf(userId)` 适配〔Mapper 保持 Long〕+builder `String.valueOf(...)`），
  覆盖 `RbacIdentityServiceImpl`/`RbacPermissionServiceImpl`/`UserDetailsServiceImpl`
  （`getUserAuthorities(String)`）/`CustomIdentityServiceImpl`/`CustomPermissionServiceImpl`/
  multi-channel `AppIdentityService`（并剥两处 `(Long)` 强转；MCB 每子应用必炸的 `@Override`
  编译错=T8.82R 联动盲区闭环）。`SecurityQueryMapper` 与 `Sys*Mapper` 模板保持 Long
  （模板自产接口，出界声明不动面）。
- **MCB 库模块依赖缺席**（F3'，双 pom；T8.81 S6 十七错断链）：anemic 五件套产物
  （Controller/Entity/…）经 studio MCB 死配置路由恒落库模块，而 `common-shared-pom` 仅
  common+model+lombok、`common-domain-pom` 无 web——`fast-ddd-web`/`fast-ddd-crud` 类型
  （spring-web/validation/springdoc）恒不可解析。双 pom 补齐 fast-ddd-web（shared 另补
  crud），versionless 由 parent dependencyManagement 管版。

### Added

- `SecuritySpiSignatureTemplatesRenderTest`（八臂）：六模板+双 pom 直渲染回归——正锚
  （String 签名/`Long.valueOf` 适配点/`String.valueOf` builder/pom 依赖块）+负锚
  （`(Long userId)` 残影/未适配 `ByUserId(userId)` 直传/`(Long) user.getId()` 强转/
  `<version>` 泄漏/FreeMarker 痕迹归零）。

## [1.0.5] - 2026-10-15

内部正式发布（2026-10-15，Owner「请发布，并push」授权；GA 工件含 framework 1.0.2 可解析闭包
部署内部 Maven 仓（地址略）；git 锚=tag `v1.0.5`）。当前
认证元组升格为 `tooling 1.0.5 / framework 1.0.2`——1.0.2 元组降格 released。承接未发布的
1.0.3 与 1.0.4 候选全部内容（发布即吸收，候选矩阵行随定稿移除）。

### 修复

- **SecurityQueryMapper 反转转义**（F-⑥，T8.64）：自定义安全五表路径（studio
  `SecuritySpiRenderer`；USER/ROLE/PERMISSION/USER_ROLE/ROLE_PERMISSION 五表齐备）下，生成器以
  12 键 queryVars Map 渲染 `project/security/SecurityQueryMapper.java.ftl`——模板两处
  `WHERE u.id = …` 持**反转转义形** `#${r"${userId}"}`（`#` 留在 raw 串外）：渲染通过、生成项目
  带病交付，运行期才引爆——`#${userId}` 非 MyBatis `#{}` 绑定，`${userId}` 走字符串替换，残留
  裸 `#`（MySQL 行注释）首次鉴权查询即截断 SQL。先在缺陷（bf7eb2b 初始提交即有）；T8.63 push
  前独立复审发现①。两处改 raw 串完整包裹正形 `${r"#{userId}"}`（=F-⑤ 同款转义形）。

### 新增

- **测试**：`SecurityQueryMapperTemplatesRenderTest`——以 `SecuritySpiRenderer` 同款 12 键
  queryVars Map 直渲染模板，断言 MyBatis 绑定占位符字面输出（×2）、反转形 `#${` 零残留、
  12 变量全数替换入 SQL。

### 变更

- `fastddd-compatibility-matrix.yaml`+`docs/COMPATIBILITY_MATRIX(.zh-CN).md`：追加
  `(tooling 1.0.5, framework 1.0.2)` 候选元组；历史行不动；当前认证元组 `(1.0.2, 1.0.0)` 不变。

## [1.0.4] - 2026-10-14

候选构建（未发布——仅本地 install/测试态，兼容矩阵登记为 `candidate` 候选元组；当前认证元组
维持 `tooling 1.0.2 / framework 1.0.0`；升格须经验收链并经 Owner 授权）。承接未发布的
1.0.3 候选全部内容。

### 修复

- **安全 Mapper 模板渲染崩溃**（F-⑤，T8.63）：Security 勾选且设计**无** RBAC 正名表时，
  生成器**首次真渲染** `project/security/Sys*Mapper.java.ftl`（existing-contains skip 从未触发）——
  FreeMarker 2.3.32 将 `@Select` SQL 内的 MyBatis `#{param}` 占位解析为数值插值，抛
  `InvalidReferenceException`（`Failed at: #{username}`），生成任务 FAILED（162 铁证：task
  2110182776456187906）。三模板全部 15 处占位（`SysUserMapper` 6/`SysRoleMapper` 4/
  `SysPermissionMapper` 5）改 raw 字符串插值形 `${r"#{param}"}`，占位字面输出。

### 新增

- **测试**：`SecurityMapperTemplatesRenderTest`——以「无 table 元数据」上下文（=无 RBAC 正名表
  形态）渲染三模板，断言 MyBatis 占位字面输出+逐文件 multiplicity。历史 IT 恒以五张 RBAC
  正名表走 skip 臂，模板从未真渲染。

### 变更

- `fastddd-compatibility-matrix.yaml`+`docs/COMPATIBILITY_MATRIX(.zh-CN).md`：追加候选元组
  `(tooling 1.0.4, framework 1.0.2)`（status `candidate`，未发布——仅供候选验收链使用，沿
  2026-09-13 候选治理先例）；未发布的 1.0.3 候选被本候选完整承接。当前认证元组不变。

## [1.0.3] - 2026-10-12

候选构建（未发布——仅本地 install/测试态，兼容矩阵登记为 `candidate` 候选元组；当前认证元组
维持 `tooling 1.0.2 / framework 1.0.0`；升格须经验收链并经 Owner 授权）。

### 新增

- **语义扩列模板标注**（P2-2）：`rich/AggregateRoot.ftl` 类 javadoc 注入聚合名与关系摘要，
  `anemic/Entity.ftl` 注入 DDD 角色——消费 Framework 1.0.2 新增的三个可空 `TableMetadata` 列
  （`aggregateName`/`dddRole`/`relationsSummary`）。注入采用真值守卫形 `<#if x?? && x?has_content>`（FreeMarker 无裸字符串真值——`&&` 右操作数须布尔）——
  列缺席/null 时降级为不注入，对 1.0.2 之前框架 jar（属性缺失）渲染不炸；无守卫裸插值会抛异常。
  标签走 i18n 文案字典（`messages_zh.properties`/`messages_en.properties` 新键：
  `rich.aggregate.root.aggregateNamePrefix`、`rich.aggregate.root.relationsSummaryPrefix`、
  `anemic.entity.dddRolePrefix`）。
- **测试**：两个编译宿主（`GeneratedRichCodeCompileTest`/`GeneratedAnemicCodeCompileTest`）各加
  有值臂（fixture 值锚）与空值臂（守卫降级）门禁；`TemplateRendererTest` 加属性缺失窗口门禁
  （LinkedHashMap table 不含新键——新模板×旧框架）。

### 变更

- 根 POM `fastddd-framework.version` 1.0.0 → 1.0.2（Framework 1.0.2 本地 install 测试态；Tooling 自身
  `revision` 不动——1.0.3 经 `-Drevision=1.0.3` 构建）。
- `fastddd-compatibility-matrix.yaml`：追加候选元组 `(tooling 1.0.3, framework 1.0.2)`
  （status `candidate`，未发布——仅供候选验收链使用，沿 2026-09-13 候选治理先例）；开发线元组
  framework 轴随 POM 同步至 1.0.2；追加 staged 状态的 `1.0.2` framework 线 manifest 行
  （current-stable 仍留 1.0.0）。当前认证元组不变。

## [1.0.2] - 2026-09-14

仅 Tooling 的补丁版本，2026-09-14 内部正式发布（兼容矩阵当前认证元组
`studio 1.0.0-jdk21-SNAPSHOT / tooling 1.0.2 / framework 1.0.0`，状态 `current`；
Framework 保持 1.0.0，1.0.1 元组降格 `released` 作为已发布历史记录保留）。

修复范围：首次真实 Studio 导出（导出仓 `mvn clean verify`）暴露的逐表代码生成
可编译性缺陷（D-2），以及首次对导出项目真实 `java -jar` 启动暴露的运行期装配
缺陷（D-3/D-4）与可执行打包契约缺口（验收约定包含启动、健康与创建—查询往返，
不接受仅编译通过；更新/删除与符号链接专项不在本批验收范围）。同一排查发现的
H2 元数据 catalog 缺陷（D-1）属 Studio 侧
（连接 catalog/schema 解析）修复，不在 Tooling 制品变更内。修正后的影响面：充血通道
因 Swagger 注解缺陷无条件编译失败，DECIMAL 列叠加 BigDecimal 缺 import、pom 按
ANEMIC 渲染时叠加缺 application/MapStruct 依赖；贫血通道仅在含 DECIMAL 列时因
缺 import 编译失败。并非"任何配置组合均失败"。

### Fixed（修复）

- **`java.math.BigDecimal` 缺 import（D-2a）**：全模板无 BigDecimal import 分支，
  任何 DECIMAL 列（`BigDecimal` 字段）在两条通道均产出不可编译代码。现检测循环
  依据实际列类型与 `useJavaTime` 一同推导 `useBigDecimal`（充血 `AggregateRoot`/
  `po` 此前连 `java.time` 检测也没有——DATETIME 列同类潜伏缺陷，同一机制一并修复），
  覆盖充血 `AggregateRoot`/`po`/`Command` 与贫血 `Entity`/`DTO`/`CreateRequest`/
  `UpdateRequest`。
- **Swagger v2/v3 注解不一致（D-2b）**：`AggregateRoot.ftl` 使用 springfox 时代
  v2 `@ApiModel`/`@ApiModelProperty`（依赖与 import 均不存在——springdoc 迁移半途
  而废），其余模板均为 v3 `@Schema`。充血通道因此 在 `useSwagger=true` 下无条件
  编译失败。已统一为 v3 `@Schema`。
- **ANEMIC 项目产出 RICH 产物缺依赖（D-2c）**：逐表生成按表解析 `modelType`
  （手动覆盖 > AI 持久化结论 > 分析器 > 请求默认值），ANEMIC 项目可合法包含 RICH
  产物——但 `pom.xml.ftl` 的 `fast-ddd-application`、MapStruct 与注解处理器链仅按
  项目级 `modelType == "RICH"` 门控，RICH 产物既无依赖也无其调用的 MapStruct 处理器
  （生成 RepositoryImpl 调用 `*Mapper.toPo(entity)`）。pom 门控现额外接受生成通道
  在任一表解析为 RICH 时注入的 `richDependencies` 自定义属性；插件
  `<configuration>` 块仍只按项目级 modelType 门控，再运行语义不漂移。Studio 单模块
  流程对 RICH 表强制 `enableCQRS=true`，Command/Query 产物与控制器在该处自洽；
  插件/裸 API 通道 `enableCQRS=false` 的缺陷见下条。
- **关闭 CQRS 仍生成充血控制器（D-2c 同源）**：`CommandController`/`QueryController`
  无条件生成，而其引用的 `Command`/`Query`/`Handler` 仅在 `enableCQRS=true` 时生成
  ——关闭 CQRS 即产出引用缺失类的控制器。两控制器现与输入产物同随 `enableCQRS`
  门控。
- **生成的 RICH 应用无法启动——Mapper 代理双 bean（D-3，2026-09-13 运行期批）**：
  `Application.java.ftl` RICH 分支根包裸 `@MapperScan`，ClassPathMapperScanner 把
  根包下所有接口（含由 `@Repository` 实现类承载的领域仓储接口、MapStruct 转换器）
  都注册为 MyBatis 代理，与既有 Spring bean 同型冲突（`expected single matching
  bean but found 2`），`APPLICATION FAILED TO START`。RICH 分支现改为根包扫描 +
  `annotationClass = org.apache.ibatis.annotations.Mapper.class`（FQN 过滤同时排除
  `org.mapstruct.Mapper` 转换器）；贫血分支不得加过滤——贫血 Mapper（BaseRepository
  子接口）不带 `@Mapper`，依赖专属 `mapperPackage` 包扫描注册。
- **混合形态应用无法启动——RICH `*BaseMapper` 未注册（D-4，同批）**：逐表解析出
  RICH 产物而项目级 ANEMIC（`richDependencies`）时，`*BaseMapper` 带 `@Mapper` 但
  位于 `<root>.infrastructure.persistence`，不在贫血 `mapperPackage` 扫描范围内——
  生成的 `*RepositoryImpl` 注入失败（`No qualifying bean of type '…*BaseMapper'`，
  修复前已复现取证）。该形态现渲染双 `@MapperScan`（mybatis-spring 2.0.2 起
  `@Repeatable`）：`mapperPackage` 包扫描（贫血 Mapper）+ 根包注解过滤扫描（RICH
  基础 Mapper）；两扫描按构造不相交。
- **可执行打包契约缺口（同批）**：生成 Dockerfile 以 `java -jar target/*.jar` 运行、
  README 承诺 `mvn spring-boot:run`，但 pom 从未声明 `spring-boot-maven-plugin`——
  `package` 产物为无 Main-Class 的平装 jar（`java -jar` 报"没有主清单属性"）。pom
  现声明该插件 + `repackage` execution，版本不声明（继承
  `spring-boot-starter-parent` pluginManagement，不引入独立第三方版本）；平装 jar
  保留为 `*.jar.original`。

### Added（新增）

- **真实生成编译门禁（入仓）**：`GeneratedRichCodeCompileTest` /
  `GeneratedAnemicCodeCompileTest` 将真实 javac 门禁扩展到失败形态——两通道
  DECIMAL/DATETIME 列 + `useSwagger(true)`，以及 RICH + `enableCQRS=false`；
  `ProjectPomRichDependenciesTest` 钉住 pom 门控契约（ANEMIC 不携带 rich 依赖；
  ANEMIC + `richDependencies` 携带依赖而插件 configuration 仍按项目级；RICH 保持
  旧行为）与可执行打包契约（Boot 插件 + `repackage`、版本不声明、两通道一致）。
  既有门禁对三项缺陷全盲（无 Swagger 轴、无 DECIMAL 列、编译针对测试
  类路径而非生成的 pom）。
- **MapperScan 边界契约门禁**：`ApplicationTemplateMapperScanBoundaryTest` 钉住
  三分支扫描契约——贫血（专属包、无注解过滤）、RICH（根包 + ibatis `@Mapper` FQN
  过滤）、混合（双 `@MapperScan`）——同时锁定 D-3（根包裸扫描不得回归）与 D-4
  （混合形态必须双边界扫描）。运行级证明（真实导出项目启动/健康/创建—查询往返；
  更新/删除与符号链接专项未覆盖）在 Studio 侧导出门禁（`ExportMavenVerifyGateTest`）。

[Unreleased]: https://github.com/gallop/fastddd-tooling/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/gallop/fastddd-tooling/compare/v1.0.1...v1.0.2

---

## [1.0.1] - 2026-09-13

仅 Tooling 的补丁版本；Framework 保持 1.0.0（兼容矩阵已登记当前认证元组
`studio 1.0.0-jdk21-SNAPSHOT / tooling 1.0.1 / framework 1.0.0`；1.0.0 GA 元组
作为已发布历史记录保留）。
修复评审与验证范围见 `docs/TOOLING_1_0_1_FIX_REVIEW.md`。

### Fixed（修复）

- **`doc-maven-plugin` 源码扫描恒为 0 控制器**：`AnnotationParser.parseFile` 经
  `JavaParser.parse(String)` 解析，`CompilationUnit` 无文件 storage，`getPrimaryType()`
  恒为空——`isRestController()` 恒 false，`generate` 目标对任意输入只产出空壳文档
  （继承自 1.0.0 的存量缺陷；该缺陷使本批新增的单文件 API 文档实际不可用）。
  现改为经 `parse(Path)` 解析（显式 UTF-8），并新增源码扫描契约测试
  （`SourceScanContractTest`）钉住"源文件 → 扫描器 → 模型"链路——此前模型层测试
  未覆盖这一层，缺陷因此漏网。
- **`TemplateRenderer` 旧调用约定**：在 1.0.0 制品上，不注入字典直接
  `new TemplateRenderer(configuration).render(template, variables)` 会因
  `InvalidReferenceException: msg` 失败。现渲染时兜底注入默认中文字典的**局部副本**——
  不覆盖调用方显式传入或入口层注入（zh/en/自定义）的 `msg`，亦不回写共享
  `Configuration`。
- **`init-data.sql` 菜单种子在 MySQL 8 上的缺陷**：多行 `sys_menu` INSERT 内嵌同表
  SELECT 子查询，被 MySQL 拒绝（ERROR 1093）。现父菜单先行插入，四个子菜单经
  `INSERT ... SELECT` 引用真实父 id；MySQL 的 `ON DUPLICATE KEY` 重复执行语义保留。
- **文档布尔标记**：`deprecated`/`required` 为 `false` 时仍被渲染为"已废弃/必填"
  （`??` 判空将 `false` 视为存在）。模板改为按布尔值判断；同一批修复空 `summary`、
  可选字段条件与 `generatedAt`（LocalDateTime）渲染。

### Added（新增）

- **单文件 API 文档**：补齐缺失的 `api-doc-all.md.ftl` 聚合模板；`singleFile=true` 时
  生成 `API-Documentation.md`，索引指向实际文件而非各控制器文件。

### Changed（变更）

- **输出编码显式 UTF-8**：`DocGenerator` 与 `CodeGenMojo` 的文件写入不再使用平台
  默认编码。
- **HTML/JSON 输出格式快速失败**：在创建输出目录之前明确报错，不再静默生成
  Markdown（行为变化已在中英文插件 README 说明；本批不新增格式实现）。
- **语言标识严格校验**：仅接受 `zh`/`en` 及带连字符/下划线的区域变体
  （`zh-CN`、`en_US` 等）；`english`/`zhgarbage` 一类字符串被拒绝。
  测试套件新增中英原始字典键集合一致性检查。

---

## [1.0.0] - 2026-09-13

首个正式发布版本基线：**JDK 21 + Spring Boot 3.5.16**。
本版本由 `JDK21_SPRING_BOOT_3_5_UPGRADE_PLAN.md` 的 UPG-000～UPG-010 驱动，
从 JDK 17 / Boot 3.4.5 开发快照确定性升级而来。

> **重发说明（2026-09-13，Owner 决策）。** 内部 Tooling 1.0.0 交付（tag 于 2026-09-11 重打）
> 撤销并在原版本位随 Framework 1.0.0 一并重发：前置核实确认旧制品无任何消费者（两个试点项目
> 消费的是 `com.gallop.fastddd:1.0.0-jdk21-rc1`，并非本 GA）、从未部署、从未对外交付。旧 tag
> 对象与制品字节已带 SHA-256 指纹归档于 `.fastddd-releases/internal-v1.0.0-r2/evidence/
> withdrawn-internal-v1.0.0/`；旧内部 release 仓隔离保留并标记撤销。未发布的 `1.0.0-jdk21`
> 版本计划撤销——正式版本统一为 `1.0.0`（Tooling 与 Framework 版本轴仍独立）；历史 rc1 记录不变。

### Added（新增）

- **CI-friendly 版本治理**：引入 `${revision}` 属性作为 reactor 唯一版本入口，27 个模块版本统一受其驱动；
  配合 `flatten-maven-plugin`（`resolveCiFriendliesOnly`）在发布 POM 中把 `${revision}` 解析为字面量，
  满足"版本只从一个属性进入 reactor"且"发布 POM 版本已解析"。
- **`fastddd-bom` 统一依赖契约模块**：业务项目只需 `import` 该 BOM 即获得 FastDDD 全部模块与推荐第三方
  依赖的一致版本，无需各自声明版本。BOM 为独立 POM，发布产物自洽（无环境属性泄漏）。
- **Maven Enforcer 构建门禁**：`requireJavaVersion[21,)`、`requireMavenVersion[3.9.0,4.0.0)`、
  `requirePluginVersions`（所有插件版本须显式）、`banDuplicatePomDependencyVersions`。
- **JDK 21 编译基线**：根 pom `java.version=21` + `maven.compiler.release`，产物 class major version 65。
- **Spring Boot 3.5.16 + 依赖确定性对齐**：Spring AI 1.1.8、SpringDoc 2.8.17、Knife4j 4.5.0、
  MyBatis-Plus 3.5.12、MyBatis-Spring 3.0.4、Druid `druid-spring-boot-3-starter` 1.2.28、
  mysql-connector-j 9.7.0、Micrometer 1.15.12（消除旧版分裂）、Jackson 2.21.x（Boot 管理）。
- **PMD 7**：`maven-pmd-plugin` 升至 3.24.0（PMD 7.3.0），支持 `targetJdk=21`。
- **Maven Wrapper**：`./mvnw` pin Maven 3.9.5，CI 与本地 Maven 版本统一。
- **Docker JDK 21**：构建阶段 `maven:3.9.9-eclipse-temurin-21`，运行阶段 `eclipse-temurin:21-jre-alpine`。
- **版本治理文档**：新增 `docs/COMPATIBILITY_MATRIX.md`、`docs/UPGRADE_GUIDE.md`、`docs/RELEASE_CHECKLIST.md`。
- **模板文案国际化（L4）**：生成代码/文档文案字典化（`fastddd-templates` 的 `messages/messages_{zh,en}.properties`，538 键；`zh` 输出与历史结果逐字节一致），经 FreeMarker 共享变量 `msg` 注入，模板以 `${msg.*}` 引用。新增语言参数：`init.language`（`init-project`）、`codegen.language`（`generate-code`）、`fastdoc.language`（doc 插件 `generate` 目标）——均默认 `zh`；`en` 将生成注释、校验消息、种子数据与 UI 文案切换为英文，不改变代码结构。

### Changed（变更）

- Spring Boot 3.4.5 → **3.5.16**；JDK 17 → **JDK 21**（编译与运行时）。
- Druid 坐标迁移：`druid-spring-boot-starter`（Boot 2 坐标）→ **`druid-spring-boot-3-starter`**（4 处引用）。
- MySQL 驱动迁移：`mysql:mysql-connector-java` → **`com.mysql:mysql-connector-j`** 9.7.0（6 处引用）。
- 清理手工版本属性（mysql-connector / micrometer / lombok / freemarker / junit / testcontainers），
  统一交 Spring Boot BOM 管理，消除版本分裂。
- `application-dev.yml` 修复重复 `fastddd:` 顶层键（Boot 3.5 SnakeYAML 严格解析拒绝重复键）。
- 兼容矩阵：撤回未发布的 `1.0.0-jdk21` 登记——预发布认证元组与 `1.0.0-jdk21-SNAPSHOT` 开发线随版本计划撤销移除；POM 默认组合回落 Tooling `1.0.0-SNAPSHOT` 消费已发布 Framework `1.0.0` GA。

### Fixed（修复）

- **B-01**：`PerformanceIntegrationTest.testBatchInsertPerformance` 因"逻辑删除→重插相同 username"撞
  `username` UNIQUE 而失败；改用注入 `JdbcTemplate` 的物理 `DELETE` 清理。全量门禁 356 tests / 0 failures。
- `fast-ddd-examples/pom.xml` 预先存在的 `<relativePath>` 错误（`../../pom.xml` → `../pom.xml`）。
- `fast-ddd-web/logback-spring.xml` 两处被 logstash-encoder 忽略的无效 `<includeStackTrace>` 属性清理。
- `project/README.md.ftl`：转义反引号代码围栏在生成的 README 中输出字面反斜杠；改为真实 Markdown 围栏（可见行为变化：4 行围栏与此前生成的项目存在差异）。
- **生成项目信息泄露修复**：`project/AuthController.java.ftl`（刷新令牌 catch 块）与 `project/GlobalExceptionHandler.java.ftl`（`RuntimeException` 500 处理器）把原始 `e.getMessage()` 拼进客户端响应。重发复核确认：当前 init-project / generate-code / archetype 生成链均不渲染这两个模板（无激活消费方），缺陷位于模板源头，凡经采用或参照该模板建项的项目（如试点的人工建项流程）即被继承。两处已在模板源改为返回固定字典文案，完整异常仍进日志；`IllegalArgumentException` / `ConstraintViolationException` 的 400 处理器按设计保留消息透传——二者本就是面向调用方的校验类消息。

### Known Limitations / 遗留（不阻塞 1.0.0）

- **CI runner 未部署**：远程为内网 Gitea，`.github/workflows/*.yml` 为声明式文件、无 runner 执行；
  JDK 21 / wrapper / `./mvnw` / PMD7 改动属"CI 就绪即生效"基线，待 runner 部署后验证。
- **`checkstyle.xml` 规则文件缺失**：`configLocation=checkstyle.xml` 但仓库无此文件，
  code-quality 步骤带 `continue-on-error` 容错；补规则文件留独立任务。
- **外部基础设施运行时冒烟待补**：Redis、Elasticsearch、RocketMQ 分布式链路、Docker 构建启动健康检查
  在本机无对应运行时，仅做编译/上下文级验证；待部署环境或 Docker 就绪后补 smoke。
- **`SecurityIntegrationTest` 类级 `@Disabled`**：自引用 `@SpringBootTest(classes=自身)` 上下文加载问题，
  既有测试债（非本次升级引入、非为过门禁新加）。
- **Mockito self-attach 警告**：JDK 21 下 inline mock 自附加（全绿、覆盖率正常），
  显式 mockito agent 与 JaCoCo argLine 共存留 JDK 25+ 迁移时配置。
- **BOM 与根 pom 依赖清单重复**：本次为降低 reactor 解析风险，根 pom `<dependencyManagement>` 保持不变，
  BOM 独立镜像清单。登记为已知技术债，1.0.1 由根 pom `import` BOM 收敛（独立验证 reactor 解析）。

### 1.0.1 路线（已登记必做项）

- **二进制兼容性校验（japicmp / Revapi）**：1.0.0 无 GA 基线 jar，japicmp 无可比基准；
  建立首个 GA 基线后，1.0.1 起对每次发布做 API 兼容性校验。
- **根 pom 委托 BOM**：根 pom `<dependencyManagement>` 改为 `import` `fastddd-bom`，实现依赖版本单一来源。
- **生成模板切 BOM**：archetype-resources / FreeMarker 生成项目模板默认 `import` `fastddd-bom`，
  让生成的业务项目开箱即用统一依赖契约。

---

## 升级前开发快照（归档，不维护）

| 项 | 值 |
|----|----|
| Java | 17 |
| Spring Boot | 3.4.5 |
| FastDDD 版本字符串 | `1.0.0-SNAPSHOT` |
| 状态 | 仅 Git tag `baseline/jdk17-boot3.4.5` 归档，不再维护 |

> 该快照不作为任何兼容线对外承诺。所有在研业务系统应随 1.0.0 一起升级到 JDK 21 / Boot 3.5.16 基线
> （业务升级步骤见对应 Framework Community 版本的升级指南与 UPG-011 历史记录）。
