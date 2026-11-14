package com.gallop.fastddd.templates;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 生成通道 ID 类型契约守卫（ID-008）——防止脚手架模板向生成产物注入弱类型 ID。
 *
 * <p>取证（ID-008）发现：主实体模板 {@code anemic/Entity.ftl}、{@code rich/po.ftl} 已在 ID-007
 * 迁移到强类型 {@code extends BasePO<Long>}，但 6 个脚手架模板（{@code project/security/*} ×5 +
 * {@code project/context/PO.ftl}）仍发原始 {@code extends BasePO}（无类型参数）。原始类型使生成产物
 * 的 {@code getId()} 退化为 {@code Serializable} 弱类型，消费方须 {@code (Long) po.getId()} 强转——
 * 正是 ADR-001 / ID-001～ID-007 整改消除的"旧 ID 模型"。</p>
 *
 * <p>本测试机械断言：<b>所有 {@code .ftl} 模板内的 {@code extends BasePO} 必须带类型参数</b>
 * （即 {@code BasePO<}）。新增模板若漏写 {@code <Long>} 立即编译期/测试期拦截，确保
 * FastDDD 1.0 生成通道不再向新公共 API 引入旧 ID 模型（ID-008 验收第 3 条）。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class TemplateIdTypeContractTest {

    private static final Pattern EXTENDS_BASE_PO = Pattern.compile("extends\\s+BasePO\\b");

    @Test
    void allTemplatesEmitTypedBasePo_noRawExtendsBasePo() throws IOException {
        List<String> violations = new ArrayList<>();
        Path templatesDir = resolveTemplatesDir();

        try (Stream<Path> walk = Files.walk(templatesDir)) {
            List<Path> ftls = walk.filter(p -> p.toString().endsWith(".ftl")).sorted().toList();
            assertThat(ftls).as("须发现至少一个 .ftl 模板（templates 目录解析: %s）", templatesDir).isNotEmpty();

            for (Path ftl : ftls) {
                List<String> lines = Files.readAllLines(ftl, StandardCharsets.UTF_8);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    Matcher m = EXTENDS_BASE_PO.matcher(line);
                    if (m.find()) {
                        // 同一 BasePO 词必须紧跟 '<'（类型参数）；原始类型即违例。
                        int idx = m.end(); // 指向 "BasePO" 之后第一个字符
                        boolean typed = idx < line.length() && line.charAt(idx) == '<';
                        if (!typed) {
                            violations.add(ftl.getFileName() + ":" + (i + 1) + " → " + line.trim());
                        }
                    }
                }
            }
        }

        assertThat(violations)
                .as("所有模板的 `extends BasePO` 必须带类型参数（如 BasePO<Long>）；" +
                            "原始类型使生成产物 getId() 退化为 Serializable 弱类型（ID-008）")
                .isEmpty();
    }

    /** 定位模板源目录：从 CWD 向上查找 src/main/resources/templates（兼顾 mvn 模块目录与 IDE 工作区）。 */
    private static Path resolveTemplatesDir() {
        Path dir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        while (dir != null) {
            Path candidate = dir.resolve("src/main/resources/templates");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
            dir = dir.getParent();
        }
        throw new IllegalStateException("未找到 src/main/resources/templates（从 "
                + System.getProperty("user.dir") + " 向上）");
    }
}
