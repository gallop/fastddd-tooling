package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 生成项目版本解耦回归测试（ECO-003 后续：生成通道双版本轴）。
 *
 * <p>历史缺陷：{@code pom.xml.ftl} 中 fastddd-maven-plugin 复用 Framework 版本变量
 * {@code ${fastdddVersion}}，导致生成项目的 Tooling 插件版本由 Framework 版本推导。
 * 本测试用<b>互不相同</b>的 Framework / Tooling 版本渲染模板，断言两条版本轴各自独立：
 * 框架依赖走 Framework 轴，插件版本走 Tooling 轴（{@code fastddd-tooling.version} 属性）。</p>
 *
 * <p>断言基于渲染后的实际 POM（DOM 解析），不是模板文本匹配。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class GeneratedProjectVersionDecouplingTest {

    /** 三轴刻意互不相同，避免相同版本掩盖耦合。 */
    private static final String FRAMEWORK_VERSION = "9.9.1-framework-axis-SNAPSHOT";
    private static final String TOOLING_VERSION = "9.9.2-tooling-axis-SNAPSHOT";

    private TemplateRenderer renderer;

    @BeforeEach
    void setUp() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(TemplateContext.class, "/templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        // L4：注入文案字典（测试走缺省 zh，与历史断言的生成结果一致）
        TemplateMessages.apply(configuration, TemplateMessages.DEFAULT_LANGUAGE);

        renderer = new TemplateRenderer(configuration);
    }

    @Test
    void generatedPomSeparatesFrameworkAndToolingVersionAxes() throws Exception {
        String pom = renderer.render("project/pom.xml.ftl", contextWithDistinctVersions());

        Document doc = parse(pom);

        // Framework 轴：属性 + 全部框架依赖
        assertThat(propertyOf(doc, "fastddd.version")).isEqualTo(FRAMEWORK_VERSION);
        assertThat(dependencyVersionOf(doc, "fast-ddd-model")).isEqualTo(FRAMEWORK_VERSION);
        assertThat(dependencyVersionOf(doc, "fast-ddd-web")).isEqualTo(FRAMEWORK_VERSION);
        assertThat(dependencyVersionOf(doc, "fast-ddd-crud")).isEqualTo(FRAMEWORK_VERSION);

        // Tooling 轴：独立属性 + 插件引用该属性（而非 Framework 版本字面量）
        assertThat(propertyOf(doc, "fastddd-tooling.version")).isEqualTo(TOOLING_VERSION);
        assertThat(pluginVersionOf(doc, "fastddd-maven-plugin"))
                .as("fastddd-maven-plugin 版本必须走 fastddd-tooling.version 属性（Tooling 轴）")
                .isEqualTo("${fastddd-tooling.version}")
                .isNotEqualTo(FRAMEWORK_VERSION);
    }

    @Test
    void generatedReadmeUsesToolingAxisForPluginCommands() throws Exception {
        String readme = renderer.render("project/README.md.ftl", contextWithDistinctVersions());

        assertThat(readme)
                .as("README 的 generate-code 命令不得用 Framework 版本调用插件")
                .contains("fastddd-maven-plugin:${fastddd-tooling.version}:generate-code")
                .doesNotContain("fastddd-maven-plugin:" + FRAMEWORK_VERSION);
        assertThat(readme)
                .as("生成工具版本标签走 Tooling 轴")
                .contains("FastDDD Initializr v" + TOOLING_VERSION);
    }

    /**
     * 兼容回落：调用方未设置 Tooling 版本时（旧通道/直接 Map 渲染），
     * TemplateContext 提供独立字面量回落，插件版本不再跟随 Framework 轴。
     */
    @Test
    void toolingAxisFallsBackIndependentlyOfFrameworkAxis() throws Exception {
        TemplateContext ctx = TemplateContext.builder()
                .groupId("com.test")
                .artifactId("fallback-app")
                .version("1.0.0-SNAPSHOT")
                .packageName("com.test.app")
                .modelType(TemplateContext.ModelType.ANEMIC)
                .databaseType(TemplateContext.DatabaseType.MYSQL)
                .fastdddVersion(FRAMEWORK_VERSION)
                .build()
                .calculatePackages();

        String pom = renderer.render("project/pom.xml.ftl", ctx);
        Document doc = parse(pom);

        String toolingFallback = propertyOf(doc, "fastddd-tooling.version");
        assertThat(toolingFallback).isNotBlank();
        assertThat(toolingFallback)
                .as("回落值不得取自 Framework 轴（旧实现把插件版本钉在框架版本上）")
                .isNotEqualTo(FRAMEWORK_VERSION);
        assertThat(dependencyVersionOf(doc, "fast-ddd-web")).isEqualTo(FRAMEWORK_VERSION);
    }

    // ==================== 辅助 ====================

    private TemplateContext contextWithDistinctVersions() {
        return TemplateContext.builder()
                .groupId("com.test")
                .artifactId("decoupled-app")
                .version("1.0.0-SNAPSHOT")
                .packageName("com.test.app")
                .modelType(TemplateContext.ModelType.ANEMIC)
                .databaseType(TemplateContext.DatabaseType.MYSQL)
                .modules(java.util.Set.of("security"))
                .fastdddVersion(FRAMEWORK_VERSION)
                .fastdddToolingVersion(TOOLING_VERSION)
                .javaVersion("21")
                .springBootVersion("3.5.16")
                .build()
                .calculatePackages();
    }

    private static Document parse(String xml) throws Exception {
        DocumentBuilderFactory f = DocumentBuilderFactory.newDefaultInstance();
        f.setNamespaceAware(false);
        return f.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    private static String propertyOf(Document doc, String name) {
        NodeList properties = doc.getElementsByTagName("properties");
        for (int i = 0; i < properties.getLength(); i++) {
            NodeList children = properties.item(i).getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (name.equals(child.getNodeName())) {
                    return child.getTextContent().trim();
                }
            }
        }
        return null;
    }

    private static String dependencyVersionOf(Document doc, String artifactId) {
        return versionInBlock(doc, "dependencies", artifactId);
    }

    private static String pluginVersionOf(Document doc, String artifactId) {
        return versionInBlock(doc, "plugins", artifactId);
    }

    private static String versionInBlock(Document doc, String blockTag, String artifactId) {
        NodeList blocks = doc.getElementsByTagName(blockTag);
        for (int i = 0; i < blocks.getLength(); i++) {
            NodeList children = blocks.item(i).getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node entry = children.item(j);
                if (!("dependency".equals(entry.getNodeName()) || "plugin".equals(entry.getNodeName()))) {
                    continue;
                }
                String id = childText(entry, "artifactId");
                if (artifactId.equals(id)) {
                    return childText(entry, "version");
                }
            }
        }
        return null;
    }

    private static String childText(Node entry, String name) {
        NodeList children = entry.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (name.equals(child.getNodeName())) {
                return child.getTextContent().trim();
            }
        }
        return null;
    }
}
