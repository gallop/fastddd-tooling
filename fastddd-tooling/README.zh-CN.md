# FastDDD Tooling

[English](README.md) | **简体中文**

工具层提供代码生成、文档生成、Maven 插件和项目脚手架等开发效率工具。

## 模块列表

| 模块 | 说明 |
|------|------|
| [fastddd-templates](fastddd-templates/) | FreeMarker 模板库，覆盖 PO/Service/Controller/DTO/SQL 的全套生成模板 |
| [fastddd-code-generator](fastddd-code-generator/) | 代码生成引擎，从数据库表元数据生成标准 DDD 代码 |
| [fastddd-doc-generator](fastddd-doc-generator/) | 数据库文档生成器，输出 Markdown/HTML 格式的数据库文档 |
| [fastddd-maven-plugin](fastddd-maven-plugin/) | Maven 代码生成插件，支持命令行一键生成代码和文档 |
| [fastddd-doc-maven-plugin](fastddd-doc-maven-plugin/) | Maven 文档生成插件，集成到构建流程 |
| [fastddd-project-archetype](fastddd-project-archetype/) | Maven Archetype 项目脚手架，快速创建标准 DDD 项目结构 |

## 快速使用

### 代码生成

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.6:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/your_db \
  -Dcodegen.username=root \
  -Dcodegen.password=123456 \
  -Dcodegen.basePackage=com.example.project \
  -Dcodegen.tablePrefix=t_
```

### 数据库文档生成

```bash
mvn io.github.gallop.fastddd:fastddd-maven-plugin:1.0.6:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/your_db \
  -Dfastdoc.username=root \
  -Dfastdoc.password=123456 \
  -Dfastdoc.format=both
```

### 项目脚手架

```bash
mvn archetype:generate \
  -DarchetypeGroupId=io.github.gallop.fastddd \
  -DarchetypeArtifactId=fastddd-project-archetype \
  -DarchetypeVersion=1.0.6 \
  -DgroupId=com.example \
  -DartifactId=my-project \
  -Dversion=1.0.0-SNAPSHOT
```
