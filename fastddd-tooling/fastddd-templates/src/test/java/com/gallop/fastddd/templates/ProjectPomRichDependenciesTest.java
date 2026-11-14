package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * D-2 修复批（2026-09-13）模板契约门禁：pom 依赖门控与聚合根注解/导入。
 *
 * <p>历史缺陷（由真实导出 {@code mvn clean verify} 首次暴露，结构断言测试全盲）：</p>
 * <ul>
 *   <li><b>c（依赖门控错位）</b>：{@code pom.xml.ftl} 仅按项目级 {@code modelType=="RICH"} 写入
 *       fast-ddd-application 与 MapStruct（含注解处理器），而逐表代码生成通道可经分析器/AI 结论/
 *       手动覆盖把任意表解析为 RICH——ANEMIC 项目产出 RICH 产物即缺依赖缺处理器。修复：门控改为
 *       {@code (richDependencies!false) || modelType == "RICH"}，由生成通道按"是否存在 RICH 产物"
 *       注入 {@code richDependencies}；插件 {@code <configuration>} 仍只认项目级 modelType
 *       （再运行语义不随逐表解析漂移）。</li>
 *   <li><b>b（swagger v2/v3 不一致）</b>：聚合根用 v2 {@code @ApiModel}/{@code @ApiModelProperty}
 *       （无依赖无 import），其余模板均 v3 {@code @Schema}。修复：统一 v3。</li>
 *   <li><b>a（BigDecimal/时间导入缺失）</b>：模板无 {@code java.math.BigDecimal} import 分支。</li>
 * </ul>
 *
 * <p>本类锁定模板层契约；产物级编译证明见 code-generator 的
 * {@code GeneratedRichCodeCompileTest}/{@code GeneratedAnemicCodeCompileTest}（真实 javac），
 * 生成项目级 Maven 证明见 Studio 导出门禁（真实 {@code mvn clean verify}）。</p>
 *
 * @author gallop
 * @since 1.0.2
 */
class ProjectPomRichDependenciesTest {

    private TemplateRenderer renderer;
    private TemplateContext context;

    @BeforeEach
    void setUp() throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(TemplateContext.class, "/templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        TemplateMessages.apply(configuration, TemplateMessages.DEFAULT_LANGUAGE);

        renderer = new TemplateRenderer(configuration);

        context = TemplateContext.builder()
                .groupId("com.test")
                .artifactId("test-app")
                .version("1.0.0-SNAPSHOT")
                .packageName("com.test.app")
                .projectName("Test Application")
                .modelType(TemplateContext.ModelType.ANEMIC)
                .databaseType(TemplateContext.DatabaseType.MYSQL)
                .modules(new HashSet<>())
                .enableCQRS(false)
                .fastdddVersion("1.0.0")
                .fastdddToolingVersion("1.0.2")
                .javaVersion("21")
                .springBootVersion("3.5.16")
                .author("Test Author")
                .date("2026-09-13")
                .build()
                .calculatePackages();

        // 模拟表元数据：含 DECIMAL(BigDecimal)/DATETIME(LocalDateTime) 列
        Map<String, Object> table = new LinkedHashMap<>();
        table.put("name", "t_order_info");
        table.put("entityName", "OrderInfo");
        table.put("comment", "订单信息表");

        List<Map<String, Object>> columns = new ArrayList<>();
        columns.add(column("id", "id", "Long", "主键"));
        columns.add(column("order_no", "orderNo", "String", "订单号"));
        columns.add(column("amount", "amount", "BigDecimal", "金额"));
        columns.add(column("order_time", "orderTime", "LocalDateTime", "下单时间"));
        table.put("columns", columns);

        context.addCustomProperty("table", table);
        context.addCustomProperty("entityName", "OrderInfo");
        context.addCustomProperty("lowerEntityName", "orderInfo");
        context.addCustomProperty("tableName", "t_order_info");
        context.addCustomProperty("comment", "订单信息表");
    }

    private static Map<String, Object> column(String columnName, String fieldName, String javaType, String comment) {
        Map<String, Object> c = new LinkedHashMap<>();
        c.put("columnName", columnName);
        c.put("fieldName", fieldName);
        c.put("javaType", javaType);
        c.put("comment", comment);
        c.put("nullable", true);
        c.put("primaryKey", false);
        return c;
    }

    /** ANEMIC 且无 RICH 产物：不得添加 fast-ddd-application/MapStruct/注解处理器（不加无用依赖）。 */
    @Test
    void anemicWithoutRichDependenciesOmitsRichDeps() throws IOException {
        String pom = renderer.renderProjectTemplate("pom.xml.ftl", context);
        assertThat(pom).doesNotContain("<artifactId>fast-ddd-application</artifactId>");
        assertThat(pom).doesNotContain("<artifactId>mapstruct</artifactId>");
        assertThat(pom).doesNotContain("mapstruct-processor");
        assertThat(pom).doesNotContain("lombok-mapstruct-binding");
        assertThat(pom).doesNotContain("<mapstruct.version>");
        // 插件 configuration 亦不得出现 RICH 语义
        assertThat(pom).doesNotContain("<modelType>RICH</modelType>");
    }

