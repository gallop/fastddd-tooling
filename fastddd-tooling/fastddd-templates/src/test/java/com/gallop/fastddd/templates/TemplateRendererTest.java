package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 模板渲染器测试
 *
 * @author FastDDD Generator
 * @since 1.0.0
 */
class TemplateRendererTest {

    private TemplateRenderer renderer;
    private TemplateContext context;

    @BeforeEach
    void setUp() throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(TemplateContext.class, "/templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        // L4：注入文案字典（测试走缺省 zh，与历史断言的生成结果一致）
        TemplateMessages.apply(configuration, TemplateMessages.DEFAULT_LANGUAGE);

        renderer = new TemplateRenderer(configuration);

        // 创建测试上下文
        TemplateContext.TemplateContextBuilder builder = TemplateContext.builder()
            .groupId("com.test")
            .artifactId("test-app")
            .version("1.0.0-SNAPSHOT")
            .packageName("com.test.app")
            .projectName("Test Application")
            .projectDescription("Test Application Description")
            .modelType(TemplateContext.ModelType.ANEMIC)
            .databaseType(TemplateContext.DatabaseType.MYSQL)
            .modules(new HashSet<>(Arrays.asList("web", "crud", "cache")))
            .enableCQRS(false)
            .fastdddVersion("1.0.0-jdk21-SNAPSHOT")
            .javaVersion("21")
            .springBootVersion("3.5.16")
            .author("Test Author")
            .date("2025-01-24");

        // 先构建上下文，再计算包路径
        context = builder.build();
        context = context.calculatePackages();

        // 模拟表元数据
        Map<String, Object> table = new LinkedHashMap<>();
        table.put("name", "t_user");
        table.put("entityName", "User");
        table.put("comment", "用户表");

        List<Map<String, Object>> columns = new ArrayList<>();

        Map<String, Object> idColumn = new LinkedHashMap<>();
        idColumn.put("columnName", "id");
        idColumn.put("fieldName", "id");
        idColumn.put("javaType", "Long");
        idColumn.put("comment", "主键ID");
        columns.add(idColumn);

        Map<String, Object> nameColumn = new LinkedHashMap<>();
        nameColumn.put("columnName", "user_name");
        nameColumn.put("fieldName", "userName");
        nameColumn.put("javaType", "String");
        nameColumn.put("comment", "用户名");
        columns.add(nameColumn);

        Map<String, Object> emailColumn = new LinkedHashMap<>();
        emailColumn.put("columnName", "email");
        emailColumn.put("fieldName", "email");
        emailColumn.put("javaType", "String");
        emailColumn.put("comment", "邮箱");
        columns.add(emailColumn);

        table.put("columns", columns);
        context.addCustomProperty("table", table);

        // 贫血/充血模板共用的顶级变量
        context.addCustomProperty("entityName", "User");
        context.addCustomProperty("lowerEntityName", "user");
        context.addCustomProperty("tableName", "t_user");
        context.addCustomProperty("comment", "用户表");
    }

    @Test
    void testRenderEntityTemplate() throws IOException {
        // 设置模板开关
        context.getCustomProperties().put("useLombok", true);
        context.getCustomProperties().put("useSwagger", true);
        context.getCustomProperties().put("useBasePO", true);
        context.getCustomProperties().put("useMyBatisPlus", true);

        // 渲染模板
        String result = renderer.renderAnemicTemplate("Entity.ftl", context);

        // 验证结果
        assertThat(result).contains("package com.test.app.entity;");
        assertThat(result).contains("public class User extends BasePO");
        assertThat(result).contains("@TableName(\"t_user\")");
        assertThat(result).contains("private String userName;");
        assertThat(result).contains("private String email;");
        assertThat(result).contains("@Schema");

        System.out.println("=== Entity.java ===");
        System.out.println(result);
    }

    @Test
    void testRenderServiceTemplate() throws IOException {
        String result = renderer.renderAnemicTemplate("Service.ftl", context);

        assertThat(result).contains("package com.test.app.service;");
        assertThat(result).contains("public interface UserService extends IService<User, Long>");

        System.out.println("=== UserService.java ===");
        System.out.println(result);
    }

