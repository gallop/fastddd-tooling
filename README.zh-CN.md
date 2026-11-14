# FastDDD Tooling Community

![Version](https://img.shields.io/badge/version-1.0.7-blue) ![JDK](https://img.shields.io/badge/JDK-21-orange) ![License](https://img.shields.io/badge/license-Apache--2.0-green)

[English](README.md) | **简体中文**

FastDDD Tooling Community 是 FastDDD 的开发期工具链，提供稳定 Tooling API、代码与文档生成引擎、模板，以及项目初始化与代码生成 Maven 插件。它依赖 FastDDD Framework Community，但不依赖 FastDDD Studio Pro。

> **当前状态：1.0.7 GA**（2026-11-15，tag `v1.0.7`——首个 Maven Central 公开发布）。Tooling 自身版本与 Framework 版本独立演进，兼容组合以仓内兼容矩阵为准。本版认证 Framework 配对为 **1.1.3**（已发布 Maven Central）。

## 功能特性

- **一条命令生成 DDD 脚手架** —— `init-project` 渲染可运行的 Spring Boot 工程；RICH 模式按限界上下文铺设四层结构（interfaces / application / domain / infrastructure），ANEMIC 模式生成 PO + Service + Controller。
- **多上下文感知** —— `-Dinit.contexts=order,product` 为每个上下文生成聚合根、Repository、Mapper 与 Controller；跨上下文依赖约束在领域层。
- **安全脚手架（可选）** —— JWT 登录链、Security SPI 骨架（`AppIdentityService`）与 Vue 3 登录页套件（登录页、token 工具、用户存储）。
- **可选能力模块** —— event / cache / search / monitor 按开关注入。
- **数据库驱动代码生成** —— `generate-code` 从活库表结构逆向生成 CRUD 各层。
- **数据库文档生成** —— `generate-doc`（FastDoc）为活库表结构生成文档；也可经 `fastddd-doc-maven-plugin` 绑入构建。
- **双语脚手架** —— `-Dinit.language=zh|en` 切换生成物的注释、消息、README 与 UI 文案，不影响代码结构。

## 模块

| 模块 | 定位 |
|---|---|
| `fastddd-tooling-api` | Studio、插件和扩展使用的稳定契约层 |
| `fastddd-templates` | Community 代码模板 |
| `fastddd-fastdoc-core` | FastDoc 文档模型与生成内核 |
| `fastddd-fastdoc-spring-boot-starter` | FastDoc 可选运行期集成 |
| `fastddd-code-generator` | 代码生成引擎 |
| `fastddd-doc-generator` | 文档生成引擎 |
| `fastddd-maven-plugin` | 项目初始化（`init-project`）与代码生成（`generate-code`）Maven 插件 |
| `fastddd-doc-maven-plugin` | 文档生成 Maven 插件 |
| `fastddd-project-archetype` | 旧脚手架兼容模块（Deprecated） |

## 快速开始——生成 DDD 项目脚手架

前置条件：**JDK 21**。Tooling 与 FastDDD Framework 均已发布 Maven Central（坐标 `io.github.gallop.fastddd`）——一条命令生成脚手架：

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.7:init-project \
  -Dinit.groupId=com.example \
  -Dinit.artifactId=shop \
  -Dinit.package=com.example.shop \
  -Dinit.modelType=RICH \
  -Dinit.databaseType=postgresql \
  -Dinit.contexts=order,product
```

生成项目自动 pin Framework **1.1.3**，直接从 Maven Central 解析——无需任何源码构建。

### 源码构建（贡献者）

```bash
# 1) Framework 1.0.2（POM 默认轴）—— 来自公开 framework 仓
git clone --depth 1 --branch v1.0.2 https://github.com/gallop/fastddd-framework.git
cd fastddd-framework && ./mvnw -B -Drevision=1.0.2 clean install -DskipTests
# 2) Tooling —— 本仓库
cd .. && git clone https://github.com/gallop/fastddd-tooling.git
cd fastddd-tooling && ./mvnw -B clean install -DskipTests
```

> 提示：想基于 Central 已发布的 Framework 线构建，可跳过第 1 步，并在第 2 步追加 `-Dfastddd-framework.version=1.1.3`。

### 生成效果

上述命令将渲染出：

```text
shop/
├── pom.xml · Dockerfile · README.md
└── src/main/java/com/example/shop/
    ├── order/                                # 限界上下文
    │   ├── interfaces/      OrderController · dto/OrderResponse
    │   ├── application/     OrderService
    │   ├── domain/          Order（聚合根）· OrderRepository · OrderStatus
    │   └── infrastructure/  persistence/（Mapper · PO · Converter · RepositoryImpl）
    └── product/ …（同构四层）
```

主要参数：

| 参数 | 默认值 | 说明 |
|---|---|---|
| `init.modelType` | `ANEMIC` | 领域模型类型：`ANEMIC`（PO + Service + Controller）或 `RICH`（DDD 四层，复杂业务推荐） |
| `init.databaseType` | `mysql` | 数据库类型：`mysql` / `postgresql` / `h2` |
| `init.contexts` | （无） | 逗号分隔的限界上下文名，仅 `snake_case`（仅 RICH 模式生效）；按上下文生成四层脚手架 |
| `init.enableSecurity` | `false` | 生成 Security SPI 骨架（`AppIdentityService`） |
| `init.enableEvent` | `false` | 启用事件模块依赖 + 聚合根事件发布代码 |
| `init.enableCache` / `init.enableSearch` / `init.enableMonitor` | `false` | 可选能力模块 |
| `init.idStrategy` | `ASSIGN_ID` | 渲染进生成项目的 MyBatis-Plus 主键策略 |
| `init.tablePrefix` | `t_` | 生成的 `@TableName` 表前缀 |
| `init.language` | `zh` | 脚手架文案语言：`zh`（中文）或 `en`（英文）。覆盖 javadoc/注释、README、校验消息、日志消息、Swagger 注解值、application.yml/Dockerfile 注释、Vue 登录页 UI 文案及 `init-data.sql` 种子数据（角色/菜单/权限显示名）；不影响代码结构 |

多上下文规则：上下文之间只能依赖彼此的**领域层**（Repository 接口 + 聚合根）；生成前先规划单向无环的依赖 DAG。

## 更多生成 goal

### 从数据库生成 CRUD 代码

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.7:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/your_db \
  -Dcodegen.username=root \
  -Dcodegen.password=your_password \
  -Dcodegen.basePackage=com.example.project \
  -Dcodegen.tablePrefix=t_
```

### 生成数据库文档

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.7:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/your_db \
  -Dfastdoc.username=root \
  -Dfastdoc.password=your_password \
  -Dfastdoc.format=both
```

## 构建

```bash
./mvnw clean verify
```

需先保证兼容版本的 Framework 制品可从 Maven 仓库解析。

## 链接

- 模块聚合说明：[`fastddd-tooling/README.zh-CN.md`](fastddd-tooling/README.zh-CN.md)
- 文档入口：[`docs/README.zh-CN.md`](docs/README.zh-CN.md)
- 兼容矩阵：[`fastddd-tooling/fastddd-tooling-api/src/main/resources/fastddd-compatibility-matrix.yaml`](fastddd-tooling/fastddd-tooling-api/src/main/resources/fastddd-compatibility-matrix.yaml)

## 生态

- **fastddd-framework** —— 配套仓库：本工具链为之生成代码的 DDD 运行时框架；制品已发布 Maven Central（`io.github.gallop.fastddd`）：<https://github.com/gallop/fastddd-framework>
- **FastDDD Skills** —— 沉淀 FastDDD 最佳实践的 Claude 技能集：架构设计 → 项目初始化 → 开发模式 → 自动化开发循环。单独分发。 <!-- 分发地址待开源后补充 -->

## 许可证

FastDDD Tooling Community 使用 [Apache License 2.0](LICENSE)，第三方与上游归属说明见 [NOTICE](NOTICE)。本许可不覆盖 FastDDD Studio Pro 的源码、UI、部署资产或商业能力。
