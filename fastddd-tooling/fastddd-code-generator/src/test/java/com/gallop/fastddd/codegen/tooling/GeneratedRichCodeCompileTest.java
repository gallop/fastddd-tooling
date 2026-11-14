package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.CodeGeneratorAutoConfiguration;
import com.gallop.fastddd.common.model.ColumnMetadata;
import com.gallop.fastddd.common.model.TableMetadata;
import com.gallop.fastddd.tooling.api.GenerationRequest;
import com.gallop.fastddd.tooling.api.IdStrategy;
import com.gallop.fastddd.tooling.api.ModelType;
import com.gallop.fastddd.tooling.api.ProjectGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ID-007 步骤 8（RICH 侧）：生成代码真实编译门禁。
 *
 * <p>充血模型经 codegen 引擎（{@code CodeGeneratorService.generateRichModelCode}）的生成路径
 * 历史上一编译即坏：PO 原始类型 {@code extends BasePO}、聚合根引用的 {@code *CreatedEvent} 与
 * RepositoryImpl 注入的 {@code *BaseMapper} 从未生成、{@code interfacesPackage} 未定义、
 * CommandController/queryController/CommandHandler/queryHandler 的框架导入包名指向不存在的
 * {@code com.gallop.fastddd.application.controller/handler.*}（实为 {@code .web.controller.*} 与
 * {@code .application.command/query.*}）。本测试用真实编译堵住整条路径的回归。</p>
 *
 * <p>经真实自动配置装配 {@link ProjectGenerator}，按 Studio 同款方式（RICH 强制 enableCQRS）
 * 生成全套充血产物（PO/聚合根/创建事件/EntityMapper/基础Mapper/Repository/RepositoryImpl/
 * 应用服务/Command/CommandHandler/Query/QueryHandler/CommandController/QueryController），
 * 写入临时目录后用 JDK {@code JavaCompiler} 针对 reactor 测试类路径真实编译，断言 0 错误。
 * 覆盖 {@link IdStrategy#AUTO}/{@link IdStrategy#ASSIGN_ID} 两种主键策略（步骤 8 RICH 半边）。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class GeneratedRichCodeCompileTest {

    private static final String BASE_PKG = "com.example.demo";
    private static final String SRC_PREFIX = "backend/src/main/java/";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CodeGeneratorAutoConfiguration.class));

    @TempDir
    Path tempDir;

    @Test
    void richAutoGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            Map<String, String> files = generator.generate(request(IdStrategy.AUTO)).getFiles();
            assertGeneratedCompiles(files);
        });
    }

    @Test
    void richAssignIdGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            Map<String, String> files = generator.generate(request(IdStrategy.ASSIGN_ID)).getFiles();
            assertGeneratedCompiles(files);
        });
    }

    /**
     * D-2a/b 门禁（2026-09-13 修复批）：RICH + DECIMAL/DATETIME 列 + Swagger。
     *
     * <p>历史回归：① DECIMAL→{@code BigDecimal}、DATETIME→{@code LocalDateTime} 字段在
     * AggregateRoot/PO/Create*Command 中无 import 分支；② 聚合根使用 swagger v2
     * {@code @ApiModel}/{@code @ApiModelProperty}（无依赖亦无 import，springfox→springdoc 半途迁移），
     * 而 {@code useSwagger=false} 的既有门禁对此全盲。本例以 Studio 同款 {@code useSwagger(true)}
     * + 含 DECIMAL/DATETIME 列的表生成 RICH 全套并真实编译，两项一并锁死。</p>
     */
    @Test
    void richDecimalSwaggerGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            GenerationRequest req = GenerationRequest.builder(decimalTable(), BASE_PKG)
                    .moduleName("demo")
                    .modelType(ModelType.RICH)
                    .enableCQRS(true)
                    .useLombok(true)
                    .useSwagger(true) // Studio 通道恒真：v3 @Schema 轴纳入编译
                    .idStrategy(IdStrategy.AUTO)
                    .author("CompileGate")
                    .build();
            Map<String, String> files = generator.generate(req).getFiles();
            assertThat(files.values().stream()
                    .anyMatch(v -> v.contains("private BigDecimal amount;")))
                    .as("DECIMAL 列须映射为 BigDecimal 字段").isTrue();
            assertThat(files.values().stream()
                    .noneMatch(v -> v.contains("@ApiModel") || v.contains("@ApiModelProperty")))
                    .as("RICH 产物不得再出现 swagger v2 注解（已统一 v3 @Schema）").isTrue();
            assertGeneratedCompiles(files);
        });
    }

    /**
     * D-2c 门禁（2026-09-13 修复批）：RICH + enableCQRS=false。
     *
     * <p>历史回归：CommandController/QueryController 无条件生成，但它们引用的
     * Create*Command/Get*ByIdQuery 及其 Handler 仅在 enableCQRS=true 时生成——关闭 CQRS 即产出
     * 引用缺失类的不可编译控制器。修复后控制器随 enableCQRS 同门控：本例断言产物不含控制器
     * 文件且 8 件基础套（PO/聚合根/CreatedEvent/EntityMapper/BaseMapper/Repository/RepositoryImpl/
     * ApplicationService）真实编译通过。</p>
     */
    @Test
    void richWithoutCqrsGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            GenerationRequest req = GenerationRequest.builder(userTable(), BASE_PKG)
                    .moduleName("demo")
                    .modelType(ModelType.RICH)
                    .enableCQRS(false)
                    .useLombok(true)
                    .useSwagger(false)
                    .idStrategy(IdStrategy.AUTO)
                    .author("CompileGate")
                    .build();
            Map<String, String> files = generator.generate(req).getFiles();
            assertThat(files.keySet().stream().noneMatch(k -> k.endsWith("CommandController.java")
                    || k.endsWith("QueryController.java")))
                    .as("enableCQRS=false 时不得生成引用 CQRS 产物的控制器").isTrue();
            assertThat(files.keySet().stream().noneMatch(k -> k.contains("Command.java")
                    || k.contains("Query.java") || k.contains("Handler.java")))
                    .as("enableCQRS=false 时不得生成 CQRS 四件套").isTrue();
            assertGeneratedCompiles(files);
        });
    }

    /**
     * 语义扩列消费（Q3 样例①）：聚合根 javadoc 注入聚合名+关系摘要。
     *
     * <p>Framework 1.0.2 起 TableMetadata 新增 aggregateName/relationsSummary 可空列，模板以
     * {@code <#if x?? && x?has_content>} 真值守卫形消费（FreeMarker 无裸字符串真值——&& 右操作数须布尔）——有值臂以 fixture 值为锚（语言无关——取「销售订单聚合」避开正常输出「订单聚合根」子串撞车）验证注入链；
     * 空值臂验证守卫降级（null 值不注入不炸，摘守卫裸插值将抛 InvalidReferenceException）。</p>
     */
    @Test
    void richSemanticColumnsAnnotateAggregateRootJavadoc() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            TableMetadata table = userTable();
            table.setAggregateName("销售订单聚合");
            table.setRelationsSummary("ONE_TO_MANY:t_order_item");
            GenerationRequest req = GenerationRequest.builder(table, BASE_PKG)
                    .moduleName("demo")
                    .modelType(ModelType.RICH)
                    .enableCQRS(true)
                    .useLombok(true)
                    .useSwagger(false)
                    .idStrategy(IdStrategy.AUTO)
                    .author("SemanticGate")
                    .build();
            Map<String, String> files = generator.generate(req).getFiles();
            assertThat(files.values().stream()
                    .anyMatch(v -> v.contains("销售订单聚合")))
                    .as("聚合名须注入聚合根 javadoc 语义标注").isTrue();
            assertThat(files.values().stream()
                    .anyMatch(v -> v.contains("ONE_TO_MANY:t_order_item")))
                    .as("关系摘要须注入聚合根 javadoc 语义标注").isTrue();
            assertGeneratedCompiles(files);
        });
    }

    @Test
    void richSemanticColumnsAbsentStillGeneratesAndCompiles() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            // 既有 userTable 不设三键（null 值态）——守卫形须降级为不注入且不炸
            Map<String, String> files = generator.generate(request(IdStrategy.AUTO)).getFiles();
            assertThat(files.values().stream()
                    .noneMatch(v -> v.contains("销售订单聚合")))
                    .as("未设聚合名时不得注入语义标注").isTrue();
            assertGeneratedCompiles(files);
        });
    }

    private static void assertGeneratedCompiles(Map<String, String> files) throws IOException {
        // 写入稳定路径 target/generated-rich-sources/ 以便失败时人工复核 / 手动 javac
        Path projectDir = Path.of("target").toAbsolutePath();
        Files.createDirectories(projectDir);
        Path srcRoot = projectDir.resolve("generated-rich-src");
        clearDir(srcRoot);
        List<Path> written = new ArrayList<>();
        for (Map.Entry<String, String> e : files.entrySet()) {
            String key = e.getKey();
            if (!key.startsWith(SRC_PREFIX) || !key.endsWith(".java")) {
                continue;
            }
            Path out = srcRoot.resolve(key.substring(SRC_PREFIX.length()));
            Files.createDirectories(out.getParent());
            Files.writeString(out, e.getValue());
            written.add(out);
        }
        // RICH 基础 8 件套：PO + 聚合根 + CreatedEvent + EntityMapper + BaseMapper + Repository
        //                  + RepositoryImpl + ApplicationService；enableCQRS=true 再加
        //                  Command/CommandHandler/Query/QueryHandler/CommandController/QueryController
        assertThat(written).as("RICH 生成文件数（基础 8 件，CQRS 开启时 ≥14）").hasSizeGreaterThanOrEqualTo(8);

        boolean ok = compile(srcRoot, written);
        assertThat(ok).as("生成的 RICH 代码须编译通过（见上方诊断输出）").isTrue();
    }

    private static boolean compile(Path srcRoot, List<Path> sources) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> units = fm.getJavaFileObjects(sources.toArray(Path[]::new));
        Path classesOut = Path.of("target").toAbsolutePath().resolve("generated-rich-classes");
        clearDir(classesOut);
        // 关键：显式 -processorpath 让 Lombok（与 MapStruct）注解处理器被 SPI 自动发现。
        // ToolProvider 系统编译器在 in-process 模式下不会自动扫描测试类路径上的处理器 SPI，
        // 缺此项则 @Data/@Slf4j/@RequiredArgsConstructor 不生成成员 → 跨文件引用 getter/log 全部失败。
        String processorPath = buildProcessorPath();
        List<String> options = new ArrayList<>(Arrays.asList(
                "-classpath", System.getProperty("java.class.path"),
                "-d", classesOut.toString(),
                "-sourcepath", srcRoot.toString(),
                "--release", "21"));
        if (!processorPath.isEmpty()) {
            options.add("-processorpath");
            options.add(processorPath);
        }

        boolean ok = compiler.getTask(null, fm, diagnostics, options, null, units).call();
        List<String> errors = diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(d -> d.getKind() + ":" + d.getSource() + ":" + d.getLineNumber() + " " + d.getMessage(null))
                .collect(Collectors.toList());
        if (!errors.isEmpty()) {
            // 写入稳定文件以便复核（System.out 被 surefire 吞掉）
            Path errFile = Path.of("target").toAbsolutePath().resolve("generated-rich-compile-errors.txt");
            Files.writeString(errFile, String.join("\n", errors));
            errors.forEach(System.err::println);
        }
        fm.close();
        return ok && errors.isEmpty();
    }

    private static void clearDir(Path dir) throws IOException {
        if (Files.exists(dir)) {
            try (var stream = Files.walk(dir)) {
                stream.sorted(java.util.Comparator.reverseOrder())
                        .forEach(p -> {
                            try { Files.deleteIfExists(p); } catch (IOException ignored) { }
                        });
            }
        }
        Files.createDirectories(dir);
    }

    /**
     * 从 {@code java.class.path} 中析出 Lombok 与 MapStruct 注解处理器 jar，拼成 -processorpath。
     * 两者都是 codegen 的（optional）依赖，必在测试类路径上。
     */
    private static String buildProcessorPath() {
        List<String> jars = new ArrayList<>();
        for (String entry : System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
            String name = entry.toLowerCase();
            if (name.contains("lombok") || name.contains("mapstruct-processor")) {
                jars.add(entry);
            }
        }
        return String.join(java.io.File.pathSeparator, jars);
    }

    private static GenerationRequest request(IdStrategy idStrategy) {
        return GenerationRequest.builder(userTable(), BASE_PKG)
                .moduleName("demo")
                .modelType(ModelType.RICH)
                .enableCQRS(true) // RICH 强制 CQRS；CommandController/QueryController 依赖 Command/Query 产物
                .useLombok(true)
                .useSwagger(false)
                .idStrategy(idStrategy)
                .author("CompileGate")
                .build();
    }

    private static TableMetadata userTable() {
        TableMetadata table = new TableMetadata();
        table.setTableName("t_order");
        table.setTableComment("订单");
        table.setEntityName("Order");

        List<ColumnMetadata> cols = new ArrayList<>();
        ColumnMetadata id = new ColumnMetadata();
        id.setColumnName("id");
        id.setFieldName("id");
        id.setJavaType("Long");
        id.setPrimaryKey(true);
        id.setColumnComment("主键");
        cols.add(id);

        ColumnMetadata orderNo = new ColumnMetadata();
        orderNo.setColumnName("order_no");
        orderNo.setFieldName("orderNo");
        orderNo.setJavaType("String");
        orderNo.setColumnComment("订单号");
        cols.add(orderNo);

        table.setColumns(cols);
        return table;
    }

    /** 含 DECIMAL(BigDecimal)/DATETIME(LocalDateTime) 列的表——D-2a/b RICH 侧 import 门禁用例。 */
    private static TableMetadata decimalTable() {
        TableMetadata table = new TableMetadata();
        table.setTableName("t_order_info");
        table.setTableComment("订单信息表");
        table.setEntityName("OrderInfo");

        List<ColumnMetadata> cols = new ArrayList<>();
        ColumnMetadata id = new ColumnMetadata();
        id.setColumnName("id");
        id.setFieldName("id");
        id.setJavaType("Long");
        id.setPrimaryKey(true);
        id.setColumnComment("主键");
        cols.add(id);

        ColumnMetadata orderNo = new ColumnMetadata();
        orderNo.setColumnName("order_no");
        orderNo.setFieldName("orderNo");
        orderNo.setJavaType("String");
        orderNo.setColumnComment("订单号");
        cols.add(orderNo);

        ColumnMetadata amount = new ColumnMetadata();
        amount.setColumnName("amount");
        amount.setFieldName("amount");
        amount.setJavaType("BigDecimal");
        amount.setColumnComment("金额");
        cols.add(amount);

        ColumnMetadata orderTime = new ColumnMetadata();
        orderTime.setColumnName("order_time");
        orderTime.setFieldName("orderTime");
        orderTime.setJavaType("LocalDateTime");
        orderTime.setColumnComment("下单时间");
        cols.add(orderTime);

        table.setColumns(cols);
        return table;
    }
}
