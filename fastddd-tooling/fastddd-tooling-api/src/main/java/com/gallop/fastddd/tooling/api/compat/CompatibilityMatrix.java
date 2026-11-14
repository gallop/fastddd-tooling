package com.gallop.fastddd.tooling.api.compat;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 跨产品兼容矩阵（VER-001 步骤 4）—— Studio → Tooling → Framework 版本组合的机器可读契约访问器。
 *
 * <p>规范源为 classpath 资源 {@code fastddd-compatibility-matrix.yaml}（本模块 {@code src/main/resources}）。
 * 人类可读镜像见 {@code docs/COMPATIBILITY_MATRIX.md}。本访问器提供三件事：</p>
 * <ol>
 *   <li><b>平台 manifest</b>（{@link #manifestFor(String)}）：某 Framework 版本线的
 *       Boot/Java/Spring AI/SpringDoc/Knife4j 平台依赖基线（步骤 1）。</li>
 *   <li><b>目标 Framework 版本</b>（{@link #currentFrameworkRevision()}）：当前 stable 线的 revision，
 *       供生成通道（Maven Plugin / Studio）决定生成项目应依赖的 Framework 版本——
 *       <b>不复制 Studio/Tooling 自身版本</b>（步骤 5）。</li>
 *   <li><b>组合合法性判定</b>（{@link #isCompatible(String, String, String)}）：判断给定
 *       (Studio, Tooling, Framework) 元组是否已登记，供构建期/CI 阻断未登记的不兼容组合（步骤 6）。</li>
 * </ol>
 *
 * <p><b>版本独立化</b>（ADR-005 §3.2）：三仓分别拥有自身 {@code revision}，下游通过独立的
 * 上游版本属性选择已发布制品。本矩阵登记允许的差异化元组，CI 据此阻断未认证组合；
 * 三个版本值暂时相同只代表当前候选组合，不构成锁步约束。</p>
 *
 * <p>本类为无状态契约，{@link #load()} 每次返回新实例；调用方通常缓存单例。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public final class CompatibilityMatrix {

    private static final String RESOURCE = "/fastddd-compatibility-matrix.yaml";
    private static final String STATUS_CURRENT_STABLE = "current-stable";

    private final List<FrameworkLine> frameworkLines;
    private final List<CompatibleTuple> compatibleTuples;

    private CompatibilityMatrix(List<FrameworkLine> frameworkLines,
                                List<CompatibleTuple> compatibleTuples) {
        this.frameworkLines = List.copyOf(frameworkLines);
        this.compatibleTuples = List.copyOf(compatibleTuples);
    }

    /**
     * 从 classpath 加载矩阵。资源缺失视为契约违约，抛 {@link IllegalStateException}
     * （矩阵是构建期/生成期的硬依赖，不应静默降级）。
     */
    public static CompatibilityMatrix load() {
        try (InputStream in = CompatibilityMatrix.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("兼容矩阵资源缺失: " + RESOURCE);
            }
            Map<String, Object> root = new Yaml().load(in);
            return parse(root);
        } catch (IOException e) {
            throw new IllegalStateException("读取兼容矩阵失败: " + RESOURCE, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static CompatibilityMatrix parse(Map<String, Object> root) {
        List<FrameworkLine> lines = new ArrayList<>();
        for (Map<String, Object> raw : asList(root.get("framework-lines"))) {
            lines.add(new FrameworkLine(
                    str(raw.get("id")),
                    str(raw.get("status")),
                    str(raw.get("revision")),
                    str(raw.get("description")),
                    parseManifest(asMap(raw.get("manifest")))));
        }
        List<CompatibleTuple> tuples = new ArrayList<>();
        for (Map<String, Object> raw : asList(root.get("compatible-tuples"))) {
            tuples.add(new CompatibleTuple(
                    str(raw.get("studio")),
                    str(raw.get("tooling")),
                    str(raw.get("framework")),
                    str(raw.get("status")),
                    str(raw.get("description"))));
        }
        return new CompatibilityMatrix(lines, tuples);
    }

    @SuppressWarnings("unchecked")
    private static PlatformManifest parseManifest(Map<String, Object> raw) {
        return new PlatformManifest(
                str(raw.get("java")),
                str(raw.get("spring-boot")),
                str(raw.get("spring-ai")),
                str(raw.get("springdoc")),
                str(raw.get("knife4j")));
    }

    /** 全部 Framework 版本线（不可变）。 */
    public List<FrameworkLine> getFrameworkLines() {
        return frameworkLines;
    }

    /** 全部合法跨产品元组（不可变）。 */
    public List<CompatibleTuple> getCompatibleTuples() {
        return compatibleTuples;
    }

    /**
     * 当前 stable Framework 版本线的 revision —— 生成通道用此作为生成项目的目标 Framework 版本。
     *
     * @throws IllegalStateException 矩阵未声明任何 {@code status=current-stable} 线时
     */
    public String currentFrameworkRevision() {
        return frameworkLines.stream()
                .filter(l -> STATUS_CURRENT_STABLE.equals(l.status()))
                .map(FrameworkLine::revision)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "兼容矩阵未声明 status=" + STATUS_CURRENT_STABLE + " 的 Framework 版本线"));
    }

    /**
     * 当前 status=current 的已认证兼容元组。
     */
    public CompatibleTuple currentTuple() {
        return compatibleTuples.stream()
                .filter(t -> "current".equals(t.status()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("兼容矩阵未声明 status=current 的元组"));
    }

    /**
     * 取某 Framework 版本线的平台 manifest。
     *
     * @param frameworkRevision Framework revision（如 {@code 1.0.0-jdk21-SNAPSHOT}）
     * @return 该线 manifest；未知 revision 返回 {@code null}
     */
    public PlatformManifest manifestFor(String frameworkRevision) {
        return frameworkLines.stream()
                .filter(l -> Objects.equals(l.revision(), frameworkRevision))
                .map(FrameworkLine::manifest)
                .findFirst()
                .orElse(null);
    }

    /**
     * 判定 (Studio, Tooling, Framework) 组合是否在矩阵内。
     *
     * <p>任一参数为 {@code null} 视为该维度通配（不参与匹配）——便于仅校验部分维度。
     * 例如仅校验 framework 合法性时可传 {@code isCompatible(null, null, revision)}。</p>
     */
    public boolean isCompatible(String studio, String tooling, String framework) {
        return compatibleTuples.stream().anyMatch(t ->
                (studio == null || Objects.equals(t.studio(), studio))
                        && (tooling == null || Objects.equals(t.tooling(), tooling))
                        && (framework == null || Objects.equals(t.framework(), framework)));
    }

    // ==================== 解析辅助 ====================

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object raw) {
        if (raw instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>(list.size());
            for (Object e : list) {
                if (e instanceof Map<?, ?> m) {
                    out.add(asMap(m));
                }
            }
            return out;
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object raw) {
        if (raw instanceof Map<?, ?> m) {
            Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : m.entrySet()) {
                out.put(String.valueOf(e.getKey()), e.getValue());
            }
            return out;
        }
        return Collections.emptyMap();
    }

    private static String str(Object raw) {
        return raw == null ? null : String.valueOf(raw);
    }

    // ==================== 契约类型 ====================

    /**
     * Framework 版本线：一条产品线（如 1.0.0-jdk21）的平台基线。
     */
    public record FrameworkLine(String id, String status, String revision,
                                 String description, PlatformManifest manifest) {
    }

    /**
     * 平台 manifest：某 Framework 版本线联调验证通过的第三方平台依赖版本。
     */
    public record PlatformManifest(String java, String springBoot, String springAi,
                                    String springdoc, String knife4j) {
    }

    /**
     * 合法的跨产品版本元组。
     */
    public record CompatibleTuple(String studio, String tooling, String framework,
                                   String status, String description) {
    }
}
