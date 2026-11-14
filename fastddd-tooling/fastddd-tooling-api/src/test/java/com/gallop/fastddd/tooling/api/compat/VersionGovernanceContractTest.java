package com.gallop.fastddd.tooling.api.compat;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 版本治理契约校验（VER-001 步骤 6/7）——断言编译/构建本身捕获不到的版本一致性不变量。
 *
 * <p>构建能捕获的（reactor 内依赖缺版本管理 → 编译失败）不在本测试范围；本测试专守构建盲区：</p>
 * <ul>
 *   <li><b>分仓版本边界</b>：Tooling 根 POM 必须 import Framework BOM；Tooling 坐标不能反向进入
 *       Framework BOM。</li>
 *   <li><b>Tooling 内部依赖管理</b>：根 POM 必须以当前 {@code revision} 管理全部 Tooling 模块，
 *       避免 RC/GA 覆盖版本时无版本内部依赖无法解析。</li>
 *   <li><b>步骤 6</b>：当前 Tooling/Framework <b>有效构建</b>组合（CLI -D 覆盖优先于 POM 字面量）
 *       须在兼容矩阵内登记。合成验证模式（-Dfastddd.matrix.syntheticValidation=true，
 *       仅供隔离验证链）下断言反转：合成组合不得混入正式矩阵。</li>
 *   <li><b>步骤 1</b>：矩阵 current-stable 线的平台 manifest 须与 docs/COMPATIBILITY_MATRIX.md 基线一致。</li>
 * </ul>
 *
 * <p>POM 解析用 JDK 内置 DOM；定位根 pom 采用从 CWD 向上查找 artifactId=fastddd-tooling-parent 的稳健方式
 * （兼顾 mvn 与 IDE 执行）。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class VersionGovernanceContractTest {

    private static final String GROUP = "io.github.gallop.fastddd";

    @Test
    void toolingRootImportsFrameworkBomAtConfiguredFrameworkVersion() throws Exception {
        File rootPom = findRootPom();
        Element dependencyManagement = firstChild(parse(rootPom).getDocumentElement(), "dependencyManagement");
        assertThat(dependencyManagement).as("Tooling 根 POM 须声明 dependencyManagement").isNotNull();
        assertThat(hasDependency(dependencyManagement, "fastddd-bom",
                "${fastddd-framework.version}", "import"))
                .as("Tooling 必须以独立的 fastddd-framework.version import Framework BOM")
                .isTrue();
    }

    @Test
    void toolingRootManagesAllToolingModulesAtCurrentRevision() throws Exception {
        File rootPom = findRootPom();
        Set<String> managedArtifacts = fastdddArtifactsInDepMgmt(rootPom);
        assertThat(managedArtifacts).contains(
                "fastddd-tooling-api",
                "fastddd-templates",
                "fastddd-fastdoc-core",
                "fastddd-fastdoc-spring-boot-starter",
                "fastddd-code-generator",
                "fastddd-doc-generator",
                "fastddd-maven-plugin",
                "fastddd-doc-maven-plugin",
                "fastddd-project-archetype");
    }

    /**
     * 合成验证模式标志（{@code -Dfastddd.matrix.syntheticValidation=true}）。
     *
     * <p>仅供隔离验证链使用合成差异化版本（如 9.9.x-axis-SNAPSHOT）暴露版本耦合缺陷：
     * 合成组合<b>不得</b>登记进正式矩阵——正式矩阵只登记经发布门禁认证的真实组合。
     * 该模式下本测试断言反转（合成组合不在册 = 矩阵卫生），其余治理断言不变。</p>
     *
     * <p>未设置标志时（默认，含正式发布门禁）：严格断言有效构建组合在册——
     * RC/GA 覆盖构建（-Drevision=1.0.0-RC1 等）未经矩阵登记将被本测试阻断。</p>
     */
    private static final boolean SYNTHETIC_VALIDATION =
            Boolean.getBoolean("fastddd.matrix.syntheticValidation");

    @Test
    void currentToolingFrameworkPairIsRegisteredInMatrix_step6() throws Exception {
        File rootPom = findRootPom();
        // 有效构建值（CLI -D 覆盖优先）：登记性治理针对的是实际构建组合，
        // 而非 POM 文件默认线——否则 RC/差异化覆盖构建会绕过矩阵治理。
        String toolingRevision = effectiveProperty(rootPom, "revision");
        String frameworkRevision = effectiveProperty(rootPom, "fastddd-framework.version");
        CompatibilityMatrix matrix = CompatibilityMatrix.load();
        if (SYNTHETIC_VALIDATION) {
            assertThat(matrix.isCompatible(null, toolingRevision, frameworkRevision))
                    .as("合成验证组合 (%s, %s) 不得登记进正式矩阵（正式认证走发布门禁登记流程）",
                            toolingRevision, frameworkRevision)
                    .isFalse();
        } else {
            assertThat(matrix.isCompatible(null, toolingRevision, frameworkRevision))
                    .as("当前 Tooling/Framework 有效构建组合 (%s, %s) 须在兼容矩阵内登记",
                            toolingRevision, frameworkRevision)
                    .isTrue();
        }
        // 两种模式下均断言 POM 文件默认（开发线）组合在册——合成验证不放松正式线治理。
        assertThat(matrix.isCompatible(null,
                property(rootPom, "revision"), property(rootPom, "fastddd-framework.version")))
                .as("POM 默认组合 (%s, %s) 须在兼容矩阵内登记（不受覆盖构建影响）",
                        property(rootPom, "revision"), property(rootPom, "fastddd-framework.version"))
                .isTrue();
    }

    @Test
    void matrixManifestMatchesBaseline_step1() {
        CompatibilityMatrix matrix = CompatibilityMatrix.load();
        String revision = matrix.currentFrameworkRevision();
        CompatibilityMatrix.PlatformManifest manifest = matrix.manifestFor(revision);
        assertThat(manifest).as("current-stable 线须有 manifest").isNotNull();
        // 基线须与 docs/COMPATIBILITY_MATRIX.md "关键依赖矩阵（1.0.x 线）" 一致。
        assertThat(manifest.java()).isEqualTo("21");
        assertThat(manifest.springBoot()).isEqualTo("3.5.16");
        assertThat(manifest.springAi()).isEqualTo("1.1.8");
        assertThat(manifest.springdoc()).isEqualTo("2.8.17");
        assertThat(manifest.knife4j()).isEqualTo("4.5.0");
    }

    // ==================== POM 解析辅助 ====================

    /** 从 CWD 向上查找 Tooling 根 pom（兼顾 mvn 模块目录与 IDE 工作区）。 */
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
        assertThat(props).as(pom + " 须有 <properties>").isNotNull();
        Element value = firstChild(props, name);
        assertThat(value).as(pom + " 须声明 <" + name + "> 属性").isNotNull();
        return value.getTextContent().trim();
    }

    /**
     * 属性的<b>有效构建值</b>：CLI {@code -D} 覆盖（根 POM surefire 配置经
     * {@code systemPropertyVariables} 透传）优先，回落 POM 文件字面量。
     * CI-friendly {@code revision} 与上游 {@code fastddd-framework.version} 均可被覆盖。
     */
    private static String effectiveProperty(File pom, String name) {
        String overridden = System.getProperty(name);
        if (overridden != null && !overridden.isBlank()) {
            return overridden.trim();
        }
        return property(pom, name);
    }

    private static Set<String> fastdddArtifactsInDepMgmt(File pom) {
        Set<String> out = new LinkedHashSet<>();
        Element root = parse(pom).getDocumentElement();
        Element dm = firstChild(root, "dependencyManagement");
        if (dm == null) return out;
        Element deps = firstChild(dm, "dependencies");
        if (deps == null) return out;
        NodeList depList = deps.getElementsByTagName("dependency");
        for (int i = 0; i < depList.getLength(); i++) {
            Element dep = (Element) depList.item(i);
            Element gid = firstChild(dep, "groupId");
            Element aid = firstChild(dep, "artifactId");
            if (gid != null && GROUP.equals(gid.getTextContent().trim()) && aid != null) {
                out.add(aid.getTextContent().trim());
            }
        }
        return out;
    }

    private static boolean hasDependency(Element dependencyManagement, String artifactId,
                                         String version, String scope) {
        Element dependencies = firstChild(dependencyManagement, "dependencies");
        if (dependencies == null) return false;
        NodeList depList = dependencies.getElementsByTagName("dependency");
        for (int i = 0; i < depList.getLength(); i++) {
            Element dependency = (Element) depList.item(i);
            Element aid = firstChild(dependency, "artifactId");
            Element depVersion = firstChild(dependency, "version");
            Element depScope = firstChild(dependency, "scope");
            if (aid != null && artifactId.equals(aid.getTextContent().trim())
                    && depVersion != null && version.equals(depVersion.getTextContent().trim())
                    && depScope != null && scope.equals(depScope.getTextContent().trim())) {
                return true;
            }
        }
        return false;
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
