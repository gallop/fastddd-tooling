# Tooling 独立消费者契约测试

本模块拥有 Tooling 的外部消费者场景。ECO-005 将 `fastdoc-minimal` 从 Framework 迁入本模块，使 Framework 不再反向依赖 Tooling。

```bash
./mvnw -B clean install -DskipTests \
  -Dfastddd-framework.version=<framework-version>
./mvnw -B verify -pl fastddd-tooling-consumer-tests -Pconsumer-tests \
  -Dfastddd-framework.version=<framework-version>
```

场景从 Framework BOM 取得 Framework 依赖管理，并显式依赖 Tooling 的 `fastddd-fastdoc-spring-boot-starter`。完整日志位于 `target/its/fastdoc-minimal/build.log`。

2026-08-31 本地验收：`fastdoc-minimal` 通过，`Passed: 1, Failed: 0, Errors: 0, Skipped: 0`。
