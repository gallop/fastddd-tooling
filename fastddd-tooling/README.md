# FastDDD Tooling

**English** | [简体中文](README.zh-CN.md)

The tooling layer provides developer-productivity tools such as code generation, documentation generation, Maven plugins, and project scaffolding.

## Modules

| Module | Description |
|------|------|
| [fastddd-templates](fastddd-templates/) | FreeMarker template library covering the full set of generation templates for PO/Service/Controller/DTO/SQL |
| [fastddd-code-generator](fastddd-code-generator/) | Code generation engine that generates standard DDD code from database table metadata |
| [fastddd-doc-generator](fastddd-doc-generator/) | Database documentation generator that outputs database documentation in Markdown/HTML formats |
| [fastddd-maven-plugin](fastddd-maven-plugin/) | Maven code generation plugin that generates code and documentation in one step from the command line |
| [fastddd-doc-maven-plugin](fastddd-doc-maven-plugin/) | Maven documentation generation plugin integrated into the build process |
| [fastddd-project-archetype](fastddd-project-archetype/) | Maven Archetype project scaffolding for quickly creating a standard DDD project structure |

## Quick Start

### Code Generation

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.7:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/your_db \
  -Dcodegen.username=root \
  -Dcodegen.password=123456 \
  -Dcodegen.basePackage=com.example.project \
  -Dcodegen.tablePrefix=t_
```

### Database Documentation Generation

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.7:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/your_db \
  -Dfastdoc.username=root \
  -Dfastdoc.password=123456 \
  -Dfastdoc.format=both
```

### Project Scaffolding

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.github.gallop.fastddd \
  -DarchetypeArtifactId=fastddd-project-archetype \
  -DarchetypeVersion=1.0.6 \
  -DgroupId=com.example \
  -DartifactId=my-project \
  -Dversion=1.0.0-SNAPSHOT
```
