# FastDoc - Database Documentation Generation Tool

**English** | [简体中文](README.zh-CN.md)

FastDoc is the database documentation generation tool in the FastDDD framework. It automatically extracts metadata from a database and generates polished documentation in Markdown and HTML formats.

## Features

- 🚀 **Automated documentation generation**: Automatically extracts table structures, column information, indexes, foreign keys, and other metadata from the database
- 📊 **Multiple output formats**: Supports documentation output in both Markdown and HTML
- 🎨 **Beautiful styling**: Ships with polished built-in HTML styles for professional-grade documents
- 🔧 **Highly configurable**: Supports custom output directories, document titles, author information, and more
- 📱 **Responsive design**: Generated HTML documents are viewable on mobile devices
- 🗂️ **Batch export**: Supports exporting documentation for an entire database or a single table
- 🔍 **Smart filtering**: Automatically identifies and skips system tables, with table name filtering support

## Supported Databases

- MySQL 5.7+
- PostgreSQL 9.6+
- Oracle 11g+
- SQL Server 2012+
- H2 Database
- SQLite

## Quick Start

### 1. REST API

FastDoc provides a complete REST API for generating and downloading documentation over HTTP.

#### Get database metadata
```http
GET /api/fastdoc/metadata
```

#### Generate a Markdown document for the database
```http
GET /api/fastdoc/database/markdown?title=我的数据库文档&author=gallop
```

#### Generate an HTML document for the database
```http
GET /api/fastdoc/database/html?title=我的数据库文档&author=gallop
```

#### Generate documentation for a specific table
```http
GET /api/fastdoc/table/{tableName}/markdown
GET /api/fastdoc/table/{tableName}/html
```

#### Export documentation to the file system
```http
POST /api/fastdoc/export/database
Content-Type: application/json

{
    "outputDir": "/path/to/output",
    "format": "html",
    "title": "数据库文档",
    "author": "gallop"
}
```

### 2. Maven Plugin

Add the FastDDD Maven plugin to your project's `pom.xml`:

```xml
<plugin>
    <groupId>io.github.gallop.fastddd</groupId>
    <artifactId>fastddd-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <!-- 数据库连接配置 -->
        <jdbcUrl>jdbc:mysql://localhost:3306/your_database</jdbcUrl>
        <username>your_username</username>
        <password>your_password</password>
        <driverClassName>com.mysql.cj.jdbc.Driver</driverClassName>
        
        <!-- 文档生成配置 -->
        <outputDir>target/fastdoc</outputDir>
        <outputFormat>html</outputFormat>
        <title>我的数据库文档</title>
        <description>这是一个自动生成的数据库文档</description>
        <author>gallop</author>
        <version>1.0.0</version>
        
        <!-- 可选配置 -->
        <includeSystemTables>false</includeSystemTables>
        <tableNamePattern>user_*,order_*</tableNamePattern>
        <generateSeparateTableDocs>true</generateSeparateTableDocs>
    </configuration>
</plugin>
```

#### Generate documentation
```bash
# 生成数据库文档
mvn fastddd:generate-doc

# 生成代码和文档
mvn fastddd:generate-code fastddd:generate-doc
```

### 3. Programmatic Usage

```java
@Autowired
private FastDocService fastDocService;

@Autowired
private FastDocTemplateService templateService;

@Autowired
private FastDocExportService exportService;

// 获取数据库元数据
DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();

// 生成 Markdown 文档
String markdownDoc = templateService.generateDatabaseMarkdown(metadata, "数据库文档", "gallop");

// 生成 HTML 文档
String htmlDoc = templateService.generateDatabaseHtml(metadata, "数据库文档", "gallop");

// 导出到文件
FastDocExportService.ExportResult result = exportService.exportDatabaseDoc(
    "/path/to/output", "html", "数据库文档", "gallop"
);
```

## Configuration Reference

### Maven Plugin Configuration Parameters

| Parameter | Type | Required | Default | Description |
|--------|------|------|--------|------|
| jdbcUrl | String | Yes | - | Database connection URL |
| username | String | Yes | - | Database username |
| password | String | Yes | - | Database password |
| driverClassName | String | Yes | - | Database driver class name |
| outputDir | String | No | target/fastdoc | Output directory |
| outputFormat | String | No | html | Output format (html/markdown) |
| title | String | No | 数据库文档 | Document title |
| description | String | No | - | Document description |
| author | String | No | FastDoc | Document author |
| version | String | No | 1.0.0 | Document version |
| includeSystemTables | Boolean | No | false | Whether to include system tables |
| tableNamePattern | String | No | - | Table name filter pattern (comma-separated, wildcards supported) |
| generateSeparateTableDocs | Boolean | No | false | Whether to generate separate per-table documents |

### REST API Parameters

#### Query Parameters
- `title`: Document title
- `author`: Document author
- `includeSystemTables`: Whether to include system tables (true/false)
- `tablePattern`: Table name filter pattern

#### Request Body Parameters (export endpoints)
```json
{
    "outputDir": "输出目录路径",
    "format": "输出格式（html/markdown）",
    "title": "文档标题",
    "author": "文档作者",
    "description": "文档描述",
    "version": "文档版本",
    "includeSystemTables": false,
    "tableNamePattern": "表名过滤模式",
    "generateSeparateTableDocs": true
}
```

## HTTP Export Security (Trusted Root Directory)

