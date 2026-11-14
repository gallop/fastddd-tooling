# FastDDD Maven Plugin

**English** | [简体中文](README.zh-CN.md)

The Maven plugin for the FastDDD framework, providing code generation, database documentation generation, and more.

## Features

- **Code generation**: Automatically generates Java code from database table structures
- **Documentation generation**: Generates database documentation in Markdown and HTML formats
- **Multi-database support**: Supports mainstream databases such as MySQL, PostgreSQL, Oracle, and SQL Server
- **Highly configurable**: Provides rich configuration options to meet the needs of different projects

## Quick Start

### 1. Add the Plugin Dependency

Add the plugin to your project's `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>io.github.gallop.fastddd</groupId>
            <artifactId>fastddd-maven-plugin</artifactId>
            <version>1.0.0</version>
        </plugin>
    </plugins>
</build>
```

### 2. Code Generation

```bash
mvn fastddd:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/test \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=com.example
```

### 3. Documentation Generation

```bash
mvn fastddd:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/test \
  -Dfastdoc.username=root \
  -Dfastdoc.password=password
```

## Detailed Configuration

### Code Generation Configuration

#### Required Parameters

| Parameter | Description | Example |
|------|------|------|
| `codegen.url` | Database connection URL | `jdbc:mysql://localhost:3306/test` |
| `codegen.username` | Database username | `root` |
| `codegen.password` | Database password | `password` |
| `codegen.basePackage` | Base package name | `com.example` |

#### Optional Parameters

| Parameter | Description | Default Value |
|------|------|--------|
| `codegen.driverClassName` | Database driver class name | Auto-detected |
| `codegen.outputDir` | Output directory | `target/generated-sources/fastddd` |
| `codegen.tables` | Table name list (comma-separated) | All tables |
| `codegen.tablePrefix` | Table name prefix | None |
| `codegen.generateEntity` | Whether to generate entity classes | `true` |
| `codegen.generateRepository` | Whether to generate Repository interfaces | `true` |
| `codegen.generateService` | Whether to generate Service interfaces and implementations | `true` |
| `codegen.generateController` | Whether to generate Controllers | `true` |
| `codegen.generateDto` | Whether to generate DTO classes | `true` |
| `codegen.generateMapper` | Whether to generate Mapper interfaces | `false` |
| `codegen.overwrite` | Whether to overwrite existing files | `false` |
| `codegen.author` | Author information | `FastDDD` |
| `codegen.moduleName` | Module name | Project artifactId |
| `codegen.useLombok` | Whether to use Lombok annotations | `true` |
| `codegen.useSwagger` | Whether to use Swagger annotations | `true` |
| `codegen.useJpa` | Whether to use JPA annotations | `true` |
| `codegen.useValidation` | Whether to use validation annotations | `true` |
| `codegen.modelType` | Model type: `ANEMIC` (anemic model) or `RICH` (rich model) | `ANEMIC` |
| `codegen.enableCQRS` | Whether to enable CQRS mode (only effective when modelType=RICH) | `false` |
| `codegen.language` | Language of generated comments and copy (javadoc, validation messages, log messages, Swagger annotation values): `zh` / `en` (code structure is unaffected) | `zh` |

### Documentation Generation Configuration

#### Required Parameters

| Parameter | Description | Example |
|------|------|------|
| `fastdoc.url` | Database connection URL | `jdbc:mysql://localhost:3306/test` |
| `fastdoc.username` | Database username | `root` |
| `fastdoc.password` | Database password | `password` |

#### Optional Parameters

| Parameter | Description | Default Value |
|------|------|--------|
| `fastdoc.driverClassName` | Database driver class name | Auto-detected |
| `fastdoc.outputDir` | Output directory | `target/fastdoc` |
| `fastdoc.format` | Output format | `both` |
| `fastdoc.includeSystemTables` | Whether to include system tables | `false` |
| `fastdoc.tablePattern` | Table name filter pattern | None |
| `fastdoc.generateSeparateTableDocs` | Whether to generate separate per-table documents | `true` |
| `fastdoc.title` | Document title | Project name + " 数据库文档" |
| `fastdoc.description` | Document description | None |
| `fastdoc.author` | Author information | `FastDDD` |
| `fastdoc.version` | Version information | Project version |

## Usage Examples

### Complete Code Generation Configuration

```xml
<plugin>
    <groupId>io.github.gallop.fastddd</groupId>
    <artifactId>fastddd-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <url>jdbc:mysql://localhost:3306/test</url>
        <username>root</username>
        <password>password</password>
        <basePackage>com.example</basePackage>
        <tables>user,order,product</tables>
        <tablePrefix>t_</tablePrefix>
        <author>gallop</author>
        <useLombok>true</useLombok>
        <useSwagger>true</useSwagger>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate-code</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Complete Documentation Generation Configuration

```xml
<plugin>
    <groupId>io.github.gallop.fastddd</groupId>
    <artifactId>fastddd-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <url>jdbc:mysql://localhost:3306/test</url>
        <username>root</username>
        <password>password</password>
        <format>html</format>
        <title>测试数据库文档</title>
        <description>这是一个测试数据库的文档</description>
        <author>gallop</author>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate-doc</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Command-Line Usage

#### Generate code for specific tables

```bash
mvn fastddd:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/test \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=com.example \
  -Dcodegen.tables=user,order \
  -Dcodegen.tablePrefix=t_ \
  -Dcodegen.author=gallop
```

