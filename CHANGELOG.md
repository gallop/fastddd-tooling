# Changelog

**English** | [简体中文](CHANGELOG.zh-CN.md)

This file records all notable changes to FastDDD, following the [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) format;
versioning follows [Semantic Versioning](https://semver.org/).

For the full version baselines and compatibility lines see [docs/COMPATIBILITY_MATRIX.md](docs/COMPATIBILITY_MATRIX.md);
for business-system upgrade steps, follow the upgrade guide of the Framework Community version in use.

---

## [1.0.7] - 2026-11-14

Candidate for the first Maven Central public release, pairing Framework 1.1.3 — the only
Framework line currently published on Central. Certification evidence: full reactor built
against framework 1.1.3 resolved straight from Maven Central (12/12 modules BUILD SUCCESS,
148/0/0/2 tests), generated projects auto-pin 1.1.3 and compile Central-only; the single
failing test was the version-governance contract demanding exactly this registration.

### Added
- Public release metadata: project `<url>` + `<scm>` on the root POM (ECO-004 placeholder
  honored; Sonatype Central validation prerequisite) — flattened POMs carry both across
  the whole reactor.
- Compatibility-matrix candidate tuple `(tooling 1.0.7, framework 1.1.3)`.

### Changed
- README (bilingual) aligned with release reality: version/status, quick start rewritten
  for the Central publication facts, new Features section, "What you get" generated-project
  structure sample, and demos for the `generate-code` / `generate-doc` goals.

### Security
- `CredentialScanTest` AI-key canary split into constant-folded segments (detection
  semantics unchanged — mutation-verified); internal repository addresses redacted from
  CHANGELOG and compatibility-matrix narratives ahead of open-sourcing.

## [1.0.6] - 2026-10-20

Internal release (2026-10-20, T8.83 broken-chain fix batch, tag v1.0.6; acceptance chain: 162 live-stack S3 rerun exit 0 / 56 classes zero errors — the F2' ten-error chain closed — and S6 rerun exit 0 / 46 classes, full 7-module reactor green — the F3' seventeen-error chain closed; pre-release adversarial review passed).

### Fixed

- **Security SPI signature drift** (F2', six templates; T8.81 S3 ten-error compile-smoke chain):
  the framework security-starter SPI is String-typed (`getUserInfo(String)`,
  `getPermissionsByUserId(String)`, `getRolesByUserId(String)`, `SecurityUserDetails.userId`,
  `SecurityUserInfo.id`), but six templates shipped Long signatures/direct passes/explicit casts —
  rendering fine while every generated project failed to compile. Fixed in the Demo-canonical shape
  (String signatures, `Long.valueOf(userId)` adapters at mapper calls with mappers kept Long,
  `String.valueOf(...)` builder values) across `RbacIdentityServiceImpl`, `RbacPermissionServiceImpl`,
  `UserDetailsServiceImpl` (`getUserAuthorities(String)`), `CustomIdentityServiceImpl`,
  `CustomPermissionServiceImpl`, and multi-channel `AppIdentityService` (also strips the two
  `(Long)` casts into String builders; the `@Override` compile error on every MCB sub-app is the
  T8.82R linkage-blind-spot closure). `SecurityQueryMapper` and the `Sys*Mapper` templates stay
  Long (template-owned interfaces, out of scope by declaration).
- **MCB library-module dependency gap** (F3', dual pom; T8.81 S6 seventeen-error chain): anemic
  five-piece artifacts (Controller/Entity/…) are always routed into library modules by the studio
  MCB dead config, but `common-shared-pom` carried only common+model+lombok and `common-domain-pom`
  lacked web — so `fast-ddd-web`/`fast-ddd-crud` types (spring-web, validation, springdoc) never
  resolved. Both poms now declare fast-ddd-web (+crud in shared) versionless under the parent
  dependencyManagement.

### Added

- `SecuritySpiSignatureTemplatesRenderTest` (eight arms): direct-render regression for the six
  templates + both poms — positive anchors (String signatures, `Long.valueOf` adapters,
  `String.valueOf` builders, pom dependency blocks) and negative anchors (`(Long userId)`,
  unadapted `ByUserId(userId)` passes, `(Long) user.getId()` casts, `<version>` leakage,
  FreeMarker residue).

## [1.0.5] - 2026-10-15

Internal official release (2026-10-15, owner-approved 「请发布，并push」; GA artifacts plus the
framework 1.0.2 resolution closure deployed to the internal GA Maven repository
(address withheld); git anchor `v1.0.5`). The current certified tuple is
now `tooling 1.0.5 / framework 1.0.2` — the 1.0.2 tuple demoted to released. Carries the
unreleased 1.0.3 and 1.0.4 candidates in full (absorbed at release; their candidate matrix
rows removed).

### Fixed

- **SecurityQueryMapper escape inversion** (F-⑥, T8.64): on the custom-security five-table
  path (studio `SecuritySpiRenderer`; all of USER/ROLE/PERMISSION/USER_ROLE/ROLE_PERMISSION
  tables present), the generator renders `project/security/SecurityQueryMapper.java.ftl` with a
  12-key queryVars Map — and the template's two `WHERE u.id = …` sites carried the *inverted*
  escape `#${r"${userId}"}` (`#` left outside the raw string). It renders fine, ships the broken
  mapper into generated projects, and only detonates at runtime: `#${userId}` is not a MyBatis
  `#{}` binding, `${userId}` runs as string substitution, and the leftover bare `#` (MySQL line
  comment) truncates the auth SQL on the first query. Pre-existing since the initial commit
  (bf7eb2b); found by the T8.63 pre-push adversarial review (finding ①). Both sites now use the
  full raw-string form `${r"#{userId}"}` (same shape as the F-⑤ fix).

### Added

- **Tests**: `SecurityQueryMapperTemplatesRenderTest` — renders the template with the exact
  12-key queryVars Map `SecuritySpiRenderer` passes, asserting the MyBatis binding placeholder
  comes out literally (×2), the inverted `#${` form is absent, and all twelve variables
  substitute into the SQL.

### Changed

- `fastddd-compatibility-matrix.yaml` + `docs/COMPATIBILITY_MATRIX(.zh-CN).md`: appended the
  `(tooling 1.0.5, framework 1.0.2)` candidate tuple; historical rows untouched; the current
  certified tuple `(1.0.2, 1.0.0)` unchanged.

## [1.0.4] - 2026-10-14

Candidate build (NOT released — local install / test state only, registered in the
compatibility matrix as a `candidate` tuple; the current certified tuple stays
`tooling 1.0.2 / framework 1.0.0`; promotion requires the acceptance chain and owner
approval). Carries the unreleased 1.0.3 candidate in full.

### Fixed

- **Security mapper template render crash** (F-⑤, T8.63): with Security enabled and *no* RBAC
  tables in the design, the generator renders `project/security/Sys*Mapper.java.ftl` for real for
  the first time (the existing-contains skip never fired) — and FreeMarker 2.3.32 parsed the
  MyBatis `#{param}` placeholders in the `@Select` SQL as numerical interpolation, throwing
  `InvalidReferenceException` (`Failed at: #{username}`) and failing the generation task
  (live evidence: 162 task 2110182776456187906). All 15 placeholder sites across the three
  templates (`SysUserMapper` 6 / `SysRoleMapper` 4 / `SysPermissionMapper` 5) now use the
  raw-string interpolation form `${r"#{param}"}` so the placeholders render literally.

### Added

- **Tests**: `SecurityMapperTemplatesRenderTest` — renders all three Sys* mapper templates with a
  table-metadata-free context (= the no-RBAC-tables shape) and asserts the MyBatis placeholders
  come out literally with per-file multiplicity. Historical ITs always seeded the five RBAC
  tables and walked the skip arm, so these templates had never actually been rendered.

### Changed

- `fastddd-compatibility-matrix.yaml` + `docs/COMPATIBILITY_MATRIX(.zh-CN).md`: appended the
  candidate tuple `(tooling 1.0.4, framework 1.0.2)` (status `candidate`, unreleased — for the
  candidate acceptance chain only, per the 2026-09-13 precedent); the unreleased 1.0.3 candidate
  is carried in full. The current certified tuple is unchanged.

## [1.0.3] - 2026-10-12

Candidate build (NOT released — local install / test state only, registered in the
compatibility matrix as a `candidate` tuple; the current certified tuple stays
`tooling 1.0.2 / framework 1.0.0`; promotion requires the acceptance chain and owner approval).

### Added

- **Semantic-expansion template annotations** (P2-2): `rich/AggregateRoot.ftl` class javadoc now injects
  the aggregate name and relations summary, and `anemic/Entity.ftl` injects the DDD role, consuming the three
  nullable `TableMetadata` columns added by Framework 1.0.2 (`aggregateName`/`dddRole`/`relationsSummary`).
  Injection uses the truthy guard form `<#if x?? && x?has_content>` (FreeMarker has no bare-string truthiness — the `&&` right operand must be boolean) — absent/null/blank columns degrade to no annotation
  and render fine against pre-1.0.2 framework jars (missing property); unguarded interpolation would throw.
  Labels go through the i18n message dictionary (new keys in `messages_zh.properties` / `messages_en.properties`:
  `rich.aggregate.root.aggregateNamePrefix`, `rich.aggregate.root.relationsSummaryPrefix`,
  `anemic.entity.dddRolePrefix`).
- **Tests**: value-arm and absent-arm gates on both compile hosts (`GeneratedRichCodeCompileTest`,
  `GeneratedAnemicCodeCompileTest` — fixture-value anchors plus guard-degradation arms) and a missing-property
  window gate on `TemplateRendererTest` (LinkedHashMap table without the new keys — new-template×old-framework).

### Changed

- Root POM `fastddd-framework.version` 1.0.0 → 1.0.2 (Framework 1.0.2 local-install test state; Tooling's own
  `revision` untouched — 1.0.3 is built via `-Drevision=1.0.3`).
- `fastddd-compatibility-matrix.yaml`: appended the candidate tuple `(tooling 1.0.3, framework 1.0.2)`
  (status `candidate`, unreleased — for the candidate acceptance chain only, per the 2026-09-13 precedent);
  the development-line tuple's framework axis moved to 1.0.2 alongside the POM; a staged `1.0.2` framework-line
  manifest entry was added (current-stable stays on 1.0.0). The current certified tuple is unchanged.

## [1.0.2] - 2026-09-14

Patch release of Tooling only, internally released 2026-09-14 (registered in the
compatibility matrix as the current certified tuple
`studio 1.0.0-jdk21-SNAPSHOT / tooling 1.0.2 / framework 1.0.0`, status `current`;
Framework stays at 1.0.0 and the 1.0.1 tuple is demoted to `released` as a published
historical record).

Fix scope: per-table code generation compilability defects (D-2) exposed by the first
real Studio export (`mvn clean verify` on the exported repo), plus the runtime-assembly
defects (D-3/D-4) and the executable-packaging contract gap exposed by the first real
`java -jar` boot of an exported project (acceptance includes boot/health and a
create–query roundtrip, not compilation alone; update/delete and the symlink-specific
scenario are outside this release's acceptance scope). The H2 metadata catalog
defect (D-1) found in the same
investigation is fixed Studio-side (connection catalog/schema resolution), not in
Tooling artifacts. Corrected impact assessment: the
rich-model channel was broken
unconditionally by the Swagger annotation defect, plus the BigDecimal import defect on
DECIMAL columns and the missing application/MapStruct dependencies when the pom was
rendered ANEMIC; the anemic channel was broken only by the missing BigDecimal import on
DECIMAL columns. Not "every combination fails".

### Fixed

- **Missing `java.math.BigDecimal` imports (D-2a)**: templates had no BigDecimal import
  branch anywhere, so any DECIMAL column (`BigDecimal` field) produced non-compiling
  code in both channels. Detection loops now derive `useBigDecimal` from the actual
  column types alongside `useJavaTime` (rich `AggregateRoot`/`po` previously had no
  `java.time` detection either — same-class latent defect on DATETIME columns, fixed
  with the same mechanism) across rich `AggregateRoot`/`po`/`Command` and anemic
  `Entity`/`DTO`/`CreateRequest`/`UpdateRequest`.
- **Swagger v2/v3 annotation inconsistency (D-2b)**: `AggregateRoot.ftl` used
  springfox-era v2 `@ApiModel`/`@ApiModelProperty` (neither dependency nor import
  present — springdoc migration was left half-done) while every other template used v3
  `@Schema`. The rich channel therefore failed to compile unconditionally under
  `useSwagger=true`. Unified to v3 `@Schema`.
- **Rich artifacts from ANEMIC projects missing dependencies (D-2c)**: per-table
  generation resolves `modelType` per table (manual override > persisted AI >
  analyzer > request default), so an ANEMIC project can legitimately contain RICH
  artifacts — but `pom.xml.ftl` gated `fast-ddd-application`, MapStruct and the
  annotation-processor path on the project-level `modelType == "RICH"` only, leaving
  those artifacts without dependencies or the MapStruct processor they call
  (`*Mapper.toPo(entity)` is invoked in the generated RepositoryImpl). The pom gate
  now also honors a `richDependencies` custom property set by the generation channel
  when any table resolved RICH; the plugin `<configuration>` block keeps gating on the
  project-level modelType so re-run semantics do not drift. Studio single-module flows
  force `enableCQRS=true` for RICH tables, so `Command`/`Query` artifacts and their
  controllers are consistent there; the plugin/raw-API path with `enableCQRS=false`
  is fixed below.
- **Rich controllers generated with CQRS disabled (same root as D-2c)**:
  `CommandController`/`QueryController` were emitted unconditionally while the
  `Command`/`Query`/`Handler` classes they reference are only generated under
  `enableCQRS=true` — closing CQRS produced controllers referencing missing classes.
  Both controllers are now gated on `enableCQRS` together with their inputs.
- **Generated RICH app cannot start — duplicate mapper beans (D-3, runtime batch
  2026-09-13)**: the RICH branch of `Application.java.ftl` used a bare root-package
  `@MapperScan`, so `ClassPathMapperScanner` registered every interface under the root
  package as a MyBatis proxy — repository interfaces (backed by `@Repository` impls)
  and MapStruct converters included — producing two beans of the same type
  (`expected single matching bean but found 2`) and `APPLICATION FAILED TO START`.
  The RICH branch now scans the root package with
  `annotationClass = org.apache.ibatis.annotations.Mapper.class` (the FQN filter also
  excludes `org.mapstruct.Mapper` converters); the anemic branch must stay unfiltered
  (anemic mappers are `BaseRepository` sub-interfaces without `@Mapper` and rely on
  the dedicated `mapperPackage` scan).
- **Mixed-model app cannot start — rich `*BaseMapper` never registered (D-4, same
  batch)**: when per-table resolution yields RICH artifacts in an ANEMIC project
  (`richDependencies`), the `*BaseMapper` interfaces carry `@Mapper` but live in
  `<root>.infrastructure.persistence`, outside the anemic `mapperPackage` scan — the
  generated `*RepositoryImpl` then fails injection (`No qualifying bean of type
  '…*BaseMapper'`; reproduced before fixing, evidence in the batch result doc). The
  Application template now renders a double `@MapperScan` (`@Repeatable` since
  mybatis-spring 2.0.2) for this shape: the `mapperPackage` package scan for anemic
  mappers plus the annotation-filtered root scan for RICH base mappers; the two scans
  are disjoint by construction.
- **Executable packaging contract gap (same batch)**: the generated Dockerfile runs
  `java -jar target/*.jar` and the README offers `mvn spring-boot:run`, but the pom
  never declared `spring-boot-maven-plugin` — `package` produced a plain jar with no
  Main-Class ("no main manifest attributes" on `java -jar`). The pom now declares the
  plugin with a `repackage` execution and no version (inherited from the
  `spring-boot-starter-parent` pluginManagement — no third-party upgrade); the plain
  jar is kept as `*.jar.original`.

### Added

- **Real-generation compile gates (in-repo)**: `GeneratedRichCodeCompileTest` /
  `GeneratedAnemicCodeCompileTest` extend the real-javac gates to the failing shapes —
  DECIMAL/DATETIME tables with `useSwagger(true)` in both channels, and RICH with
  `enableCQRS(false)`; `ProjectPomRichDependenciesTest` pins the pom gating contract
  (ANEMIC omits the rich dependencies; ANEMIC + `richDependencies` carries them while
  the plugin configuration stays project-level; RICH keeps the legacy behavior) and
  the executable-packaging contract (Boot plugin + `repackage`, version undeclared,
  both channels). Existing gates were blind to all three defects (no Swagger axis, no
  DECIMAL columns, compilation against the test classpath rather than the generated
  pom).
- **MapperScan boundary contract gate**: `ApplicationTemplateMapperScanBoundaryTest`
  pins the three-branch scanning contract — anemic (dedicated package, no annotation
  filter), RICH (root package with the ibatis `@Mapper` FQN filter), mixed (double
  `@MapperScan`) — locking both D-3 (no unfiltered root scan) and D-4 (the mixed
  shape must scan both boundaries) against regression. The runtime proof
  (boot/health/create–query roundtrip on real exported projects; update/delete and the
  symlink-specific scenario not covered) lives in the Studio-side export gate
  (`ExportMavenVerifyGateTest`).

[Unreleased]: https://github.com/gallop/fastddd-tooling/compare/v1.0.2...HEAD
[1.0.2]: https://github.com/gallop/fastddd-tooling/compare/v1.0.1...v1.0.2

---

## [1.0.1] - 2026-09-13

Patch release of Tooling only; Framework stays at 1.0.0 (registered in the compatibility
matrix as the current certified tuple `studio 1.0.0-jdk21-SNAPSHOT / tooling 1.0.1 / framework 1.0.0`;
the 1.0.0 GA tuple is retained as a released historical record).
Fix review and verification scope: `docs/TOOLING_1_0_1_FIX_REVIEW.md`.

### Fixed

- **`doc-maven-plugin` source scanning found 0 controllers**: `AnnotationParser.parseFile`
  parsed via `JavaParser.parse(String)`, so the `CompilationUnit` carried no file storage and
  `getPrimaryType()` was always empty — `isRestController()` always returned false and the
  `generate` goal produced an empty shell document for any input (inherited from 1.0.0; it
  invalidated the single-file API documentation added in this release). Parsing now goes
  through `parse(Path)` (explicit UTF-8); a source-scan contract test
  (`SourceScanContractTest`) pins the source-file → scanner → model chain, which is the layer
  the previous model-level tests missed.
- **`TemplateRenderer` legacy call convention**: calling
  `new TemplateRenderer(configuration).render(template, variables)` without a dictionary
  injection failed with `InvalidReferenceException: msg` on 1.0.0 artifacts. Rendering now
  falls back to the default Chinese dictionary in a per-render local copy — it never
  overrides a caller-supplied or entry-layer injected (`zh`/`en`/custom) `msg`, and never
  writes back to the shared `Configuration`.
- **`init-data.sql` menu seeding on MySQL 8**: the multi-row `sys_menu` INSERT embedded a
  same-table SELECT subquery, which MySQL rejects (ERROR 1093). The parent menu is now
  inserted first and the four child menus reference the real parent id via
  `INSERT ... SELECT`; the `ON DUPLICATE KEY` re-execution semantics for MySQL are kept.
- **Documentation boolean flags**: `deprecated`/`required` flags with value `false` were
  rendered as deprecated/required markers (`??` null-check treated `false` as present).
  Templates now test the boolean value; empty `summary`/optional-field conditions and
  `generatedAt` (LocalDateTime) rendering fixed in the same pass.

### Added

- **Single-file API documentation**: the missing `api-doc-all.md.ftl` aggregate template
  is provided; with `singleFile=true` the generator writes `API-Documentation.md` and the
  index links to the actual file instead of per-controller files.

### Changed

- **Output encoding is explicit UTF-8** in `DocGenerator` and `CodeGenMojo` file writes
  (previously platform default charset).
- **HTML/JSON output formats now fail fast** with a clear error before the output
  directory is created, instead of silently writing Markdown (behavior documented in both
  plugin READMEs; no new format implementations added).
- **Language identifiers are validated strictly**: only `zh`/`en` and hyphenated locale
  variants (`zh-CN`, `en_US`, …) are accepted; garbage such as `english`/`zhgarbage` is
  rejected. A raw zh/en dictionary key-parity check was added to the test suite.

---

## [1.0.0] - 2026-09-13

First official release baseline: **JDK 21 + Spring Boot 3.5.16**.
This version was driven by UPG-000 through UPG-010 of `JDK21_SPRING_BOOT_3_5_UPGRADE_PLAN.md`,
as a deterministic upgrade from the JDK 17 / Boot 3.4.5 development snapshot.

> **Re-issue note (2026-09-13, Owner decision).** The internal tooling 1.0.0 delivery (tag re-cut
> 2026-09-11) was withdrawn and re-issued in the same version slot alongside Framework 1.0.0:
> pre-verification confirmed the old artifacts had no consumers (both pilot projects consume
> `com.gallop.fastddd:1.0.0-jdk21-rc1`, not this GA) and were never deployed or delivered
> externally. The old tag objects and artifact bytes are archived with SHA-256 fingerprints under
> `.fastddd-releases/internal-v1.0.0-r2/evidence/withdrawn-internal-v1.0.0/`; the old internal
> release repo is isolated and marked withdrawn. The unpublished `1.0.0-jdk21` version plan is
> abandoned — the formal version line is unified on `1.0.0` (Tooling and Framework version axes
> remain independent); historical rc1 records unchanged.

### Added

- **CI-friendly version governance**: the `${revision}` property is the single version entry of the reactor, driving all 27 module versions uniformly; combined with `flatten-maven-plugin` (`resolveCiFriendliesOnly`), `${revision}` is resolved to a literal in published POMs — satisfying both "versions enter the reactor through one property only" and "published POM versions are already resolved".
- **`fastddd-bom` unified dependency-contract module**: business projects simply `import` this BOM to get consistent versions of all FastDDD modules and the recommended third-party dependencies, without declaring versions themselves. The BOM is a standalone POM; its published artifact is self-contained (no environment-property leakage).
- **Maven Enforcer build gates**: `requireJavaVersion[21,)`, `requireMavenVersion[3.9.0,4.0.0)`, `requirePluginVersions` (all plugin versions explicit), `banDuplicatePomDependencyVersions`.
- **JDK 21 compile baseline**: root pom `java.version=21` + `maven.compiler.release`; artifacts are class major version 65.
- **Spring Boot 3.5.16 + deterministic dependency alignment**: Spring AI 1.1.8, SpringDoc 2.8.17, Knife4j 4.5.0, MyBatis-Plus 3.5.12, MyBatis-Spring 3.0.4, Druid `druid-spring-boot-3-starter` 1.2.28, mysql-connector-j 9.7.0, Micrometer 1.15.12 (eliminating legacy version splits), Jackson 2.21.x (Boot-managed).
- **PMD 7**: `maven-pmd-plugin` upgraded to 3.24.0 (PMD 7.3.0), supporting `targetJdk=21`.
- **Maven Wrapper**: `./mvnw` pins Maven 3.9.5, unifying CI and local Maven versions.
- **Docker JDK 21**: build stage `maven:3.9.9-eclipse-temurin-21`, runtime stage `eclipse-temurin:21-jre-alpine`.
- **Version-governance documentation**: added `docs/COMPATIBILITY_MATRIX.md`, `docs/UPGRADE_GUIDE.md`, `docs/RELEASE_CHECKLIST.md`.
- **Template copy internationalization (L4)**: generated-code and document copy moved into a message dictionary (`fastddd-templates`: `messages/messages_{zh,en}.properties`, 538 keys; `zh` output byte-identical with historical results) injected into FreeMarker as the shared variable `msg`, referenced from templates via `${msg.*}`. New language parameters: `init.language` (`init-project`), `codegen.language` (`generate-code`), `fastdoc.language` (doc plugin `generate` goal) — all default `zh`; `en` switches generated comments, validation messages, seed data and UI copy to English without changing code structure.

### Changed

- Spring Boot 3.4.5 → **3.5.16**; JDK 17 → **JDK 21** (compile and runtime).
- Druid coordinate migration: `druid-spring-boot-starter` (Boot 2 coordinate) → **`druid-spring-boot-3-starter`** (4 references).
- MySQL driver migration: `mysql:mysql-connector-java` → **`com.mysql:mysql-connector-j`** 9.7.0 (6 references).
- Removed hand-managed version properties (mysql-connector / micrometer / lombok / freemarker / junit / testcontainers), delegating uniformly to Spring Boot BOM management and eliminating version splits.
- `application-dev.yml`: fixed a duplicated top-level `fastddd:` key (Boot 3.5 SnakeYAML strict parsing rejects duplicate keys).
- Compatibility matrix: withdrew the unpublished `1.0.0-jdk21` registrations — the pre-release certification tuple and the `1.0.0-jdk21-SNAPSHOT` development line are removed along with the abandoned version plan; the POM default combination returns to Tooling `1.0.0-SNAPSHOT` consuming the published Framework `1.0.0` GA.

### Fixed

- **B-01**: `PerformanceIntegrationTest.testBatchInsertPerformance` failed because "logical delete → re-insert of the same username" hit the `username` UNIQUE constraint; switched to a physical `DELETE` via an injected `JdbcTemplate`. Full gate: 356 tests / 0 failures.
- Pre-existing `<relativePath>` error in `fast-ddd-examples/pom.xml` (`../../pom.xml` → `../pom.xml`).
- Cleaned up two invalid `<includeStackTrace>` attributes in `fast-ddd-web/logback-spring.xml` that were ignored by logstash-encoder.
- `project/README.md.ftl`: escaped-backtick code fences rendered literal backslashes into generated READMEs; now real Markdown fences (visible behavior change: 4 fence lines differ from previously generated projects).
- **Generated-project information-leak fix**: `project/AuthController.java.ftl` (token-refresh catch block) and `project/GlobalExceptionHandler.java.ftl` (`RuntimeException` 500 handler) concatenated raw `e.getMessage()` into client responses. Re-issue re-verification confirmed that none of the current generation chains (init-project / generate-code / archetype) render these two templates — no active consumer exists; the defect sat at the template source and was inherited by any project created through a path using or copying from these templates (e.g. the pilots' manual project setup). Both templates now return fixed dictionary messages while full exception details stay in the log; the 400 handlers for `IllegalArgumentException` / `ConstraintViolationException` keep passing their messages through by design — those are intentional caller-facing validation-style messages.

### Known Limitations / carried over (not blocking 1.0.0)

- **CI runner not deployed**: the remote is an internal Gitea instance; `.github/workflows/*.yml` are declarative files with no runner executing them; the JDK 21 / wrapper / `./mvnw` / PMD 7 changes form an "effective once CI is ready" baseline, to be verified after a runner is deployed.
- **`checkstyle.xml` rules file missing**: `configLocation=checkstyle.xml` but the repository has no such file; the code-quality step runs with `continue-on-error` tolerance; adding the rules file is a separate task.
- **External-infrastructure runtime smoke tests pending**: Redis, Elasticsearch, RocketMQ distributed chains, and Docker build/startup health checks had no corresponding runtimes on the dev machine — only compile/context-level verification; smoke tests to be added once the deployment environment or Docker is ready.
- **`SecurityIntegrationTest` class-level `@Disabled`**: self-referencing `@SpringBootTest(classes=itself)` context-loading problem; pre-existing test debt (not introduced by this upgrade, not added to pass the gate).
- **Mockito self-attach warning**: inline mock self-attachment under JDK 21 (all green, coverage normal); coexistence of an explicit mockito agent with the JaCoCo argLine is deferred to the JDK 25+ migration.
- **BOM and root pom dependency lists duplicated**: to reduce reactor-resolution risk this time, the root pom's `<dependencyManagement>` stayed unchanged and the BOM mirrors the list independently. Registered as known technical debt; from 1.0.1 the root pom converges by importing the BOM (with separate reactor-resolution verification).

### 1.0.1 roadmap (registered must-do items)

- **Binary-compatibility checks (japicmp / Revapi)**: 1.0.0 has no GA baseline jar, so japicmp has no baseline to compare against; after the first GA baseline is established, every release from 1.0.1 on gets an API-compatibility check.
- **Root pom delegates to the BOM**: the root pom's `<dependencyManagement>` switches to `import` `fastddd-bom`, achieving a single source of dependency versions.
- **Generation templates switch to the BOM**: archetype-resources / FreeMarker generation templates import `fastddd-bom` by default, giving generated business projects the unified dependency contract out of the box.

---

## Pre-upgrade development snapshot (archived, unmaintained)

| Item | Value |
|----|----|
| Java | 17 |
| Spring Boot | 3.4.5 |
| FastDDD version string | `1.0.0-SNAPSHOT` |
| Status | archived as Git tag `baseline/jdk17-boot3.4.5` only, no longer maintained |

> This snapshot carries no external compatibility-line commitment. All business systems in development should upgrade to the JDK 21 / Boot 3.5.16 baseline together with 1.0.0
> (for business upgrade steps see the upgrade guide of the corresponding Framework Community version and the UPG-011 historical record).