Since ECO-RELEASE-04 (2026-09-07), the `exportDir` request parameter and the `tableName` path variable
of the REST export endpoints (`POST /api/fastdoc/database/export`,
`POST /api/fastdoc/table/{tableName}/export`, `POST /api/fastdoc/tables/export`) are treated as
**untrusted input**. Before any directory creation or file write takes place, they are validated
against the trusted root (`FastDocExportPathGuard`), and writes are pinned inside the trusted root.
On validation failure the endpoints return 400 with `{"success":false,"message":...}`. This boundary
does not rely on method-level security annotations — even if an endpoint is configured as permitAll,
out-of-root writes are still rejected.

**Configuration**:

```yaml
fastddd:
  fastdoc:
    export:
      root-dir: /var/lib/myapp/fastdoc-export   # 可信根；缺省为 ${user.dir}/fastdoc-export
```

**Validation rules**: `exportDir` only allows relative subdirectories inside the trusted root;
absolute paths, drive letters (including drive-relative forms such as `C:x`), UNC prefixes, `../`
traversal, and paths that resolve outside the root via symbolic links/junctions are all rejected
(including a real-path check requiring the `toRealPath` of existing path segments to fall within
the trusted root's real path). `tableName` rejects path separators, `..` fragments, control
characters, and excessive length.

**Migration notes (change in security default behavior)**:
- The default export location when `exportDir` is not sent is unchanged (still `fastdoc-export`
  under the process working directory);
- The previous usage of "pointing at any directory via an absolute path or `../`" is now rejected
  (returns 400) — to export elsewhere, have operations configure the trusted root via `root-dir`,
  and pass only relative subdirectories over HTTP;
- Relative subdirectories are now resolved under the trusted root (instead of under the process
  working directory);
- The Maven plugin and the programmatic approach (local/CLI trusted configuration calling
  `FastDocExportService` directly) are unaffected by this constraint.

## Generated Documentation Structure

### HTML Document Features
- 📱 Responsive design, viewable on mobile devices
- 🎨 Modern UI design
- 🔍 Table search and sorting
- 📊 Database statistics
- 🗂️ Table of contents navigation
- 💡 Column type highlighting

### Markdown Document Features
- 📝 Standard Markdown format
- 📊 Column information presented as tables
- 🔗 Internal link navigation
- 📋 Complete index and foreign key information

### Document Contents
1. **Basic database information**
   - Database name and type
   - Version information
   - Connection information
   - Generation time

2. **Statistics**
   - Total number of tables
   - Total number of columns
   - Total number of indexes
   - Total number of foreign keys

3. **Detailed table information**
   - Table name and comment
   - Column list (name, type, length, nullable, default value, comment)
   - Primary key information
   - Index information
   - Foreign key relationships

## Usage Examples

### Example 1: Generate complete database documentation

```bash
mvn fastddd:generate-doc \
  -DjdbcUrl=jdbc:mysql://localhost:3306/ecommerce \
  -Dusername=root \
  -Dpassword=123456 \
  -DoutputFormat=html \
  -Dtitle="电商系统数据库文档" \
  -Dauthor="开发团队"
```

### Example 2: Generate documentation only for user-related tables

```xml
<configuration>
    <jdbcUrl>jdbc:mysql://localhost:3306/ecommerce</jdbcUrl>
    <username>root</username>
    <password>123456</password>
    <tableNamePattern>user_*,customer_*</tableNamePattern>
    <title>用户模块数据库文档</title>
</configuration>
```

### Example 3: Generate Markdown documentation

```bash
curl -X GET "http://localhost:8080/api/fastdoc/database/markdown?title=API文档&author=gallop" \
  -H "Accept: application/octet-stream" \
  --output database-doc.md
```

### Example 4: Batch export documentation for all tables

```bash
curl -X POST "http://localhost:8080/api/fastdoc/export/tables" \
  -H "Content-Type: application/json" \
  -d '{
    "outputDir": "/tmp/docs",
    "format": "html",
    "generateSeparateTableDocs": true
  }'
```

## Best Practices

1. **Version control**: Commit the generated documentation to version control to track database structure changes
2. **Automation integration**: Integrate documentation generation into your CI/CD pipeline so documentation is always up to date
3. **Regular updates**: It is recommended to update the documentation promptly after database structure changes
4. **Team sharing**: Deploy the generated HTML documentation to an internal documentation site for easy team access

## Troubleshooting

### Common Issues

1. **Database connection failure**
   - Check that the database connection parameters are correct
   - Confirm that the database service is running
   - Verify that the user has sufficient privileges

2. **Generated documentation is empty**
   - Check whether you have permission to access the database metadata
   - Confirm that the table name filter pattern is correct
   - Verify that tables exist in the database

3. **Garbled Chinese characters**
   - Make sure the database connection URL includes the correct character encoding parameters
   - For example: `jdbc:mysql://localhost:3306/db?useUnicode=true&characterEncoding=utf8`

### Debug Logging

Enable debug logging:
```properties
logging.level.com.gallop.fastddd.web.fastdoc=DEBUG
```

## License

FastDoc is licensed under Apache 2.0.

## Contributing

Issues and Pull Requests are welcome to help improve FastDoc!

## Contact

- Author: gallop
- Email: gallop@example.com
- Project URL: will be announced in the release notes once the Community public repository is created
