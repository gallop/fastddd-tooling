# 第三方组件许可清单（Third-Party Notices）

> FastDDD Tooling 以 Apache License 2.0 发布（根 `LICENSE`）；本文件按已核实版本披露第三方组件的许可证、双/多许可选项与权威文本位置。
> 编制依据：依赖清单（RELEASE-03，117 组件）+ jar 内材料/发布 POM 逐项取证（2026-09-09）；覆盖层含原“未解析”项的全部补证结果。
> 快照日期：2026-09-09。

## 0. 分发形态与口径声明

- 本仓库发布的制品为本项目自有库 jar 与 POM（jar 内 `META-INF/` 附本仓库 `LICENSE`/`NOTICE`），**不包含任何第三方组件 jar 本体**；第三方组件由消费者按坐标自行从 Maven Central 等仓库解析。
- 分发形态定性：各第三方许可证中因“分发该组件本体”而生的实体义务（附许可证文本、保留声明、提供源码等），落点取决于**谁在分发该组件本体、以何种形态**；本仓库发布不含第三方本体，故上述义务不在本仓库分发物上触发。
- 本清单是披露实践，**不构成任何义务已履行（或无须履行）的声明**；组件 jar 内是否自带许可材料，也不自动等于该组件全部许可义务已满足——义务履行以实际分发形态对照各组件权威原文逐条判断。
- 任何人在本仓库制品之外另行分发第三方组件本体（聚合包、离线仓、镜像等）时，应以各组件权威许可原文为准自行判断并履行义务。
- 双/多许可组件：本清单列示全部合法选项；“标注选项”仅指本清单的披露口径，不替接收方行使其选择权。

## 1. 需单独注意的组件

| 组件 | 版本 | 许可证 | 权威文本位置 | 附注 |
|---|---|---|---|---|
| `com.oracle.database.jdbc:ojdbc8` | 23.7.0.25.01 | Oracle Free Use Terms and Conditions (FUTC) v1.0（2022-06-28 文本） | jar 内 `META-INF/license.txt`（全文） | fastddd-maven-plugin runtime 直依；本仓库不分发该 jar 本体。FUTC 明文允许未修改制品的开发/测试/运行/托管/再分发（五条件：许可随附、不单独就本件加收费用、保留专有标记、出口管制、禁反向工程） |
| `com.mysql:mysql-connector-j` | 9.7.0 | GPL-2.0 WITH Universal FOSS Exception v1.0 | jar 根 `LICENSE`（GPLv2 全文 + uFOSS v1.0 全文） | 经 fastddd-code-generator / fastddd-maven-plugin runtime 传递引用；本仓库不分发该 jar 本体（同 Framework 侧口径） |
| `cn.hutool:hutool-all` | 5.8.34 | MulanPSL-2.0 | jar 内无许可证文件；官方全文随本仓库归档：`third-party-licenses/MulanPSL-2.0.txt` | 经 fast-ddd-common 传递引入（本仓无直依）。再分发该组件本体时须附许可证副本并保留声明（MulanPSL-2.0 §4） |
| `ch.qos.logback:logback-classic` | 1.5.34 | EPL-2.0 OR LGPL-2.1-only（由接收方任选） | 官方双许可 pointer 随本仓库归档：`third-party-licenses/logback-LICENSE.txt` | 双列不替接收方选择；缺省指引 EPL-2.0 |
| `ch.qos.logback:logback-core` | 1.5.34 | EPL-2.0 OR LGPL-2.1-only（由接收方任选） | 同 logback-classic（官方 pointer 归档） | 同 logback-classic |
| `com.github.javaparser:javaparser-core` | 3.25.1 | Apache-2.0 / LGPL（双许可） | 内嵌 pom 声明 | 本清单标注选项：Apache-2.0 |
| `com.github.javaparser:javaparser-symbol-solver-core` | 3.25.1 | Apache-2.0 / LGPL（双许可） | 内嵌 pom 声明 | 本清单标注选项：Apache-2.0 |
| `org.javassist:javassist` | 3.29.2-GA | Apache-2.0 / MPL-1.1 / LGPL-2.1（三选一） | 内嵌 pom 声明 | 本清单标注选项：Apache-2.0 |
| `org.eclipse.sisu:org.eclipse.sisu.inject` | 0.3.5 | EPL-1.0 | 发布 POM 文件头版权注释（eclipse.org/legal/epl-v10.html） |  |
| `org.eclipse.sisu:org.eclipse.sisu.plexus` | 0.3.5 | EPL-1.0 | 发布 POM 文件头版权注释 |  |
| `javax.annotation:javax.annotation-api` | 1.2 | CDDL-1.0（jar 随附 CDDL 全文；pom 另列 GPLv2 w/ CPE） | jar 内 `LICENSE.txt`（CDDL 全文，36116B） | 以 jar 内实际携带文本为准标注 CDDL |
| `jakarta.annotation:jakarta.annotation-api` | 2.1.1 | EPL-2.0 / GPL-2.0 with Classpath Exception（双许可） | jar 内 `LICENSE.md` + `NOTICE.md` | 本清单标注选项：EPL-2.0 |
| `jakarta.xml.bind:jakarta.xml.bind-api` | 4.0.5 | EDL 1.0（= BSD-3-Clause，单许可） | jar 内 `LICENSE.md` + `NOTICE.md` |  |
| `jakarta.activation:jakarta.activation-api` | 2.1.4 | EDL 1.0（= BSD-3-Clause，单许可） | jar 内 `LICENSE.md` + `NOTICE.md` |  |

