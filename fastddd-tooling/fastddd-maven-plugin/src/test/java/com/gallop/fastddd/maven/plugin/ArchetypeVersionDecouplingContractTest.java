package com.gallop.fastddd.maven.plugin;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * fastddd-project-archetype 版本解耦契约测试（ECO-003 生成通道）。
 *
 * <p>历史缺陷：archetype 原型 POM 中 fastddd-maven-plugin 的版本写死为
 * {@code ${fastddd.version}}（Framework 轴），生成项目的 Tooling 插件版本由
 * Framework 版本推导；且无独立的 Tooling 版本属性。</p>
 *
 * <p><b>测试落点说明</b>：archetype 模块为 {@code maven-archetype} packaging
 * （纯资源打包，默认 lifecycle 不绑定 surefire），在其内部放置 JUnit 测试不会被执行。
 * 故本断言置于同 reactor、surefire 正常运行的 fastddd-maven-plugin 模块，
 * 经仓库根 POM 定位 archetype 资源文件。</p>
 *
 * <p>archetype 已 DEPRECATED，仅做必要兼容修复；本测试只锁版本轴语义，
 * 不扩展其功能。实际 {@code archetype:generate} 产物验证在 ECO-005 隔离链执行。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class ArchetypeVersionDecouplingContractTest {

    /** 模块内相对仓库根路径：聚合目录 fastddd-tooling/ 下的 archetype 模块。 */
    private static final String ARCHETYPE_MODULE = "fastddd-tooling/fastddd-project-archetype";

    /** 原型 POM 中 fastddd-maven-plugin 声明的版本引用（多行 DOTALL 匹配）。 */
    private static final Pattern PLUGIN_VERSION = Pattern.compile(
            "fastddd-maven-plugin</artifactId>\\s*<version>([^<]+)</version>", Pattern.DOTALL);

    @Test
    void archetypePomSeparatesFrameworkAndToolingVersionAxes() throws Exception {
        String pom = archetypeResource("src/main/resources/archetype-resources/pom.xml");

        // 两条独立属性轴均须声明（Tooling 轴为本次修复新增）
        assertTrue(pom.contains("<fastddd.version>${fastdddVersion}</fastddd.version>"),
                "原型 POM 须定义 Framework 轴属性（Velocity 变量 fastdddVersion 注入）");
        assertTrue(pom.contains("<fastddd-tooling.version>${fastdddToolingVersion}</fastddd-tooling.version>"),
                "原型 POM 须定义独立的 Tooling 轴属性");

        // 框架依赖走 Framework 轴
        assertTrue(pom.contains("<artifactId>fast-ddd-web</artifactId>"));
        assertTrue(pom.contains("<version>${fastddd.version}</version>"),
                "框架依赖版本走 ${fastddd.version}");

        // 插件版本走 Tooling 轴（历史缺陷：此处原为 ${fastddd.version}）
        Matcher m = PLUGIN_VERSION.matcher(pom);
        assertTrue(m.find(), "原型 POM 须声明 fastddd-maven-plugin 及其版本");
        assertEquals("${fastddd-tooling.version}", m.group(1).trim(),
                "fastddd-maven-plugin 版本必须走 Tooling 轴属性，不得复用 Framework 轴");
        assertFalse(m.find(), "原型 POM 不应出现第二处 fastddd-maven-plugin 版本声明");
    }

    @Test
    void archetypeMetadataRegistersBothVersionAxesWithDefaults() throws Exception {
        File metadata = archetypeResourceFile("src/main/resources/META-INF/maven/archetype-metadata.xml");
        Document doc = parse(metadata);

        String frameworkDefault = requiredPropertyDefault(doc, "fastdddVersion");
        String toolingDefault = requiredPropertyDefault(doc, "fastdddToolingVersion");
        assertNotNull(frameworkDefault, "archetype 须注册 fastdddVersion 必填属性");
        assertNotNull(toolingDefault, "archetype 须注册 fastdddToolingVersion 必填属性");
        assertTrue(toolingDefault.matches("\\d+\\.\\d+\\.\\d+(-[\\w.-]+)?"),
                "Tooling 轴默认值须为可解析版本字面量: " + toolingDefault);
        // 两条轴属性必须同时存在且独立注册（不互相引用占位符）
        assertNotEquals("${fastdddVersion}", toolingDefault,
                "Tooling 轴默认值不得引用 Framework 轴（互不推导）");
    }

    @Test
    void archetypeReadmePluginCommandUsesToolingAxis() throws Exception {
        String readme = archetypeResource("src/main/resources/archetype-resources/README.md");
        assertTrue(readme.contains("fastddd-maven-plugin:${fastddd-tooling.version}"),
                "README generate-code 命令须引用 ${fastddd-tooling.version}");
        assertFalse(readme.contains("fastddd-maven-plugin:${fastddd.version}"),
                "README 不得以 Framework 版本调用插件（历史耦合缺陷）");
    }

    // ==================== 定位/解析辅助 ====================

    private static File archetypeResourceFile(String relative) {
        File rootPom = findRootPom();
        File file = rootPom.getParentFile()
                .toPath()
                .resolve(ARCHETYPE_MODULE)
                .resolve(relative)
                .toFile();
        assertTrue(file.isFile(), "archetype 资源缺失: " + file.getAbsolutePath());
        return file;
    }

    private static String archetypeResource(String relative) throws Exception {
        return Files.readString(archetypeResourceFile(relative).toPath());
    }

    /** requiredProperty 的 defaultValue（无则 null）。 */
    private static String requiredPropertyDefault(Document doc, String key) {
        NodeList props = doc.getElementsByTagName("requiredProperty");
        for (int i = 0; i < props.getLength(); i++) {
            Element prop = (Element) props.item(i);
            if (key.equals(prop.getAttribute("key"))) {
                Element dv = firstChild(prop, "defaultValue");
                return dv == null ? null : dv.getTextContent().trim();
            }
        }
        return null;
    }

    /** 从 CWD 向上查找 Tooling 根 pom（与 FastdddVersionPropertiesContractTest 同模式）。 */
    private static File findRootPom() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (dir != null) {
            File candidate = dir.resolve("pom.xml").toFile();
            if (candidate.isFile() && "fastddd-tooling-parent".equals(artifactId(candidate))) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("未找到 artifactId=fastddd-tooling-parent 的根 pom（从 "
                + System.getProperty("user.dir") + " 向上）");
    }

    private static String artifactId(File pom) {
        Element root = parse(pom).getDocumentElement();
        Element aid = firstChild(root, "artifactId");
        return aid == null ? null : aid.getTextContent().trim();
    }

    private static Document parse(File xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newDefaultInstance();
            f.setNamespaceAware(false);
            DocumentBuilder b = f.newDocumentBuilder();
            return b.parse(xml);
        } catch (Exception e) {
            throw new IllegalStateException("解析 XML 失败: " + xml, e);
        }
    }

    private static Element firstChild(Element parent, String name) {
        NodeList nodes = parent.getElementsByTagName(name);
        for (int i = 0; i < nodes.getLength(); i++) {
            if (nodes.item(i).getParentNode() == parent) {
                return (Element) nodes.item(i);
            }
        }
        return null;
    }
}
