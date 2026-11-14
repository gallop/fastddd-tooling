# FastDDD 模板开发指南

> FastDDD 统一模板库 - 模板开发规范和最佳实践
>
> 版本：1.0.0
> 更新时间：2025-01-24

---

## 📚 目录

- [概述](#概述)
- [模板引擎](#模板引擎)
- [目录结构](#目录结构)
- [模板变量](#模板变量)
- [公共模板片段](#公共模板片段)
- [开发规范](#开发规范)
- [最佳实践](#最佳实践)
- [常见问题](#常见问题)

---

## 概述

FastDDD 统一模板库为以下工具提供一致的代码生成能力：

| 工具 | 用途 | 模板位置 |
|------|------|----------|
| **fastddd-code-generator** | 从数据库生成 CRUD 代码 | `fastddd-templates` |
| **Web Initializr** | Web 界面生成项目 | `fastddd-templates` |
| **Maven Plugin** | 命令行生成代码 | `fastddd-templates` |
| **Maven Archetype** | Maven 原型生成项目 | `archetype-resources` (Velocity) |

**目标**：确保所有工具生成的代码风格一致，减少维护成本。

---

## 模板引擎

### FreeMarker

**优势**：
- 功能强大，支持复杂的逻辑控制
- 与 Spring Boot 深度集成
- 性能优异，支持模板缓存
- 社区活跃，文档完善

**基本语法**：

```ftl
<#-- 注释 -->
<#-- 这是注释，不会出现在输出中 -->

<#-- 变量输出 -->
${packageName}

<#-- 条件判断 -->
<#if modelType == "RICH">
    // RICH 模型代码
<#elseif modelType == "ANEMIC">
    // ANEMIC 模型代码
<#else>
    // 默认代码
</#if>

<#-- 循环 -->
<#list modules as module>
    - ${module.name}
</#list>

<#-- 宏定义 -->
<#macro greet name>
    Hello, ${name}!
</#macro>

<#-- 调用宏 -->
<@greet name="World" />

<#-- 赋值 -->
<#assign className = "User" />

<#-- 空值处理 -->
${description!"默认描述"}

<#-- 函数调用 -->
${packageName?replace('.', '/')}

<#-- 默认值 -->
${author!"FastDDD Generator"}
```

---

## 目录结构

```
fastddd-templates/
├── pom.xml                          # Maven 配置
├── src/main/
│   ├── java/com/gallop/fastddd/templates/
│   │   └── TemplateContext.java     # 统一模板上下文
│   └── resources/templates/
│       ├── common/                   # 公共模板片段
│       │   ├── header.ftl           # 文件头注释
│       │   ├── imports.ftl          # 通用导入
│       │   ├── class-comment.ftl    # 类注释
│       │   ├── field.ftl            # 字段定义
│       │   ├── getter-setter.ftl    # Getter/Setter
│       │   └── toString.ftl         # ToString
│       ├── project/                 # 项目级模板
│       │   ├── pom.xml.ftl         # Maven POM
│       │   ├── application.yml.ftl # 应用配置
│       │   ├── Application.java.ftl # 启动类
│       │   └── README.md.ftl       # 项目说明
│       ├── anemic/                 # 贫血模型模板
│       │   ├── Entity.ftl          # 实体类
│       │   ├── Service.ftl         # 服务接口
│       │   ├── ServiceImpl.ftl     # 服务实现
│       │   ├── Controller.ftl      # 控制器
│       │   └── Mapper.ftl          # MyBatis Mapper
│       └── rich/                   # 充血模型模板
│           ├── AggregateRoot.ftl   # 聚合根
│           ├── Command.ftl         # 命令
│           ├── CommandHandler.ftl  # 命令处理器
│           ├── Query.ftl           # 查询
│           ├── QueryHandler.ftl    # 查询处理器
│           ├── EntityRepository.ftl # 仓储接口
│           └── EntityMapper.ftl    # 实体映射器
└── docs/
    └── TEMPLATE_DEVELOPMENT_GUIDE.md  # 本文档
```

---

## 模板变量

### 核心变量

| 变量名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| `groupId` | String | Maven Group ID | `com.example` |
| `artifactId` | String | Maven Artifact ID | `my-fastddd-app` |
| `version` | String | 项目版本 | `1.0.0-SNAPSHOT` |
| `packageName` | String | 基础包名 | `com.example.app` |
| `modelType` | String | 模型类型 | `ANEMIC`, `RICH` |
| `databaseType` | String | 数据库类型 | `MYSQL`, `POSTGRESQL`, `H2` |
| `fastdddVersion` | String | FastDDD 版本 | `1.0.0-jdk21-SNAPSHOT` |
| `javaVersion` | String | Java 版本 | `21` |
| `author` | String | 作者 | `FastDDD Generator` |
| `date` | String | 日期 | `2025-01-24` |

### 包路径变量

| 变量名 | 说明 | ANEMIC 示例 | RICH 示例 |
|--------|------|-------------|----------|
| `entityPackage` | 实体包名 | `com.example.app.entity` | `com.example.app.infrastructure.persistence` |
| `servicePackage` | 服务包名 | `com.example.app.service` | - |
| `controllerPackage` | 控制器包名 | `com.example.app.controller` | - |
| `domainPackage` | 领域包名 | - | `com.example.app.domain` |
| `applicationPackage` | 应用包名 | - | `com.example.app.application` |
| `infrastructurePackage` | 基础设施包名 | - | `com.example.app.infrastructure.persistence` |
| `interfacesPackage` | 接口包名 | - | `com.example.app.interfaces` |

### 配置变量

| 变量名 | 类型 | 说明 |
|--------|------|------|
| `enableCQRS` | boolean | 是否启用 CQRS |
| `useLombok` | boolean | 是否使用 Lombok |
| `useSwagger` | boolean | 是否使用 Swagger |
| `useValidation` | boolean | 是否使用 Jakarta Validation |
| `modules` | Set<String> | 选中的模块列表 |
| `connectionPool` | String | 生成项目连接池：`HIKARI`（缺省）或 `DRUID`（ECO-007A 起渲染对应池依赖，见下） |

#### 连接池渲染契约（ECO-007A）

`project/pom.xml.ftl` 对数据库形态（`databaseType != "NONE"`）显式渲染池依赖，双分支互斥：

- `HIKARI`（缺省）：`spring-boot-starter-jdbc`（版本经 Boot parent 受管）。C4 依赖边界整改后
  `fast-ddd-crud` 不再传递任何池实现，数据库形态的生成项目不得依赖 `fast-ddd-event` 等模块的
  偶然传递获得 HikariCP；
- `DRUID`：`druid-spring-boot-3-starter` + `<druid.version>` 属性（对齐 fastddd-bom 受管值）。
  生成项目以 Boot parent 为父、不经 BOM 受管，依赖必须钉版本（模板内以 `${r"${druid.version}"}` raw
  string 引用属性）。

当前三通道（init-project / archetype / Studio）均缺省 HIKARI，无入口喂入 DRUID；显式 DRUID 形态的
可构建/可运行验证走验证侧渲染（`TemplateContext.connectionPool` setter + `toTemplateVariables()`，
注意该输出面不含 `enableEvent` 变量，pom 渲染需另行补喂）。

#### 监控阈值契约（ECO-007A）

`fastddd.monitor.sql.slow-query-threshold` 在模板中必须为**纯数值毫秒**（如 `slow-query-threshold: 1000`）：
框架 `SqlMonitoringProperties.slowQueryThreshold` 为 `long`（毫秒），不接 Spring Duration 单位后缀——
`1000ms` 字面量会在 `@ConfigurationProperties` 绑定期抛 NumberFormatException，导致生成项目自带配置
启动即失败。

### 使用 TemplateContext

```java
// 创建模板上下文
TemplateContext context = TemplateContext.builder()
    .groupId("com.example")
    .artifactId("my-app")
    .packageName("com.example.app")
    .modelType(ModelType.RICH)
    .databaseType(DatabaseType.MYSQL)
    .enableCQRS(true)
    .useLombok(true)
    .useSwagger(true)
    .build();

// 计算包路径
context.calculatePackages();

// 转换为 FreeMarker 变量
Map<String, Object> variables = context.toTemplateVariables();

// 渲染模板
Template template = cfg.getTemplate("rich/AggregateRoot.ftl");
String result = FreeMarkerTemplateUtils.processTemplateIntoString(template, variables);
```

---

## 公共模板片段

### 1. header.ftl - 文件头注释

```ftl
<#include "/common/header.ftl"/>

/**
 * ${description!""}
 *
 * @author ${author}
 * @date ${date!""}
 */
```

### 2. imports.ftl - 通用导入

```ftl
<#-- 设置导入开关 -->
<#assign useLombok = true>
<#assign useSwagger = true>
<#assign useMyBatisPlus = true>

<#-- 引入导入模板 -->
<#include "/common/imports.ftl"/>
```

### 3. class-comment.ftl - 类注释

```ftl
<#include "/common/class-comment.ftl"/>

/**
 * ${className} - ${description}
 *
 * @author ${author}
 * @date ${date}
 */
```

### 4. field.ftl - 字段定义

```ftl
<@fieldDef
    fieldName="userName"
    fieldType="String"
    fieldComment="用户名"
    isRequired=true
    maxLength=50
/>
```

### 5. getter-setter.ftl - Getter/Setter

```ftl
<@getterSetter fieldName="userName" fieldType="String" />
```

### 6. toString.ftl - ToString 方法

```ftl
<@toStringMethod className="User" fields="id,userName,email" />
```

---

## 开发规范

### 命名规范

| 类型 | 命名规则 | 示例 |
|------|----------|------|
| 模板文件 | 小写 + 点分隔符 | `Entity.ftl`, `pom.xml.ftl` |
| 模板变量 | camelCase | `packageName`, `modelType` |
| 宏名称 | camelCase | `fieldDef`, `getterSetter` |
| 包目录 | 小写 | `common/`, `project/`, `rich/` |

### 注释规范

```ftl
<#--
  模板说明注释
  包含：用途、参数、使用方法、示例

  模板名称：xxx.ftl
  作者：xxx
  创建时间：xxxx-xx-xx
-->
```

### 缩进规范

- 使用 **2 空格** 缩进
- 不使用 Tab
- 生成代码遵循 Java 代码规范（4 空格缩进）

### 空行规范

- 模板顶部保留 1 个空行（用于版权声明）
- 逻辑块之间保留 1 个空行
- 生成代码的类之间保留 2 个空行

---

## 最佳实践

### 1. 使用 `!` 处理空值

```ftl
<#-- 推荐 -->
${description!"默认描述"}

<#-- 不推荐 -->
${description}
```

### 2. 使用 `?has_content` 检查集合

```ftl
<#if fields?has_content>
    <#list fields as field>
        private ${field.type} ${field.name};
    </#list>
</#if>
```

### 3. 使用 `?cap_first` 和 `?lower_case`

```ftl
${packageName?lower_case}          // com.example.app -> com/example/app
${className?cap_first}             // user -> User
${field.name?cap_first}            // userName -> UserName
```

### 4. 使用宏提取公共逻辑

```ftl
<#-- 定义宏 -->
<#macro fieldDef fieldName fieldType>
    private ${fieldType} ${fieldName};
</#macro>

<#-- 使用宏 -->
<@fieldDef fieldName="id" fieldType="Long" />
<@fieldDef fieldName="name" fieldType="String" />
```

### 5. 使用 `<#include>` 复用模板

```ftl
<#include "/common/header.ftl"/>
<#include "/common/imports.ftl"/>
```

### 6. 条件判断使用明确的值

```ftl
<#-- 推荐 -->
<#if modelType == "RICH">
    // RICH 代码
</#if>

<#-- 不推荐（依赖隐式转换）-->
<#if modelType == "RICH">
    // RICH 代码
</#if>
```

### 7. 模板缓存配置

```java
@Configuration
public class FreeMarkerConfig {

    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);

        // 启用模板缓存（生产环境）
        cfg.setTemplateUpdateDelayMilliseconds(3600000); // 1小时

        // 开发环境可以设置较短的延迟
        // cfg.setTemplateUpdateDelayMilliseconds(1000); // 1秒

        return cfg;
    }
}
```

---

## 常见问题

### Q1: 模板变量找不到

**问题**：`${packageName}` 输出为空

**解决**：
```ftl
<#-- 检查变量是否存在 -->
<#if packageName??>
    ${packageName}
<#else>
    ${packageName!"com.example.app"}
</#if>
```

### Q2: NPE 异常

**问题**：调用 `package.name` 时 `package` 为 null

**解决**：
```ftl
<#-- 使用安全导航 -->
${package.name!"default"}

<#-- 或先检查 -->
<#if package??>
    ${package.name}
</#if>
```

### Q3: 宏参数传递错误

**问题**：宏参数未正确传递

**解决**：
```ftl
<#-- 定义时指定参数 -->
<#macro myMacro param1 param2="">
    ${param1}
    ${param2!"default"}
</#macro>

<#-- 调用时传递参数 -->
<@myMacro param1="value1" param2="value2" />
```

### Q4: 循环索引越界

**问题**：`${list[list.size()]}` 越界

**解决**：
```ftl
<#list items as item>
    ${item?index}           <!-- 从 0 开始 -->
    ${item?counter}         <!-- 从 1 开始 -->
    ${item?has_next}        <!-- 是否有下一个 -->
    ${item?is_first}        <!-- 是否第一个 -->
    ${item?is_last}         <!-- 是否最后一个 -->
</#list>
```

### Q5: 字符串处理

**常用字符串方法**：
```ftl
${packageName?replace('.', '/')}           <!-- 替换 -->
${packageName?substring(0, 3)}             <!-- 截取 -->
${packageName?upper_case}                   <!-- 大写 -->
${packageName?lower_case}                   <!-- 小写 -->
${packageName?cap_first}                    <!-- 首字母大写 -->
${packageName?uncap_first}                  <!-- 首字母小写 -->
${packageName?length}                       <!-- 长度 -->
${packageName?starts_with("com")}           <!-- 前缀检查 -->
${packageName?ends_with("app")}             <!-- 后缀检查 -->
${packageName?contains("example")}          <!-- 包含检查 -->
${packageName?split('.')}                   <!-- 分割 -->
```

---

## 模板示例

### 实体类模板（贫血模型）

```ftl
package ${entityPackage};

<#assign useLombok = true>
<#assign useSwagger = true>
<#assign useBasePO = true>
<#assign useMyBatisPlus = true>

<#include "/common/imports.ftl"/>

/**
 * ${table.comment}实体类
 *
 * @author ${author}
 * @date ${date}
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("${table.name}")
@ApiModel(value = "${table.entityName}对象", description = "${table.comment}")
public class ${table.entityName} extends BasePO<Long> {

<#list table.columns as column>
    <#if column.name != "id" && column.name != "createTime" && column.name != "updateTime">
    /**
     * ${column.comment}
     */
    @ApiModelProperty(value = "${column.comment}")
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
<#if !useLombok>
    <#include "/common/getter-setter.ftl"/>
</#if>
}
```

### 聚合根模板（充血模型）

```ftl
package ${domainPackage};

<#assign useLombok = true>
<#assign useSwagger = true>
<#assign useAggregateRoot = true>

<#include "/common/imports.ftl"/>

/**
 * ${table.comment}聚合根
 *
 * @author ${author}
 * @date ${date}
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "${entityName}聚合根", description = "${table.comment}")
public class ${entityName} extends AggregateRoot<Long> {

    private static final long serialVersionUID = 1L;

<#list table.columns as column>
    <#if column.name != "id" && column.name != "createdTime" && column.name != "updatedTime">
    /**
     * ${column.comment}
     */
    @ApiModelProperty(value = "${column.comment}")
    private ${column.javaType} ${column.fieldName};

    </#if>
</#list>
    /**
     * 创建${table.comment}
     */
    public static ${entityName} create(<#list table.columns as column><#if column.name != "id" && column.name != "createdTime">${column.javaType} ${column.fieldName}<#if column_has_next>, </#if></#if></#list>) {
        ${entityName} ${lowerEntityName} = new ${entityName}();

<#list table.columns as column>
    <#if column.name != "id" && column.name != "createdTime">
        ${lowerEntityName}.${column.fieldName} = ${column.fieldName};
    </#if>
</#list>

        ${lowerEntityName}.addDomainEvent(new ${entityName}CreatedEvent(${lowerEntityName}.getId()));

        return ${lowerEntityName};
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }
}
```

---

## 模板测试

### 单元测试

```java
@SpringBootTest
public class EntityTemplateTest {

    @Autowired
    private Configuration freemarkerConfig;

    @Test
    public void testEntityTemplate() throws Exception {
        // 准备数据
        Map<String, Object> data = new HashMap<>();
        data.put("packageName", "com.example.app");
        data.put("entityPackage", "com.example.app.entity");
        data.put("table", createMockTable());

        // 渲染模板
        Template template = freemarkerConfig.getTemplate("anemic/Entity.ftl");
        StringWriter writer = new StringWriter();
        template.process(data, writer);

        // 验证结果
        String result = writer.toString();
        assertThat(result).contains("package com.example.app.entity;");
        assertThat(result).contains("public class User extends BasePO");
    }

    private TableMetadata createMockTable() {
        // 创建模拟表元数据
        return TableMetadata.builder()
            .name("t_user")
            .entityName("User")
            .comment("用户表")
            .columns(Arrays.asList(
                ColumnMetadata.builder().name("id").fieldName("id").javaType("Long").build(),
                ColumnMetadata.builder().name("user_name").fieldName("userName").javaType("String").comment("用户名").build()
            ))
            .build();
    }
}
```

---

## 相关资源

- **FreeMarker 官方文档**: https://freemarker.apache.org/docs/
- **FreeMarker 中文文档**: http://freemarker.foolproofit.cn/
- **Java 代码规范**: https://google.github.io/styleguide/javaguide.html
- **FastDDD 框架文档**: `CLAUDE.md`

---

## 版本历史

| 版本 | 日期 | 变更说明 |
|------|------|----------|
| 1.0.0 | 2025-01-24 | 初始版本，定义模板结构和开发规范 |

---

**维护者**: FastDDD 团队
**反馈渠道**: GitHub Issues
**许可证**: Apache License 2.0