    @Test
    void testRenderControllerTemplate() throws IOException {
        String result = renderer.renderAnemicTemplate("Controller.ftl", context);

        assertThat(result).contains("package com.test.app.controller;");
        assertThat(result).contains("public class UserController extends BaseReadOnlyController<UserService, User, Long>");
        assertThat(result).contains("@RequestMapping(\"/api/user\")");
        // GEN-002 Phase C 权限扩展点（仅注释，不含 security 依赖）
        assertThat(result).contains("// @@PreAuthorize(\"hasRole('ADMIN')\")");
        assertThat(result).contains("GEN-002-EXTPT");

        System.out.println("=== UserController.java ===");
        System.out.println(result);
    }

    /**
     * GEN-002 Phase B：writeMode=DTO 时 Controller 模板生成写端点（POST/PUT/DELETE），
     * 写请求体为 DTO（非 PO），带 @Valid 校验。
     */
    @Test
    void testRenderControllerTemplateDtoWrite() throws IOException {
        context.getCustomProperties().put("writeMode", "DTO");
        context.getCustomProperties().put("dtoPackage", "com.test.app.dto");

        String result = renderer.renderAnemicTemplate("Controller.ftl", context);

        // 写端点存在
        assertThat(result).contains("@PostMapping");
        assertThat(result).contains("@PutMapping(\"/{id}\")");
        assertThat(result).contains("@DeleteMapping(\"/{id}\")");
        // 写请求体为 DTO（非 PO），带校验
        assertThat(result).contains("@Valid @RequestBody CreateUserRequest");
        assertThat(result).contains("@Valid @RequestBody UpdateUserRequest");
        assertThat(result).contains("BeanUtils.copyProperties(request, entity);");
        // 调用 service 写方法
        assertThat(result).contains("service.save(entity);");
        assertThat(result).contains("service.updateById(entity);");
        assertThat(result).contains("service.removeById(parseId(id));");
        // GEN-002 Phase C：权限扩展点——写方法独立鉴权 + 数据权限提示（仅注释）
        assertThat(result).contains("// @@PreAuthorize(\"hasAuthority('user:create')\")");
        assertThat(result).contains("// @@PreAuthorize(\"hasAuthority('user:update')\")");
        assertThat(result).contains("// @@PreAuthorize(\"hasAuthority('user:delete')\")");
        assertThat(result).contains("// @@DataPermission(dimension = \"t_user\", keyField = \"id\")");
        assertThat(result).contains("GEN-002-EXTPT");

        System.out.println("=== UserController.java (DTO WRITE) ===");
        System.out.println(result);
    }

    @Test
    void testRenderPomTemplate() throws IOException {
        String result = renderer.renderProjectTemplate("pom.xml.ftl", context);

        assertThat(result).contains("<groupId>com.test</groupId>");
        assertThat(result).contains("<artifactId>test-app</artifactId>");
        assertThat(result).contains("<version>1.0.0-SNAPSHOT</version>");
        assertThat(result).contains("<artifactId>fast-ddd-web</artifactId>");
        assertThat(result).contains("<artifactId>fast-ddd-crud</artifactId>");
        assertThat(result).contains("<maven.compiler.release>21</maven.compiler.release>");
        assertThat(result).contains("<springdoc.version>2.8.17</springdoc.version>");
        assertThat(result).contains("<knife4j.version>4.5.0</knife4j.version>");
        assertThat(result).contains("knife4j-openapi3-jakarta-spring-boot-starter");
        assertThat(result).doesNotContain("maven.compiler.source");
        assertThat(result).doesNotContain("knife4j-openapi3-spring-boot-starter");

        System.out.println("=== pom.xml ===");
        System.out.println(result);
    }

    @Test
    void testRenderApplicationYmlTemplate() throws IOException {
        String result = renderer.renderProjectTemplate("application.yml.ftl", context);

        assertThat(result).contains("name: test-app");
        assertThat(result).contains("driver-class-name: com.mysql.cj.jdbc.Driver");
        assertThat(result).contains("url: jdbc:mysql://localhost:3306/test-app");

        System.out.println("=== application.yml ===");
        System.out.println(result);
    }

