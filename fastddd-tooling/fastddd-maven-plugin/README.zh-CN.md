# FastDDD Maven Plugin

[English](README.md) | **简体中文**

FastDDD框架的Maven插件，提供代码生成、数据库文档生成等功能。

## 功能特性

- **代码生成**: 根据数据库表结构自动生成Java代码
- **文档生成**: 生成数据库文档，支持Markdown和HTML格式
- **多数据库支持**: 支持MySQL、PostgreSQL、Oracle、SQL Server等主流数据库
- **高度可配置**: 提供丰富的配置选项，满足不同项目需求

## 快速开始

### 1. 添加插件依赖

在项目的`pom.xml`文件中添加插件：

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

### 2. 代码生成

```bash
mvn fastddd:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/test \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=com.example
```

### 3. 文档生成

```bash
mvn fastddd:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/test \
  -Dfastdoc.username=root \
  -Dfastdoc.password=password
```

## 详细配置

### 代码生成配置

#### 必需参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `codegen.url` | 数据库连接URL | `jdbc:mysql://localhost:3306/test` |
| `codegen.username` | 数据库用户名 | `root` |
| `codegen.password` | 数据库密码 | `password` |
| `codegen.basePackage` | 基础包名 | `com.example` |

#### 可选参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `codegen.driverClassName` | 数据库驱动类名 | 自动检测 |
| `codegen.outputDir` | 输出目录 | `target/generated-sources/fastddd` |
| `codegen.tables` | 表名列表（逗号分隔） | 所有表 |
| `codegen.tablePrefix` | 表名前缀 | 无 |
| `codegen.generateEntity` | 是否生成实体类 | `true` |
| `codegen.generateRepository` | 是否生成Repository接口 | `true` |
| `codegen.generateService` | 是否生成Service接口和实现 | `true` |
| `codegen.generateController` | 是否生成Controller | `true` |
| `codegen.generateDto` | 是否生成DTO类 | `true` |
| `codegen.generateMapper` | 是否生成Mapper接口 | `false` |
| `codegen.overwrite` | 是否覆盖已存在的文件 | `false` |
| `codegen.author` | 作者信息 | `FastDDD` |
| `codegen.moduleName` | 模块名称 | 项目artifactId |
| `codegen.useLombok` | 是否使用Lombok注解 | `true` |
| `codegen.useSwagger` | 是否使用Swagger注解 | `true` |
| `codegen.useJpa` | 是否使用JPA注解 | `true` |
| `codegen.useValidation` | 是否使用验证注解 | `true` |
| `codegen.modelType` | 模型类型：`ANEMIC`（贫血模型）或 `RICH`（充血模型） | `ANEMIC` |
| `codegen.enableCQRS` | 是否启用CQRS模式（仅当modelType=RICH时有效） | `false` |
| `codegen.language` | 生成注释/文案语言（javadoc、校验消息、日志消息、Swagger 注解值）：`zh` / `en`（不影响代码结构） | `zh` |

### 文档生成配置

#### 必需参数

| 参数 | 说明 | 示例 |
|------|------|------|
| `fastdoc.url` | 数据库连接URL | `jdbc:mysql://localhost:3306/test` |
| `fastdoc.username` | 数据库用户名 | `root` |
| `fastdoc.password` | 数据库密码 | `password` |

#### 可选参数

| 参数 | 说明 | 默认值 |
|------|------|--------|
| `fastdoc.driverClassName` | 数据库驱动类名 | 自动检测 |
| `fastdoc.outputDir` | 输出目录 | `target/fastdoc` |
| `fastdoc.format` | 输出格式 | `both` |
| `fastdoc.includeSystemTables` | 是否包含系统表 | `false` |
| `fastdoc.tablePattern` | 表名过滤模式 | 无 |
| `fastdoc.generateSeparateTableDocs` | 是否生成单独的表文档 | `true` |
| `fastdoc.title` | 文档标题 | 项目名称 + " 数据库文档" |
| `fastdoc.description` | 文档描述 | 无 |
| `fastdoc.author` | 作者信息 | `FastDDD` |
| `fastdoc.version` | 版本信息 | 项目版本 |

## 使用示例

### 完整的代码生成配置

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

### 完整的文档生成配置

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

### 命令行使用

#### 生成指定表的代码

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

#### 生成HTML格式的数据库文档

```bash
mvn fastddd:generate-doc \
  -Dfastdoc.url=jdbc:mysql://localhost:3306/test \
  -Dfastdoc.username=root \
  -Dfastdoc.password=password \
  -Dfastdoc.format=html \
  -Dfastdoc.title="测试数据库文档" \
  -Dfastdoc.author=gallop
```

## 支持的数据库

- MySQL 5.7+
- PostgreSQL 9.6+
- Oracle 11g+
- SQL Server 2012+
- H2 Database

## 模型类型说明

FastDDD 支持两种领域模型类型：

### ANEMIC（贫血模型）- 默认

贫血模型是传统的分层架构模式，业务逻辑主要在 Service 层，实体类只包含数据和简单的 getter/setter。

**生成的代码结构：**
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

### RICH（充血模型）

充血模型是 DDD 推荐的模式，业务逻辑封装在实体内部，通过 Repository 模式访问数据，支持 CQRS。

**生成的代码结构：**
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

### CQRS 模式

当 `enableCQRS=true` 时，会为每个聚合生成独立的命令和查询类：

**命令（Command）：**
- Create{Entity}Command - 创建实体
- Update{Entity}Command - 更新实体
- Delete{Entity}Command - 删除实体

**查询（Query）：**
- Get{Entity}ByIdQuery - 根据ID查询
- Get{Entity}By{Criteria}Query - 根据条件查询

**控制器：**
- {Entity}CommandController - 处理写操作
- {Entity}QueryController - 处理读操作

### 生成充血模型代码示例

#### 生成基础充血模型代码

```bash
mvn fastddd:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/test \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=com.example \
  -Dcodegen.modelType=RICH
```

#### 生成充血模型 + CQRS 代码

```bash
mvn fastddd:generate-code \
  -Dcodegen.url=jdbc:mysql://localhost:3306/test \
  -Dcodegen.username=root \
  -Dcodegen.password=password \
  -Dcodegen.basePackage=com.example \
  -Dcodegen.modelType=RICH \
  -Dcodegen.enableCQRS=true
```

#### 在 POM 中配置充血模型生成

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

## 生成的文档格式

### Markdown格式

- `database_[数据库名]_[时间戳].md` - 数据库总览文档
- `table_[表名]_[时间戳].md` - 单个表的详细文档

### HTML格式

- `database_[数据库名]_[时间戳].html` - 数据库总览文档
- `table_[表名]_[时间戳].html` - 单个表的详细文档

## 获取帮助

```bash
# 显示所有可用目标
mvn fastddd:help

# 显示代码生成的详细帮助
mvn fastddd:help -Dgoal=generate-code

# 显示文档生成的详细帮助
mvn fastddd:help -Dgoal=generate-doc
```

## 许可证

本项目采用 Apache License 2.0 许可证。

## 贡献

欢迎提交Issue和Pull Request来帮助改进这个项目。

## 联系方式

- 作者: gallop
- 邮箱: gallop@example.com
- 项目地址：将在 Community 公开仓库创建后随发布说明公布
