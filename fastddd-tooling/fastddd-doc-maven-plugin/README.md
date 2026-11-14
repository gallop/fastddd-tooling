# FastDDD Doc Maven Plugin

**English** | [简体中文](README.zh-CN.md)

Maven plugin that generates API documentation (Markdown) from Java sources — Javadoc comments and
Spring/ Swagger annotations — via the `fastddd-doc-generator` engine.

## Usage

```bash
mvn io.github.gallop.fastddd:fastddd-doc-maven-plugin:1.0.0:generate \
  -DscanPackage=com.example.shop \
  -Dfastdoc.language=zh
```

## Parameters

| Parameter | Default | Description |
|---|---|---|
| `scanPackage` | (required) | Package to scan for controllers |
| `sourceRoot` | `${project.basedir}/src/main/java` | Source root to scan |
| `outputDir` | `${project.basedir}/docs/api` | Output directory |
| `projectName` | `${project.name}` | Project name shown in the document header |
| `projectVersion` | `${project.version}` | Project version shown in the document header |
| `projectDescription` | `${project.description}` | Project description shown in the document header |
| `format` | `MARKDOWN` | Only Markdown is implemented; the upcoming patch rejects HTML/JSON instead of silently writing Markdown |
| `includeExamples` | `true` | Include request/response JSON examples when available |
| `fastdoc.language` | `zh` | Language of generated document copy: `zh` / `en` |
| `skip` | `false` | Skip generation |
| `failOnError` | `true` | Fail the build when generation fails |

Notes:

- Released `1.0.0` lacks the aggregate template: keep `singleFile=false` on that version.
  The upcoming patch adds `-DsingleFile=true` (`API-Documentation.md`, with matching index links).
- This plugin is source-based. For database-schema documentation, use the `generate-doc` goal
  of `fastddd-maven-plugin` (`fastdoc.*` parameters) instead.
