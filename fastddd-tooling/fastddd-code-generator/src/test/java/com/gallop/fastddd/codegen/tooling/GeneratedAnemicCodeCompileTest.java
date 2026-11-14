package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.CodeGeneratorAutoConfiguration;
import com.gallop.fastddd.common.model.ColumnMetadata;
import com.gallop.fastddd.common.model.TableMetadata;
import com.gallop.fastddd.tooling.api.GenerationRequest;
import com.gallop.fastddd.tooling.api.IdStrategy;
import com.gallop.fastddd.tooling.api.ModelType;
import com.gallop.fastddd.tooling.api.ProjectGenerator;
import com.gallop.fastddd.tooling.api.WriteMode;
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
 * ID-007 步骤 8（ANEMIC 侧）：生成代码真实编译门禁。
 *
 * <p>历史回归：ID-003/004 将框架主键具体类型化（{@code BasePO<ID>}、{@code IService<T,ID>}、
 * {@code BaseServiceImpl<M,T,ID>}、{@code BaseReadOnlyController<S,T,ID>}）后，生成模板仍按旧签名输出
 * （{@code extends BasePO} 原始类型、{@code IService<X>} 单参数），导致<b>生成产物编译失败</b>。
 * 既有测试（{@link ProjectGeneratorIntegrationTest}）仅断言生成文本包含某子串，从不编译产物，
 * 故 reactor 长期全绿而生成代码实为坏码——本测试堵住该缺口。</p>
 *
 * <p>本测试经真实自动配置装配 {@link ProjectGenerator}，按 Studio 同款方式生成 ANEMIC 全套
 * （entity/mapper/service/serviceImpl/controller），写入临时目录后用 JDK {@code JavaCompiler}
 * 针对 reactor 测试类路径（含 fast-ddd-crud/web/lombok/mybatis-plus）真实编译，断言 0 错误。
 * 覆盖两种主键策略（{@link IdStrategy#AUTO}/{@link IdStrategy#ASSIGN_ID}）以贴近步骤 8
 * "四种模型组合执行真实生成和编译"的 ANEMIC 半边。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class GeneratedAnemicCodeCompileTest {

    private static final String BASE_PKG = "com.example.demo";
    private static final String SRC_PREFIX = "backend/src/main/java/";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CodeGeneratorAutoConfiguration.class));

    @TempDir
    Path tempDir;

    /** ANEMIC + AUTO：数据库自增主键，生成产物须编译通过。 */
    @Test
    void anemicAutoGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            Map<String, String> files = generator.generate(request(IdStrategy.AUTO)).getFiles();
            assertGeneratedCompiles(files);
        });
    }

    /** ANEMIC + ASSIGN_ID：雪花主键（框架默认），生成产物须编译通过。 */
    @Test
    void anemicAssignIdGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            Map<String, String> files = generator.generate(request(IdStrategy.ASSIGN_ID)).getFiles();
            assertGeneratedCompiles(files);
        });
    }

    /**
     * GEN-002 Phase B：ANEMIC + writeMode=DTO 写路径。
     *
     * <p>验证 opt-in 写端点产物真实编译：Controller 含 POST/PUT/DELETE 写方法（引用 Create/Update
     * Request DTO、{@code @Valid} 校验、{@code BeanUtils} 映射、{@code service.save/updateById/removeById}），
     * CreateRequest/UpdateRequest DTO（{@code @Data}、{@code @NotNull}）须一并编译通过。覆盖验收 ④⑤。</p>
     */
    @Test
    void anemicDtoWriteGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            GenerationRequest req = GenerationRequest.builder(userTable(), BASE_PKG)
                    .moduleName("demo")
                    .modelType(ModelType.ANEMIC)
                    .useLombok(true)
                    .useSwagger(false)
                    .idStrategy(IdStrategy.AUTO)
                    .writeMode(WriteMode.DTO)
                    .author("CompileGate")
                    .build();
            Map<String, String> files = generator.generate(req).getFiles();
            assertGeneratedCompiles(files);
        });
    }

    /**
     * D-2a/b 门禁（2026-09-13 修复批）：ANEMIC + DECIMAL/DATETIME 列 + Swagger + DTO 写路径。
     *
     * <p>历史回归：模板对 DECIMAL→{@code BigDecimal}、DATETIME→{@code LocalDateTime} 字段均无
     * import 分支（全模板 0 处 {@code java.math.BigDecimal}），Studio 通道恒 {@code useSwagger(true)}。
     * 本例用同时含 DECIMAL/DATETIME 列的表生成 Entity/DTO/CreateRequest/UpdateRequest 全套并真实编译，
     * 堵住"结构断言全绿、生成代码缺 import 编译即坏"的盲区（该缺陷由真实导出 clean verify 首次暴露）。</p>
     */
    @Test
    void anemicDecimalSwaggerDtoGeneratesCompilingCode() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            GenerationRequest req = GenerationRequest.builder(decimalTable(), BASE_PKG)
                    .moduleName("demo")
                    .modelType(ModelType.ANEMIC)
                    .useLombok(true)
                    .useSwagger(true) // Studio 通道恒真：@Schema 注解轴一并纳入编译
                    .idStrategy(IdStrategy.AUTO)
                    .writeMode(WriteMode.DTO)
                    .author("CompileGate")
                    .build();
            Map<String, String> files = generator.generate(req).getFiles();
            assertThat(files.values().stream()
                    .filter(v -> v.contains("private BigDecimal amount"))
                    .count()).as("DECIMAL 列须映射为 BigDecimal 字段").isGreaterThan(0);
            assertGeneratedCompiles(files);
        });
    }

    /**
     * 语义扩列消费（Q3 样例②）：Entity javadoc 注入 DDD 角色。
     *
     * <p>Framework 1.0.2 起 TableMetadata 新增 dddRole 可空列，模板以真值守卫形消费；
     * 有值臂以 fixture 值为锚验证注入链，空值臂验证守卫降级（null 值不注入不炸）。</p>
     */
    @Test
    void anemicDddRoleAnnotatesEntityJavadoc() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            TableMetadata table = userTable();
            table.setDddRole("AGGREGATE_ROOT");
            GenerationRequest req = GenerationRequest.builder(table, BASE_PKG)
                    .moduleName("demo")
                    .modelType(ModelType.ANEMIC)
                    .useLombok(true)
                    .useSwagger(false)
                    .idStrategy(IdStrategy.AUTO)
                    .author("SemanticGate")
                    .build();
            Map<String, String> files = generator.generate(req).getFiles();
            assertThat(files.values().stream()
                    .anyMatch(v -> v.contains("AGGREGATE_ROOT")))
                    .as("dddRole 须注入 Entity javadoc 语义标注").isTrue();
            assertGeneratedCompiles(files);
        });
    }

    @Test
    void anemicDddRoleAbsentStillGeneratesAndCompiles() throws IOException {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            // 既有 userTable 不设 dddRole（null 值态）——守卫形须降级为不注入且不炸
            Map<String, String> files = generator.generate(request(IdStrategy.AUTO)).getFiles();
            assertGeneratedCompiles(files);
        });
    }

    private static void assertGeneratedCompiles(Map<String, String> files) throws IOException {
        Path srcRoot = Files.createTempDirectory("fastddd-gen-src");
        List<Path> written = new ArrayList<>();
        for (Map.Entry<String, String> e : files.entrySet()) {
            String key = e.getKey();
            if (!key.startsWith(SRC_PREFIX) || !key.endsWith(".java")) {
                continue;
            }
            // 跳过非 ANEMIC 产物（如未来 RICH 的 PO），本测试只编译 ANEMIC 5 件套
            if (key.contains("/domain/") || key.contains("/infrastructure/")
                    || key.contains("/application/") || key.contains("/interfaces/")) {
                continue;
            }
            Path out = srcRoot.resolve(key.substring(SRC_PREFIX.length()));
            Files.createDirectories(out.getParent());
            Files.writeString(out, e.getValue());
            written.add(out);
        }
        assertThat(written).as("应至少生成 entity/mapper/service/serviceImpl/controller 5 件套").hasSizeGreaterThanOrEqualTo(5);

        boolean ok = compile(srcRoot, written);
        assertThat(ok).as("生成的 ANEMIC 代码须编译通过（见上方诊断输出）").isTrue();
    }

    /**
     * 用 JDK JavaCompiler 真实编译。lombok 注解处理器默认从 classpath 发现，
     * 故 {@code @Getter/@Setter/@Builder} 等会被处理。返回是否 0 错误。
     */
    private static boolean compile(Path srcRoot, List<Path> sources) throws IOException {
        var compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, null);

        Iterable<? extends JavaFileObject> units = fm.getJavaFileObjects(sources.toArray(Path[]::new));
        Path classesOut = Files.createTempDirectory("fastddd-gen-classes");
        String classpath = System.getProperty("java.class.path");
        List<String> options = Arrays.asList(
                "-classpath", classpath,
                "-d", classesOut.toString(),
                "-sourcepath", srcRoot.toString(),
                "--release", "21");

        boolean ok = compiler.getTask(null, fm, diagnostics, options, null, units).call();
        List<String> errors = diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(d -> d.getKind() + ":" + d.getSource() + ":" + d.getLineNumber() + " " + d.getMessage(null))
                .collect(Collectors.toList());
        // 失败时把错误打到 stdout，便于排查（javadoc/本测试自审均可见）
        if (!errors.isEmpty()) {
            errors.forEach(System.out::println);
        }
        fm.close();
        return ok && errors.isEmpty();
    }

    private static GenerationRequest request(IdStrategy idStrategy) {
        return GenerationRequest.builder(userTable(), BASE_PKG)
                .moduleName("demo")
                .modelType(ModelType.ANEMIC)
                .useLombok(true)
                .useSwagger(false) // 专注 ID/结构编译正确性；Swagger 注解为正交轴
                .idStrategy(idStrategy)
                .author("CompileGate")
                .build();
    }

    private static TableMetadata userTable() {
        TableMetadata table = new TableMetadata();
        table.setTableName("t_user");
        table.setTableComment("用户表");
        table.setEntityName("User");

        List<ColumnMetadata> cols = new ArrayList<>();
        ColumnMetadata id = new ColumnMetadata();
        id.setColumnName("id");
        id.setFieldName("id");
        id.setJavaType("Long");
        id.setPrimaryKey(true);
        id.setColumnComment("主键");
        cols.add(id);

        ColumnMetadata username = new ColumnMetadata();
        username.setColumnName("username");
        username.setFieldName("username");
        username.setJavaType("String");
        username.setColumnComment("用户名");
        cols.add(username);

        table.setColumns(cols);
        return table;
    }

    /** 含 DECIMAL(BigDecimal)/DATETIME(LocalDateTime) 列的表——D-2a import 门禁用例。 */
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