    @Test
 void testRenderApplicationTemplate() throws IOException {
        String result = renderer.renderProjectTemplate("Application.java.ftl", context);

        assertThat(result).contains("package com.test.app;");
        assertThat(result).contains("public class TestApp");
        assertThat(result).contains("SpringApplication.run(TestApp.class, args);");

        System.out.println("=== TestApp.java ===");
        System.out.println(result);
    }

    @Test
    void testRenderReadmeTemplate() throws IOException {
        String result = renderer.renderProjectTemplate("README.md.ftl", context);

        assertThat(result).contains("# test-app");
        assertThat(result).contains("- **GroupId**: com.test");
        assertThat(result).contains("- **ArtifactId**: test-app");
        assertThat(result).contains("- **Model Type**: ANEMIC");
        assertThat(result).contains("- **Database**: MySQL");

        System.out.println("=== README.md ===");
        System.out.println(result);
    }

    @Test
    void testRenderRichAggregateRootTemplate() throws IOException {
        // 切换到充血模型
        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();

        context.getCustomProperties().put("useLombok", true);
        context.getCustomProperties().put("useSwagger", true);
        context.getCustomProperties().put("useAggregateRoot", true);

        // entityName, lowerEntityName 已在 setUp 中设置

        String result = renderer.renderRichTemplate("AggregateRoot.ftl", context);

        assertThat(result).contains("package com.test.app.domain;");
        assertThat(result).contains("public class User extends AggregateRoot<Long>");
        assertThat(result).contains("public static User create(");

        System.out.println("=== User.java (RICH) ===");
        System.out.println(result);
    }

    /**
     * 语义扩列窗口（P2-2 T8.55）：新模板×旧框架属性缺失态。
     *
     * <p>setUp 的 table 为 LinkedHashMap（无 aggregateName/dddRole/relationsSummary 键），
     * 模拟 Framework&lt;1.0.2（无对应 getter）的 bean 属性缺失——真值守卫形须降级为
     * 不注入且渲染不炸（<#if x??> 对 missing 求值 false 不抛）。</p>
     */
    @Test
    void testSemanticGuardsDegradeOnMissingProperties() throws IOException {
        context.getCustomProperties().put("useLombok", true);
        context.getCustomProperties().put("useSwagger", false);

        String anemic = renderer.renderAnemicTemplate("Entity.ftl", context);
        assertThat(anemic).contains("public class User extends BasePO");
        assertThat(anemic).doesNotContain("DDD 角色：");

        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();
        String rich = renderer.renderRichTemplate("AggregateRoot.ftl", context);
        assertThat(rich).contains("public class User extends AggregateRoot<Long>");
        assertThat(rich).doesNotContain("聚合：");
    }

    @Test
    void testTemplateContextToVariables() {
        Map<String, Object> variables = context.toTemplateVariables();

        // 验证基本变量
        assertThat(variables.get("groupId")).isEqualTo("com.test");
        assertThat(variables.get("artifactId")).isEqualTo("test-app");
        assertThat(variables.get("packageName")).isEqualTo("com.test.app");
        assertThat(variables.get("modelType")).isEqualTo("ANEMIC");
        assertThat(variables.get("databaseType")).isEqualTo("MYSQL");

        // 验证包路径
        assertThat(variables.get("entityPackage")).isEqualTo("com.test.app.entity");
        assertThat(variables.get("servicePackage")).isEqualTo("com.test.app.service");
        assertThat(variables.get("controllerPackage")).isEqualTo("com.test.app.controller");
    }

    @Test
    void testTemplateContextWithRichModel() {
        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();

        Map<String, Object> variables = context.toTemplateVariables();

        // 验证充血模型包路径
        assertThat(variables.get("domainPackage")).isEqualTo("com.test.app.domain");
        assertThat(variables.get("applicationPackage")).isEqualTo("com.test.app.application");
        assertThat(variables.get("infrastructurePackage")).isEqualTo("com.test.app.infrastructure.persistence");
        assertThat(variables.get("interfacesPackage")).isEqualTo("com.test.app.interfaces");
    }
}