    /**
     * ANEMIC 项目级 + 逐表解析出 RICH 产物（richDependencies=true，D-2c 主场景）：
     * fast-ddd-application + MapStruct + 注解处理器链必须齐备；但插件 {@code <configuration>}
     * 仍按项目级 ANEMIC 不写 RICH（再运行语义不漂移）。
     */
    @Test
    void anemicWithRichDependenciesFlagCarriesRichDeps() throws IOException {
        context.addCustomProperty("richDependencies", true);
        String pom = renderer.renderProjectTemplate("pom.xml.ftl", context);
        assertThat(pom).contains("<artifactId>fast-ddd-application</artifactId>");
        assertThat(pom).contains("<artifactId>mapstruct</artifactId>");
        assertThat(pom).contains("<mapstruct.version>1.5.5.Final</mapstruct.version>");
        assertThat(pom).contains("<lombok-mapstruct-binding.version>0.2.0</lombok-mapstruct-binding.version>");
        assertThat(pom).contains("mapstruct-processor");
        assertThat(pom).contains("lombok-mapstruct-binding");
        // 插件 configuration 仍按项目级 modelType（ANEMIC 不写）
        assertThat(pom).doesNotContain("<modelType>RICH</modelType>");
    }

    /** 项目级 RICH（不传 richDependencies，旧调用方/InitProjectMojo）：门控行为向后兼容不变。 */
    @Test
    void richModelTypeKeepsLegacyGating() throws IOException {
        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();
        String pom = renderer.renderProjectTemplate("pom.xml.ftl", context);
        assertThat(pom).contains("<artifactId>fast-ddd-application</artifactId>");
        assertThat(pom).contains("mapstruct-processor");
        assertThat(pom).contains("<modelType>RICH</modelType>");
        assertThat(pom).contains("<enableCQRS>true</enableCQRS>");
    }

    /** D-2a/b：聚合根对 DECIMAL/DATETIME 列补 import，且 swagger 注解统一 v3。 */
    @Test
    void aggregateRootImportsDecimalAndTimeWithV3Schema() throws IOException {
        // domainPackage 等分层包仅在 RICH 下由 calculatePackages 计算
        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();
        context.getCustomProperties().put("useLombok", true);
        context.getCustomProperties().put("useSwagger", true);
        context.getCustomProperties().put("useAggregateRoot", true);

        String out = renderer.renderRichTemplate("AggregateRoot.ftl", context);

        assertThat(out).contains("import java.math.BigDecimal;");
        assertThat(out).contains("import java.time.*;");
        assertThat(out).contains("private BigDecimal amount;");
        // v2 注解清零，统一 v3
        assertThat(out).doesNotContain("@ApiModel");
        assertThat(out).doesNotContain("@ApiModelProperty");
        assertThat(out).contains("@Schema(description =");
    }

    /**
     * D-3 批（2026-09-13 运行期闭环）可执行打包契约：生成 Dockerfile 以 {@code java -jar target/*.jar}
     * 运行、README 承诺 {@code mvn spring-boot:run}——pom 必须声明 spring-boot-maven-plugin
     * {@code repackage}（此前缺该插件，package 产物为无 Main-Class 的平装 jar，"jar 中没有主清单属性"，
     * 两处文档承诺均落空）。版本不声明：继承 parent（spring-boot-starter-parent）pluginManagement，
     * 不引入独立第三方版本。贫血/RICH 两通道一致（打包契约与模型类型无关）。
     */
    @Test
    void executablePackagingContractBothChannels() throws IOException {
        // ANEMIC（setUp 默认上下文）
        assertBootRepackageDeclared(renderer.renderProjectTemplate("pom.xml.ftl", context));

        // RICH
        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();
        assertBootRepackageDeclared(renderer.renderProjectTemplate("pom.xml.ftl", context));
    }

    private static void assertBootRepackageDeclared(String pom) {
        assertThat(pom).contains("<artifactId>spring-boot-maven-plugin</artifactId>");
        assertThat(pom).contains("<goal>repackage</goal>");
        int start = pom.indexOf("<artifactId>spring-boot-maven-plugin</artifactId>");
        int end = pom.indexOf("</plugin>", start);
        assertThat(end).as("spring-boot-maven-plugin 声明必须有完整 </plugin> 闭合").isGreaterThan(start);
        // 版本不声明：继承 Boot parent pluginManagement（约束：不升级/不引入独立第三方版本）
        assertThat(pom.substring(start, end)).doesNotContain("<version>");
    }

    /** D-2a：PO/Create*Command/贫血三件套对 BigDecimal 列补 import（抽测代表文件）。 */
    @Test
    void decimalColumnsImportBigDecimalAcrossTemplates() throws IOException {
        context.getCustomProperties().put("useLombok", true);
        context.getCustomProperties().put("useSwagger", true);
        context.getCustomProperties().put("useBasePO", true);
        context.getCustomProperties().put("useMyBatisPlus", true);
        context.getCustomProperties().put("useValidation", true);

        // rich 半边：domainPackage 等分层包仅在 RICH 下计算
        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();
        assertThat(renderer.renderRichTemplate("po.ftl", context)).contains("import java.math.BigDecimal;");
        assertThat(renderer.renderRichTemplate("Command.ftl", context)).contains("import java.math.BigDecimal;");

        // anemic 半边：切回 ANEMIC 并重算；dtoPackage 由生成通道以自定义属性注入
        // （同 CodeGeneratorService，calculatePackages 不计算该值）
        context.setModelType(TemplateContext.ModelType.ANEMIC);
        context.calculatePackages();
        context.getCustomProperties().put("dtoPackage", "com.test.app.dto");
        assertThat(renderer.renderAnemicTemplate("Entity.ftl", context)).contains("import java.math.BigDecimal;");
        assertThat(renderer.renderAnemicTemplate("DTO.ftl", context)).contains("import java.math.BigDecimal;");
        assertThat(renderer.renderAnemicTemplate("CreateRequest.ftl", context)).contains("import java.math.BigDecimal;");
        assertThat(renderer.renderAnemicTemplate("UpdateRequest.ftl", context)).contains("import java.math.BigDecimal;");
    }
}
