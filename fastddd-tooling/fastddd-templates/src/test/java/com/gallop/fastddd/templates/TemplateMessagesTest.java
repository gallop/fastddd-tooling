package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * {@link TemplateMessages} 契约测试（L4 模板国际化）
 *
 * <ul>
 *   <li>normalize：zh/en 变体归一、空白回退、非法值快速失败；</li>
 *   <li>load：嵌套 Map 结构、en 覆盖 zh、键集 parity（en ⊇ zh，防部分翻译渲染失败）；</li>
 *   <li>apply：FreeMarker 共享变量注入后模板可经 ${msg.a.b} 取值。</li>
 * </ul>
 *
 * @author gallop
 * @since 1.0.0
 */
class TemplateMessagesTest {

    @Test
    void rawDictionariesHaveTheSameKeysBeforeFallbackMerge() throws Exception {
        java.util.Properties zh = raw("zh");
        java.util.Properties en = raw("en");
        assertThat(en.stringPropertyNames()).containsExactlyInAnyOrderElementsOf(zh.stringPropertyNames());
    }

    private java.util.Properties raw(String language) throws Exception {
        java.util.Properties properties = new java.util.Properties();
        try (var stream = TemplateMessages.class.getResourceAsStream("/messages/messages_" + language + ".properties")) {
            assertThat(stream).isNotNull();
            try (var reader = new java.io.InputStreamReader(stream, java.nio.charset.StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        }
        return properties;
    }

    @Test
    void normalizeAcceptsLanguageVariantsAndFallsBackToZh() {
        assertThat(TemplateMessages.normalize(null)).isEqualTo("zh");
        assertThat(TemplateMessages.normalize("  ")).isEqualTo("zh");
        assertThat(TemplateMessages.normalize("zh")).isEqualTo("zh");
        assertThat(TemplateMessages.normalize("ZH-CN")).isEqualTo("zh");
        assertThat(TemplateMessages.normalize("zh_CN")).isEqualTo("zh");
        assertThat(TemplateMessages.normalize("en")).isEqualTo("en");
        assertThat(TemplateMessages.normalize("EN-US")).isEqualTo("en");
    }

    @Test
    void normalizeRejectsUnknownLanguageFast() {
        assertThatThrownBy(() -> TemplateMessages.normalize("fr"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("fr");
        assertThatThrownBy(() -> TemplateMessages.normalize("english"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TemplateMessages.normalize("zhgarbage"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    void loadBuildsNestedMapFromDottedKeys() {
        Map<String, Object> zh = TemplateMessages.load("zh");
        Map<String, Object> rich = (Map<String, Object>) zh.get("rich");
        assertThat(rich).isNotNull();
        assertThat(rich.get("aggregate.root.suffix")).isNull();
        Map<String, Object> aggregate = (Map<String, Object>) rich.get("aggregate");
        Map<String, Object> root = (Map<String, Object>) aggregate.get("root");
        assertThat((String) root.get("suffix")).isEqualTo("聚合根");
    }

    @Test
    void loadEnOverridesZhWhileKeepingKeyParity() {
        Map<String, Object> zh = TemplateMessages.load("zh");
        Map<String, Object> en = TemplateMessages.load("en");

        assertThat(value(en, "common.validation.notBlank")).isEqualTo(" must not be empty");
        assertThat(value(zh, "common.validation.notBlank")).isEqualTo("不能为空");
        // parity：en 键集不小于 zh（缺失键将回退中文，此处守住字典完备性约定）
        assertThat(flatten(en).keySet()).containsAll(flatten(zh).keySet());
    }

    @Test
    void applyInjectsSharedVariableVisibleToTemplates() throws Exception {
        java.io.StringWriter out = new java.io.StringWriter();
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        TemplateMessages.apply(cfg, "zh");
        new freemarker.template.Template("t", "x=${msg.common.validation.notBlank}", cfg)
            .process(new java.util.HashMap<>(), out);
        assertThat(out.toString()).isEqualTo("x=不能为空");
    }

    @Test
    void applyInjectsEnglishDictionaryWhenLanguageEn() throws Exception {
        java.io.StringWriter out = new java.io.StringWriter();
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        TemplateMessages.apply(cfg, "en");
        new freemarker.template.Template("t", "x=${msg.common.validation.notBlank}", cfg)
            .process(new java.util.HashMap<>(), out);
        // en 值自带前导空格（与模板内无字面空格的写法配合），此处断言完整回代结果
        assertThat(out.toString()).isEqualTo("x= must not be empty");
    }

    private static String value(Map<String, Object> map, String dottedKey) {
        Object current = map;
        for (String part : dottedKey.split("\\.")) {
            current = ((Map<?, ?>) current).get(part);
        }
        return (String) current;
    }

    private static Map<String, String> flatten(Map<String, Object> map) {
        java.util.LinkedHashMap<String, String> flat = new java.util.LinkedHashMap<>();
        flattenInto(map, new StringBuilder(), flat);
        return flat;
    }

    @SuppressWarnings("unchecked")
    private static void flattenInto(Map<String, Object> map, StringBuilder prefix, Map<String, String> out) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            int mark = prefix.length();
            if (mark > 0) {
                prefix.append('.');
            }
            prefix.append(entry.getKey());
            if (entry.getValue() instanceof Map) {
                flattenInto((Map<String, Object>) entry.getValue(), prefix, out);
            } else {
                out.put(prefix.toString(), String.valueOf(entry.getValue()));
            }
            prefix.setLength(mark);
        }
    }
}
