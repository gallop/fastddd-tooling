# FastDDD Tooling Community

![Version](https://img.shields.io/badge/version-1.0.6-blue) ![JDK](https://img.shields.io/badge/JDK-21-orange) ![License](https://img.shields.io/badge/license-Apache--2.0-green)

**English** | [简体中文](README.zh-CN.md)

FastDDD Tooling Community is the development-time toolchain of FastDDD: a stable Tooling API, code and document generation engines, project templates, and Maven plugins for project initialization and code generation. It depends on FastDDD Framework Community and does not depend on FastDDD Studio Pro.

> **Status: 1.0.6 GA** (2026-10-20, tag `v1.0.6`). Tooling and Framework versions evolve independently; valid combinations are governed by the in-repo compatibility matrix. Certified Framework pairing for this release: **1.0.2**; the Framework 1.1.x line is available on Maven Central, with the tooling×1.1.x combo certification in progress.

## Features

- **One-command DDD scaffolding** — `init-project` renders a runnable Spring Boot project; RICH mode lays out a four-layer structure per bounded context (interfaces / application / domain / infrastructure), ANEMIC mode renders PO + Service + Controller.
- **Multi-context aware** — `-Dinit.contexts=order,product` generates each context's aggregate root, repository, mapper and controller; cross-context wiring is constrained to domain layers.
- **Security scaffolding (optional)** — JWT login chain, Security SPI skeleton (`AppIdentityService`), and a Vue 3 login-page suite (login page, token utils, user store).
- **Optional capability modules** — event / cache / search / monitor wired in via flags.
- **Database-driven code generation** — `generate-code` reverse-engineers CRUD layers from a live database schema.
- **Database documentation** — `generate-doc` (FastDoc) documents a live schema; also bindable into the build via `fastddd-doc-maven-plugin`.
- **Bilingual scaffolding** — `-Dinit.language=zh|en` switches generated comments, messages, README and UI copy without touching code structure.

## Modules

| Module | Role |
|---|---|
| `fastddd-tooling-api` | Stable contract layer used by Studio, plugins, and extensions |
| `fastddd-templates` | Community code templates |
| `fastddd-fastdoc-core` | FastDoc document model and generation kernel |
| `fastddd-fastdoc-spring-boot-starter` | Optional FastDoc runtime integration |
| `fastddd-code-generator` | Code-generation engine |
| `fastddd-doc-generator` | Document-generation engine |
| `fastddd-maven-plugin` | Maven plugin for project initialization (`init-project`) and code generation (`generate-code`) |
| `fastddd-doc-maven-plugin` | Document-generation Maven plugin |
| `fastddd-project-archetype` | Legacy scaffolding compatibility module (Deprecated) |

## Quick start — scaffold a DDD project

Prerequisites: **JDK 21**. FastDDD Framework artifacts are published to Maven Central under `io.github.gallop.fastddd` (currently the 1.1.x line). The compatibility-certified Framework pairing for this release is **1.0.2**, installable from source while its Central publication is pending; Tooling itself is not yet on Central. Installing from source is **two steps** — the framework first (the tooling build depends on `fastddd-bom`), then this repo:

```bash
# 1) Framework 1.0.2 (certified pairing) — from the public framework repository
git clone --depth 1 --branch v1.0.2 https://github.com/gallop/fastddd-framework.git
cd fastddd-framework && ./mvnw -B -Drevision=1.0.2 clean install -DskipTests
# 2) Tooling 1.0.6 — this repository
cd .. && git clone https://github.com/gallop/fastddd-tooling.git
cd fastddd-tooling && ./mvnw -B -Drevision=1.0.6 clean install -DskipTests
```

> Tip: to try the latest Framework straight from Maven Central instead (tooling×1.1.x combo certification in progress), skip step 1 and add `-Dfastddd-framework.version=1.1.3` to step 2.

Generate the scaffolding:

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.6:init-project \
  -Dinit.groupId=com.example \
  -Dinit.artifactId=shop \
  -Dinit.package=com.example.shop \
  -Dinit.modelType=RICH \
  -Dinit.databaseType=postgresql \
  -Dinit.contexts=order,product
```

### What you get

The command above renders:

```text
shop/
├── pom.xml · Dockerfile · README.md
└── src/main/java/com/example/shop/
    ├── order/                                # bounded context
    │   ├── interfaces/      OrderController · dto/OrderResponse
    │   ├── application/     OrderService
    │   ├── domain/          Order (aggregate) · OrderRepository · OrderStatus
    │   └── infrastructure/  persistence/ (Mapper · PO · Converter · RepositoryImpl)
    └── product/ …(same four-layer shape)
```

Key parameters:

| Parameter | Default | Description |
|---|---|---|
| `init.modelType` | `ANEMIC` | Domain model type: `ANEMIC` (PO + Service + Controller) or `RICH` (four-layer DDD, recommended for complex domains) |
| `init.databaseType` | `mysql` | Database type: `mysql` / `postgresql` / `h2` |
| `init.contexts` | (none) | Comma-separated bounded-context names, `snake_case` only (RICH mode only); generates the four-layer scaffolding per context |
| `init.enableSecurity` | `false` | Generate the Security SPI skeleton (`AppIdentityService`) |
| `init.enableEvent` | `false` | Enable the event module dependency + aggregate-root event publishing |
| `init.enableCache` / `init.enableSearch` / `init.enableMonitor` | `false` | Optional capability modules |
| `init.idStrategy` | `ASSIGN_ID` | MyBatis-Plus primary-key strategy rendered into the generated project |
| `init.tablePrefix` | `t_` | Prefix for generated `@TableName` values |
| `init.language` | `zh` | Language of the generated scaffold copy: `zh` / `en`. Covers javadoc/comments, README, validation messages, log messages, Swagger annotation values, application.yml/Dockerfile comments, Vue login-page UI copy, and `init-data.sql` seed data (role/menu/permission display names); code structure is unaffected |

Multi-context rule: contexts may depend only on each other's **domain layer** (Repository interfaces + aggregate roots); plan a one-directional, acyclic dependency DAG before generating.

## More generation goals

### Generate CRUD code from a database

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.6:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/your_db \
  -Dcodegen.username=root \
  -Dcodegen.password=your_password \
  -Dcodegen.basePackage=com.example.project \
  -Dcodegen.tablePrefix=t_
```

### Generate database documentation

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.6:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/your_db \
  -Dfastdoc.username=root \
  -Dfastdoc.password=your_password \
  -Dfastdoc.format=both
```

## Build

```bash
./mvnw clean verify
```

Requires a compatible Framework version resolvable from your Maven repositories first.

## Links

- Module aggregate notes: [`fastddd-tooling/README.md`](fastddd-tooling/README.md)
- Docs entry: [`docs/README.md`](docs/README.md)
- Compatibility matrix: [`fastddd-tooling/fastddd-tooling-api/src/main/resources/fastddd-compatibility-matrix.yaml`](fastddd-tooling/fastddd-tooling-api/src/main/resources/fastddd-compatibility-matrix.yaml)

## Ecosystem

- **fastddd-framework** — companion repository: the DDD runtime framework this toolchain scaffolds for; artifacts on Maven Central (`io.github.gallop.fastddd`): <https://github.com/gallop/fastddd-framework>
- **FastDDD Skills** — Claude skills encoding FastDDD best practices: architecture design → project init → development patterns → automated dev loop. Distributed separately. <!-- distribution URL to be added at open-source time -->

## License

FastDDD Tooling Community is licensed under the [Apache License 2.0](LICENSE); third-party and upstream attribution is in [NOTICE](NOTICE). This license does not cover FastDDD Studio Pro's source code, UI, deployment assets, or commercial capabilities.