#### Generate HTML database documentation

```bash
mvn fastddd:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/test \
  -Dfastdoc.username=root \
  -Dfastdoc.password=password \
  -Dfastdoc.format=html \
  -Dfastdoc.title="测试数据库文档" \
  -Dfastdoc.author=gallop
```

## Supported Databases

- MySQL 5.7+
- PostgreSQL 9.6+
- Oracle 11g+
- SQL Server 2012+
- H2 Database

## Model Types

FastDDD supports two domain model types:

### ANEMIC (Anemic Model) - Default

The anemic model is the traditional layered architecture pattern: business logic lives mainly in the Service layer, and entity classes contain only data and simple getters/setters.

**Generated code structure:**
```
src/main/java/
└── com/example/
    ├── entity/          # PO 实体类（继承 BasePO）
    │   ├── UserPO.java
    │   └── OrderPO.java
    ├── repository/      # Repository 接口（继承 BaseMapper）
    │   ├── UserMapper.java
    │   └── OrderMapper.java
    ├── service/         # Service 接口和实现（继承 IService/BaseServiceImpl）
    │   ├── UserService.java
    │   ├── UserServiceImpl.java
    │   ├── OrderService.java
    │   └── OrderServiceImpl.java
    ├── controller/      # Controller 类（继承 BaseController）
    │   ├── UserController.java
    │   └── OrderController.java
    └── dto/            # DTO 类
        ├── UserDTO.java
        └── OrderDTO.java
```

### RICH (Rich Model)

The rich model is the pattern recommended by DDD: business logic is encapsulated inside the entities, data is accessed through the Repository pattern, and CQRS is supported.

**Generated code structure:**
```
src/main/java/
└── com/example/
    ├── domain/          # 领域层
    │   ├── User.java              # 聚合根（继承 AggregateRoot）
    │   ├── UserId.java            # 值对象
    │   ├── UserCreatedEvent.java  # 领域事件
    │   └── ...
    ├── application/     # 应用层
    │   ├── ApplicationService.java   # 应用服务
    │   ├── command/                   # 命令
    │   │   ├── CreateUserCommand.java
    │   │   └── ...
    │   ├── handler/                   # 命令处理器
    │   │   ├── CreateUserCommandHandler.java
    │   │   └── ...
    │   └── query/                     # 查询
    │       ├── GetUserByIdQuery.java
    │       └── ...
    ├── infrastructure/  # 基础设施层
    │   ├── persistence/
    │   │   ├── UserPO.java            # 持久化对象
    │   │   ├── UserMapper.java        # Entity-PO 映射器（MapStruct）
    │   │   ├── UserRepository.java    # Repository 接口
    │   │   └── UserRepositoryImpl.java # Repository 实现
    │   └── config/
    │       └── MapStructConfig.java
    └── interfaces/      # 接口层
        ├── controller/
        │   ├── UserCommandController.java  # 命令控制器
        │   └── UserQueryController.java    # 查询控制器
        └── dto/
            ├── UserDto.java
            └── CreateUserRequest.java
```

### CQRS Pattern

When `enableCQRS=true`, separate command and query classes are generated for each aggregate:

**Commands:**
- Create{Entity}Command - Create an entity
- Update{Entity}Command - Update an entity
- Delete{Entity}Command - Delete an entity

**Queries:**
- Get{Entity}ByIdQuery - Query by ID
- Get{Entity}By{Criteria}Query - Query by criteria

**Controllers:**
- {Entity}CommandController - Handles write operations
- {Entity}QueryController - Handles read operations

### Rich Model Code Generation Examples

#### Generate basic rich-model code

```bash
mvn fastddd:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/test \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=com.example \
  -Dcodegen.modelType=RICH
```

#### Generate rich-model + CQRS code

```bash
mvn fastddd:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/test \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=com.example \
  -Dcodegen.modelType=RICH \
  -Dcodegen.enableCQRS=true
```

#### Configure rich-model generation in the POM

```xml
<plugin>
    <groupId>io.github.gallop.fastddd</groupId>
    <artifactId>fastddd-maven-plugin</artifactId>
    <version>1.0.0</version>
    <configuration>
        <url>jdbc:mysql://localhost:3306/test</url>
        <username>root</username>
        <password>password</password>
        <basePackage>com.example</basePackage>
        <modelType>RICH</modelType>
        <enableCQRS>true</enableCQRS>
        <tables>user,order</tables>
        <author>gallop</author>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>generate-code</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## Generated Documentation Formats

### Markdown Format

- `database_[数据库名]_[时间戳].md` - Database overview document
- `table_[表名]_[时间戳].md` - Detailed document for a single table

### HTML Format

- `database_[数据库名]_[时间戳].html` - Database overview document
- `table_[表名]_[时间戳].html` - Detailed document for a single table

## Getting Help

```bash
# 显示所有可用目标
mvn fastddd:help

# 显示代码生成的详细帮助
mvn fastddd:help -Dgoal=generate-code

# 显示文档生成的详细帮助
mvn fastddd:help -Dgoal=generate-doc
```

## License

This project is licensed under the Apache License 2.0.

## Contributing

Issues and Pull Requests are welcome to help improve this project.

## Contact

- Author: gallop
- Email: gallop@example.com
- Project URL: will be announced in the release notes once the Community public repository is created
