package com.gallop.fastddd.maven.plugin;

import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * fastddd-version.properties 双版本轴契约测试（ECO-003 生成通道版本解耦）。
 *
 * <p>历史缺陷：源文件只有 {@code fastddd.version=@revision@}，插件默认把 <b>Tooling 自身
 * revision</b> 当作生成项目的 Framework 版本（按 Tooling 版本推导 Framework 版本）；
 * 生成项目的 fastddd-maven-plugin 版本又复用同一值（按 Framework 版本推导 Tooling 版本）。</p>
 *
 * <p>本测试从三个层面锁住修复：</p>
 * <ol>
 *   <li><b>源文件</b>：Framework 轴必须过滤 {@code @fastddd-framework.version@}（上游认证版本），
 *       不得回退到 {@code @revision@}；Tooling 轴独立过滤 {@code @revision@}。</li>
 *   <li><b>构建产物</b>：{@code target/classes} 中的过滤结果必须分别等于两个属性的<b>有效构建值</b>
 *       （CLI {@code -D} 覆盖优先，回落根 POM 字面量），且不得残留未过滤占位符——
 *       差异化版本构建（-Drevision 覆盖）时，该断言暴露"Tooling 版本泄漏为 Framework 版本"的原缺陷。</li>
 *   <li><b>Mojo 解析</b>：{@code -Dinit.fastdddVersion} / {@code -Dinit.fastdddToolingVersion}
 *       可独立覆盖，缺省互不推导。</li>
 * </ol>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class FastdddVersionPropertiesContractTest {

    private static final String SOURCE =
            "src/main/resources/fastddd-version.properties";
    private static final String FILTERED =
            "target/classes/fastddd-version.properties";

    @Test
    @SuppressWarnings("unused")
    void sourceBindsFrameworkAxisToUpstreamPropertyNotToolingRevision() throws Exception {
        String source = Files.readString(Path.of(SOURCE));
        // 原缺陷即这一行：fastddd.version=@revision@（Tooling 版本被当作 Framework 版本）
        assertTrue(source.contains("fastddd.version=@fastddd-framework.version@"),
                "Framework 轴必须过滤上游 fastddd-framework.version 属性，而非 Tooling 自身 revision");
        assertFalse(source.contains("fastddd.version=@revision@"),
                "禁止把 Tooling revision 写入 Framework 轴（历史耦合缺陷）");
        assertTrue(source.contains("fastddd.tooling.version=@revision@"),
                "Tooling 轴必须独立过滤自身 revision");
    }

    @Test
    void filteredArtifactResolvesEachAxisFromItsOwnBuildProperty() throws Exception {
        Path filtered = Path.of(FILTERED);
        assertTrue(Files.exists(filtered),
                "过滤产物缺失: " + filtered.toAbsolutePath()
                        + "（mvn test 场景 process-resources 必先执行）");
        Properties props = load(filtered);

        String frameworkVersion = props.getProperty("fastddd.version");
        String toolingVersion = props.getProperty("fastddd.tooling.version");
        assertNotNull(frameworkVersion, "产物须含 fastddd.version");
        assertNotNull(toolingVersion, "产物须含 fastddd.tooling.version");

        // 占位符必须已被替换（资源配置被移除时会原样泄漏）
        assertFalse(frameworkVersion.contains("@") || toolingVersion.contains("@"),
                "过滤产物残留占位符: framework=" + frameworkVersion + ", tooling=" + toolingVersion);

        File rootPom = findRootPom();
        assertEquals(effectiveProperty(rootPom, "fastddd-framework.version"), frameworkVersion,
                "Framework 轴 = 有效 fastddd-framework.version（CLI -D 覆盖优先，回落根 POM 字面量）");
        assertEquals(effectiveProperty(rootPom, "revision"), toolingVersion,
                "Tooling 轴 = 有效 revision（CLI -D 覆盖优先，回落根 POM 字面量）");
        // 差异化构建（-Drevision 覆盖）时两者不同，此断言锁定互不推导；
        // 默认构建两者同为 1.0.0-jdk21-SNAPSHOT，等值不断言（由隔离链差异化组合覆盖）。
    }

    @Test
    void mojoResolvesEachAxisWithIndependentOverride() throws Exception {
        InitProjectMojo mojo = new InitProjectMojo();
        mojo.setLog(noopLog());

        // 缺省：两轴各自回落到过滤产物中的值（= 各自构建属性的有效值）
        assertEquals(frameworkAxisDefault(mojo), frameworkProperty(),
                "缺省 Framework 轴取 properties 文件值（有效上游认证版本）");
        assertEquals(toolingAxisDefault(mojo), revisionProperty(),
                "缺省 Tooling 轴取 properties 文件值（有效自身 revision）");

        // -Dinit.fastdddToolingVersion 只影响 Tooling 轴
        setField(mojo, "fastdddToolingVersion", "9.9.9-tooling-override");
        assertEquals("9.9.9-tooling-override", toolingAxisDefault(mojo),
                "-Dinit.fastdddToolingVersion 须覆盖 Tooling 轴");
        assertEquals(frameworkProperty(), frameworkAxisDefault(mojo),
                "Tooling 覆盖不得改变 Framework 轴（互不推导）");

        // -Dinit.fastdddVersion 只影响 Framework 轴
        setField(mojo, "fastdddVersion", "9.9.8-framework-override");
        assertEquals("9.9.8-framework-override", frameworkAxisDefault(mojo),
                "-Dinit.fastdddVersion 须覆盖 Framework 轴");
        assertEquals("9.9.9-tooling-override", toolingAxisDefault(mojo),
                "Framework 覆盖不得改变 Tooling 轴（互不推导）");
    }

    // ==================== Mojo 反射辅助 ====================

    private static String frameworkAxisDefault(InitProjectMojo mojo) throws Exception {
        return invokeResolve(mojo, "resolveFastdddVersion");
    }

    private static String toolingAxisDefault(InitProjectMojo mojo) throws Exception {
        return invokeResolve(mojo, "resolveFastdddToolingVersion");
    }

    private static String invokeResolve(InitProjectMojo mojo, String method) throws Exception {
        Method m = InitProjectMojo.class.getDeclaredMethod(method);
        m.setAccessible(true);
        try {
            return (String) m.invoke(mojo);
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw new IllegalStateException("调用 " + method + " 失败", e.getCause());
        }
    }

    private static void setField(InitProjectMojo mojo, String name, String value) throws Exception {
        Field f = InitProjectMojo.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(mojo, value);
    }

    /** no-op Log：避免 Mojo 在无容器环境下 getLog() NPE。 */
    private static Log noopLog() {
        return new Log() {
            @Override public boolean isDebugEnabled() { return false; }
            @Override public boolean isInfoEnabled() { return false; }
            @Override public boolean isWarnEnabled() { return false; }
            @Override public boolean isErrorEnabled() { return false; }
            @Override public void debug(CharSequence c) { }
            @Override public void debug(CharSequence c, Throwable t) { }
            @Override public void debug(Throwable t) { }
            @Override public void info(CharSequence c) { }
            @Override public void info(CharSequence c, Throwable t) { }
            @Override public void info(Throwable t) { }
            @Override public void warn(CharSequence c) { }
            @Override public void warn(CharSequence c, Throwable t) { }
            @Override public void warn(Throwable t) { }
            @Override public void error(CharSequence c) { }
            @Override public void error(CharSequence c, Throwable t) { }
            @Override public void error(Throwable t) { }
        };
    }

    // ==================== 产物/根 POM 读取辅助 ====================

    private static Properties load(Path file) throws IOException {
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(file)) {
            props.load(in);
        }
        return props;
    }

    private static String frameworkProperty() throws Exception {
        return effectiveProperty(findRootPom(), "fastddd-framework.version");
    }

    private static String revisionProperty() throws Exception {
        return effectiveProperty(findRootPom(), "revision");
    }

    /**
     * 属性的<b>有效构建值</b>：CLI {@code -D} 覆盖（根 POM surefire 配置经
     * {@code systemPropertyVariables} 透传）优先，回落根 POM 文件字面量。
     *
     * <p>资源过滤（target/classes 产物）与版本治理针对的都是 session 有效值；
     * 只读 POM 字面量在差异化/RC 覆盖构建下会把正确产物误判为失败。</p>
     */
    private static String effectiveProperty(File rootPom, String name) {
        String overridden = System.getProperty(name);
        if (overridden != null && !overridden.isBlank()) {
            return overridden.trim();
        }
        return property(rootPom, name);
    }

    /** 从 CWD 向上查找 Tooling 根 pom（与 VersionGovernanceContractTest 同模式）。 */
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

    private static String property(File pom, String name) {
        Element root = parse(pom).getDocumentElement();
        Element props = firstChild(root, "properties");
        assertNotNull(props, pom + " 须有 <properties>");
        Element value = firstChild(props, name);
        assertNotNull(value, pom + " 须声明 <" + name + "> 属性");
        return value.getTextContent().trim();
    }

    private static Document parse(File pom) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newDefaultInstance();
            f.setNamespaceAware(false);
            DocumentBuilder b = f.newDocumentBuilder();
            return b.parse(pom);
        } catch (Exception e) {
            throw new IllegalStateException("解析 POM 失败: " + pom, e);
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