## 2. 全量组件清单

共 117 项（含传递依赖）。许可证列以覆盖层核实结果为准；未特别标注者为其 pom 声明的常规宽松许可。

| 组件 | 版本 | 许可证 |
|---|---|---|
| `com.alibaba:druid-spring-boot-3-starter` | 1.2.28 | Apache-2.0 |
| `com.alibaba:druid` | 1.2.28 | Apache-2.0 |
| `com.baomidou:mybatis-plus-annotation` | 3.5.12 | Apache-2.0 |
| `com.baomidou:mybatis-plus-core` | 3.5.12 | Apache-2.0 |
| `com.baomidou:mybatis-plus-extension` | 3.5.12 | Apache-2.0 |
| `com.baomidou:mybatis-plus-generator` | 3.5.12 | Apache-2.0 |
| `com.baomidou:mybatis-plus-spring-boot-autoconfigure` | 3.5.12 | Apache-2.0 |
| `com.baomidou:mybatis-plus-spring-boot3-starter` | 3.5.12 | Apache-2.0 |
| `com.baomidou:mybatis-plus-spring` | 3.5.12 | Apache-2.0 |
| `com.baomidou:mybatis-plus` | 3.5.12 | Apache-2.0 |
| `com.fasterxml.jackson.core:jackson-annotations` | 2.21 | Apache-2.0 |
| `com.fasterxml.jackson.core:jackson-core` | 2.21.4 | Apache-2.0 |
| `com.fasterxml.jackson.core:jackson-databind` | 2.21.4 | Apache-2.0 |
| `com.fasterxml.jackson.dataformat:jackson-dataformat-yaml` | 2.21.4 | Apache-2.0 |
| `com.fasterxml.jackson.datatype:jackson-datatype-jdk8` | 2.21.4 | Apache-2.0 |
| `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | 2.21.4 | Apache-2.0 |
| `com.fasterxml.jackson.module:jackson-module-parameter-names` | 2.21.4 | Apache-2.0 |
| `com.fasterxml:classmate` | 1.7.3 | Apache-2.0 |
| `com.google.code.findbugs:jsr305` | 3.0.2 | Apache-2.0 |
| `com.google.errorprone:error_prone_annotations` | 2.11.0 | Apache-2.0 |
| `com.google.guava:failureaccess` | 1.0.1 | Apache-2.0 |
| `com.google.guava:guava` | 31.1-jre | Apache-2.0 |
| `com.google.guava:guava` | 32.0.1-jre | Apache-2.0 |
| `com.google.guava:listenablefuture` | 9999.0-empty-to-avoid-conflict-with-guava | Apache-2.0 |
| `com.google.inject:guice` | 5.1.0 | Apache-2.0 |
| `com.google.j2objc:j2objc-annotations` | 1.3 | Apache-2.0 |
| `com.zaxxer:HikariCP` | 6.3.3 | Apache-2.0 |
| `io.micrometer:micrometer-commons` | 1.15.12 | Apache-2.0 |
| `io.micrometer:micrometer-observation` | 1.15.12 | Apache-2.0 |
| `io.swagger.core.v3:swagger-annotations-jakarta` | 2.2.47 | Apache-2.0 |
| `io.swagger.core.v3:swagger-core-jakarta` | 2.2.47 | Apache-2.0 |
| `io.swagger.core.v3:swagger-models-jakarta` | 2.2.47 | Apache-2.0 |
| `jakarta.validation:jakarta.validation-api` | 3.0.2 | Apache-2.0 |
| `org.apache.commons:commons-lang3` | 3.17.0 | Apache-2.0 |
| `org.apache.logging.log4j:log4j-api` | 2.24.3 | Apache-2.0 |
| `org.apache.logging.log4j:log4j-to-slf4j` | 2.24.3 | Apache-2.0 |
| `org.apache.tomcat.embed:tomcat-embed-core` | 10.1.55 | Apache-2.0 |
| `org.apache.tomcat.embed:tomcat-embed-el` | 10.1.55 | Apache-2.0 |
| `org.apache.tomcat.embed:tomcat-embed-websocket` | 10.1.55 | Apache-2.0 |
| `org.codehaus.plexus:plexus-cipher` | 2.0 | Apache-2.0 |
| `org.codehaus.plexus:plexus-classworlds` | 2.7.0 | Apache-2.0 |
| `org.codehaus.plexus:plexus-component-annotations` | 2.1.0 | Apache-2.0 |
| `org.codehaus.plexus:plexus-interpolation` | 1.26 | Apache-2.0 |
| `org.codehaus.plexus:plexus-sec-dispatcher` | 2.0 | Apache-2.0 |
| `org.codehaus.plexus:plexus-utils` | 3.5.1 | Apache-2.0 |
| `org.freemarker:freemarker` | 2.3.34 | Apache-2.0 |
| `org.hibernate.validator:hibernate-validator` | 8.0.3.Final | Apache-2.0 |
| `org.jboss.logging:jboss-logging` | 3.6.3.Final | Apache-2.0 |
| `org.jspecify:jspecify` | 1.0.0 | Apache-2.0 |
| `org.mapstruct:mapstruct` | 1.5.5.Final | Apache-2.0 |
| `org.mybatis:mybatis-spring` | 3.0.4 | Apache-2.0 |
| `org.mybatis:mybatis` | 3.5.19 | Apache-2.0 |
| `org.springdoc:springdoc-openapi-starter-common` | 2.8.17 | Apache-2.0 |
| `org.springdoc:springdoc-openapi-starter-webmvc-api` | 2.8.17 | Apache-2.0 |
| `org.springdoc:springdoc-openapi-starter-webmvc-ui` | 2.8.17 | Apache-2.0 |
| `org.springframework.boot:spring-boot-autoconfigure` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-configuration-processor` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-starter-jdbc` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-starter-json` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-starter-logging` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-starter-tomcat` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-starter-validation` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-starter-web` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot-starter` | 3.5.16 | Apache-2.0 |
| `org.springframework.boot:spring-boot` | 3.5.16 | Apache-2.0 |
| `org.springframework:spring-aop` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-beans` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-context` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-core` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-expression` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-jcl` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-jdbc` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-tx` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-web` | 6.2.19 | Apache-2.0 |
| `org.springframework:spring-webmvc` | 6.2.19 | Apache-2.0 |
| `org.webjars:swagger-ui` | 5.32.2 | Apache-2.0 |
| `org.yaml:snakeyaml` | 2.4 | Apache-2.0 |
| `com.microsoft.sqlserver:mssql-jdbc` | 12.10.2.jre11 | MIT |
| `org.checkerframework:checker-qual` | 3.12.0 | MIT |
| `org.projectlombok:lombok` | 1.18.46 | MIT |
| `org.slf4j:jul-to-slf4j` | 2.0.18 | MIT |
| `org.slf4j:slf4j-api` | 2.0.18 | MIT |
| `org.webjars:webjars-locator-lite` | 1.1.3 | MIT |
| `com.github.javaparser:javaparser-core` | 3.25.1 | Apache-2.0 / LGPL（双许可） |
| `com.github.javaparser:javaparser-symbol-solver-core` | 3.25.1 | Apache-2.0 / LGPL（双许可） |
| `org.eclipse.sisu:org.eclipse.sisu.inject` | 0.3.5 | EPL-1.0 |
| `org.eclipse.sisu:org.eclipse.sisu.plexus` | 0.3.5 | EPL-1.0 |
| `ch.qos.logback:logback-classic` | 1.5.34 | EPL-2.0 OR LGPL-2.1-only（由接收方任选） |
| `ch.qos.logback:logback-core` | 1.5.34 | EPL-2.0 OR LGPL-2.1-only（由接收方任选） |
| `org.javassist:javassist` | 3.29.2-GA | Apache-2.0 / MPL-1.1 / LGPL-2.1（三选一） |
| `org.postgresql:postgresql` | 42.7.11 | BSD |
| `javax.annotation:javax.annotation-api` | 1.2 | CDDL-1.0（jar 随附 CDDL 全文；pom 另列 GPLv2 w/ CPE） |
| `jakarta.xml.bind:jakarta.xml.bind-api` | 4.0.5 | EDL 1.0（= BSD-3-Clause，单许可） |
| `com.mysql:mysql-connector-j` | 9.7.0 | GPL-2.0 WITH Universal FOSS Exception v1.0 |
| `cn.hutool:hutool-all` | 5.8.34 | MulanPSL-2.0 |
| `jakarta.activation:jakarta.activation-api` | 2.1.4 | EDL 1.0（= BSD-3-Clause，单许可） |
| `jakarta.annotation:jakarta.annotation-api` | 2.1.1 | EPL-2.0 / GPL-2.0 with Classpath Exception（双许可） |
| `com.oracle.database.jdbc:ojdbc8` | 23.7.0.25.01 | Oracle Free Use Terms and Conditions (FUTC) v1.0（2022-06-28 文本） |
| `aopalliance:aopalliance` | 1.0 | Public Domain（发布 POM 声明） |
| `javax.inject:javax.inject` | 1 | Apache-2.0（发布 POM 声明（apache.org 链接）） |
| `org.apache.maven.plugin-tools:maven-plugin-annotations` | 3.10.2 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven.resolver:maven-resolver-api` | 1.9.16 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven.resolver:maven-resolver-impl` | 1.9.16 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven.resolver:maven-resolver-named-locks` | 1.9.16 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven.resolver:maven-resolver-spi` | 1.9.16 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven.resolver:maven-resolver-util` | 1.9.16 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven.shared:maven-shared-utils` | 3.3.4 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-artifact` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-builder-support` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-core` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-model-builder` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-model` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-plugin-api` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-repository-metadata` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-resolver-provider` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-settings-builder` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |
| `org.apache.maven:maven-settings` | 3.9.5 | Apache-2.0（jar 内 `META-INF/LICENSE`（Apache-2.0 全文）+ `NOTICE`） |

## 3. 随本仓库归档的许可证全文

| 文件 | 对应组件 | 归档原因 |
|---|---|---|
| `third-party-licenses/MulanPSL-2.0.txt` | hutool-all 5.8.34 | 该组件 jar 内不携带许可证文本，且 MulanPSL-2.0 §4 要求再分发时提供许可证副本——归档官方原文（coscl）供取用 |
| `third-party-licenses/logback-LICENSE.txt` | logback-core/classic 1.5.34 | 双许可信息不在 jar/pom 内，官方 pointer（“由接收方任选”）是选项依据——归档官方原文（QOS.ch）供取用 |

> 其余组件的许可全文以其 jar 内携带文本或官方发布地址为权威来源，本仓库不重复归档。
> 组件级依赖图与漏洞对应关系见随发布提供的 SBOM（CycloneDX 1.5）。

