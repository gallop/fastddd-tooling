package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * D-3 修复批（2026-09-13 运行期闭环）模板契约门禁：{@code Application.java.ftl} 的
 * {@code @MapperScan} 扫描边界。
 *
 * <p><b>缺陷</b>（真实导出 {@code java -jar}/classpath 启动首次暴露，编译期门禁全盲）：
 * RICH 产物把 MyBatis Mapper（{@code org.apache.ibatis.annotations.@Mapper}，*BaseMapper 接口）、
 * MapStruct 转换器（{@code org.mapstruct.@Mapper(componentModel="spring")}）与领域仓储接口
 * （无注解，由 {@code @Repository} 实现类承载）混布在根包下——根包裸 {@code @MapperScan} 使
 * ClassPathMapperScanner 把后两者也注册成 MyBatis MapperFactoryBean 代理，与既有 Spring bean
 * 同型冲突（构造注入 "expected single matching bean but found 2"，应用无法启动）。</p>
 *
 * <p><b>修复边界</b>：RICH 分支 {@code annotationClass = org.apache.ibatis.annotations.Mapper.class}
 * 只注册真正的 MyBatis Mapper（FQN 过滤同时排除 org.mapstruct 转换器与无注解仓储接口）；
 * 贫血分支<b>不得</b>加该过滤——贫血 Mapper（BaseRepository 子接口）不带 {@code @Mapper}，
 * 依赖专属 {@code mapperPackage} 包扫描注册，加过滤将导致贫血 Mapper 全部失注册。
 * 混合形态（项目级 ANEMIC + 逐表 RICH，{@code richDependencies}）需双扫描并存
 * （D-4：RICH *BaseMapper 不在 mapperPackage 内，单包扫描漏注册）。</p>
 *
 * <p>运行级证明（启动/健康/CRUD）见 Studio 导出门禁 {@code ExportMavenVerifyGateTest}
 * （真实 {@code java -jar} 启动 + 接口回环），本类锁定模板层契约。</p>
 *
 * @author gallop
 * @since 1.0.2
 */
class ApplicationTemplateMapperScanBoundaryTest {

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
                .databaseType(TemplateContext.DatabaseType.H2)
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
    }

    /**
     * 贫血通道：扫描专属 mapperPackage（{@code <base>.mapper}），<b>不带</b> annotationClass 过滤。
     * 贫血 Mapper 是 BaseRepository 子接口、不带 @Mapper 注解——依赖包扫描注册，
     * 加 annotationClass 过滤会使贫血 Mapper 全部失注册（回归护栏）。
     */
    @Test
    void anemicScansDedicatedMapperPackageWithoutAnnotationFilter() throws IOException {
        String out = renderer.renderProjectTemplate("Application.java.ftl", context);

        assertThat(out).contains("@MapperScan(\"com.test.app.mapper\")");
        assertThat(out).doesNotContain("annotationClass");
        assertThat(out).doesNotContain("org.apache.ibatis.annotations.Mapper");
    }

    /**
     * RICH 通道：根包扫描必须带 annotationClass 过滤，且过滤目标必须是全限定名
     * {@code org.apache.ibatis.annotations.Mapper.class}（模板不生成 import，短名
     * {@code Mapper.class} 无法通过编译；FQN 同时是排除 org.mapstruct.Mapper 转换器
     * 与无注解领域仓储接口的唯一可判定边界）。
     */
    @Test
    void richRootScanFiltersByIbatisMapperAnnotation() throws IOException {
        context.setModelType(TemplateContext.ModelType.RICH);
        context.calculatePackages();

        String out = renderer.renderProjectTemplate("Application.java.ftl", context);

        assertThat(out).contains("@MapperScan(basePackages = \"com.test.app\", "
                + "annotationClass = org.apache.ibatis.annotations.Mapper.class)");
        // 根包裸扫描（无过滤）不得回归
        assertThat(out).doesNotContain("@MapperScan(\"com.test.app\")");
        // 单一 @MapperScan 声明（避免双扫描/重复注册）
        assertThat(countOccurrences(out, "@MapperScan")).isEqualTo(1);
    }

    /**
     * 混合形态（D-4，2026-09-13 运行期闭环）：项目级 ANEMIC + 逐表解析出 RICH 产物
     * （{@code richDependencies=true}）——双 {@code @MapperScan}（mybatis-spring 2.0.2+
     * {@code @Repeatable}）：
     * <ul>
     *   <li>{@code @MapperScan(mapperPackage)}：贫血 Mapper（BaseRepository 子接口，无
     *       @Mapper）依赖包扫描注册；</li>
     *   <li>{@code @MapperScan(根包, annotationClass=@Mapper)}：RICH *BaseMapper（带
     *       org.apache.ibatis @Mapper）位于 {@code <root>.infrastructure.persistence}，
     *       不在 mapperPackage 扫描范围内——单扫 mapperPackage 会漏注册，
     *       {@code TOrderInfoRepositoryImpl} 注入失败（复现证据
     *       {@code .fastddd-ver-work/d3-fix-dev/repro/mixed-boot-d4-missing-mapper.log}）。</li>
     * </ul>
     * 两扫描无重叠：包扫描命中的接口均无 @Mapper（注解过滤扫描不再注册），
     * 注解过滤命中者不在 mapperPackage 内。
     */
    @Test
    void mixedRichDependenciesScansBothBoundaries() throws IOException {
        context.addCustomProperty("richDependencies", true);

        String out = renderer.renderProjectTemplate("Application.java.ftl", context);

        assertThat(out).contains("@MapperScan(\"com.test.app.mapper\")");
        assertThat(out).contains("@MapperScan(basePackages = \"com.test.app\", "
                + "annotationClass = org.apache.ibatis.annotations.Mapper.class)");
        assertThat(countOccurrences(out, "@MapperScan")).isEqualTo(2);
    }

    private static int countOccurrences(String haystack, String needle) {
        int count = 0;
        int idx = 0;
        while ((idx = haystack.indexOf(needle, idx)) >= 0) {
            count++;
            idx += needle.length();
        }
        return count;
    }
}
