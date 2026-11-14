# FastDDD 文档生成 Maven 插件

[English](README.md) | **简体中文**

基于 Java 源码（Javadoc 注释与 Spring/Swagger 注解）生成 API 文档（Markdown）的 Maven 插件，
生成引擎为 `fastddd-doc-generator`。

## 用法

```bash
mvn io.github.gallop.fastddd:fastddd-doc-maven-plugin:1.0.0:generate \
  -DscanPackage=com.example.shop \
  -Dfastdoc.language=zh
```

## 参数

| 参数 | 默认值 | 说明 |
|---|---|---|
| `scanPackage` | （必填） | 扫描的 Controller 所在包 |
| `sourceRoot` | `${project.basedir}/src/main/java` | 源码扫描根目录 |
| `outputDir` | `${project.basedir}/docs/api` | 输出目录 |
| `projectName` | `${project.name}` | 文档头展示的项目名 |
| `projectVersion` | `${project.version}` | 文档头展示的项目版本 |
| `projectDescription` | `${project.description}` | 文档头展示的项目描述 |
| `format` | `MARKDOWN` | 仅实现 Markdown；待发布修复版会拒绝 HTML/JSON，不再静默生成 Markdown |
| `includeExamples` | `true` | 请求/响应 JSON 示例（可得时）是否包含 |
| `fastdoc.language` | `zh` | 生成文档文案语言：`zh` / `en` |
| `skip` | `false` | 是否跳过生成 |
| `failOnError` | `true` | 生成失败时是否使构建失败 |

说明：

- 已发布的 `1.0.0` 缺少聚合模板，请保持 `singleFile=false`。
  待发布修复版补齐 `-DsingleFile=true`：生成 `API-Documentation.md`，索引指向实际文件。
- 本插件面向源码。数据库结构文档请使用 `fastddd-maven-plugin` 的 `generate-doc` 目标（`fastdoc.*` 参数）。
