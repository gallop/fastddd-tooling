package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * 模板文案字典（L4 模板国际化）
 * <p>
 * 按语言加载 {@code classpath:messages/messages_*.properties}，转为嵌套 Map 后注入
 * FreeMarker 共享变量 {@code msg}，模板内以 {@code ${msg.rich.aggregateRoot.suffix}} 引用，
 * 无需逐模板 import。语言参数由各入口（init.language / codegen.language / fastdoc.language）
 * 传入，缺省 {@code zh} 保持既有生成结果兼容。
 * <p>
 * 英文文案在中文字典基础上覆盖合并——en 缺失的键回退中文，避免部分翻译导致渲染失败。
 *
 * @author gallop
 * @since 1.0.0
 */
public final class TemplateMessages {

    /**
     * 缺省语言：zh（与历史生成结果保持一致）。
     */
    public static final String DEFAULT_LANGUAGE = "zh";

    /**
     * 共享变量名：模板内通过 {@code ${msg.xxx.yyy}} 访问文案。
     */
    public static final String SHARED_VARIABLE_NAME = "msg";

    private static final String BASE_NAME = "messages/messages";

    private TemplateMessages() {
    }

    /**
     * 规范化语言参数。
     * <p>zh/zh-CN/zh_cn… 归一为 {@code zh}；en/en-US… 归一为 {@code en}；
     * null/空白回退 {@code zh}；其他值抛出 {@link IllegalArgumentException}（入口层
     * 会将其转为用户可见的构建失败，避免静默生成错误语言的代码）。
     *
     * @param language 用户传入的语言参数
     * @return 归一化后的语言码（zh 或 en）
     */
    public static String normalize(String language) {
        if (language == null || language.isBlank()) {
            return DEFAULT_LANGUAGE;
        }
        String normalized = language.trim().toLowerCase(Locale.ROOT).replace('_', '-');
        if (normalized.equals("zh") || normalized.startsWith("zh-")) {
            return "zh";
        }
        if (normalized.equals("en") || normalized.startsWith("en-")) {
            return "en";
        }
        throw new IllegalArgumentException(
            "不支持的语言: '" + language + "'（可选值: zh、en）");
    }

    /**
     * 加载指定语言的文案字典。
     * <p>en 为「zh 全量 + en 覆盖」的深合并结果，保证键集不小于 zh。
     *
     * @param language 语言参数（经 {@link #normalize} 归一）
     * @return 嵌套文案 Map（键按 {@code .} 分层）
     */
    public static Map<String, Object> load(String language) {
        String lang = normalize(language);
        Map<String, Object> merged = read("zh");
        if ("en".equals(lang)) {
            merge(merged, read("en"));
        }
        return merged;
    }

    /**
     * 将文案字典注入 FreeMarker 共享变量 {@code msg}。
     * <p>每个 Configuration 构建后调用一次即可，其加载的全部模板共享该变量。
     *
     * @param cfg      FreeMarker Configuration
     * @param language 语言参数（zh/en，空白回退 zh，非法值快速失败）
     * @throws IllegalStateException properties 资源缺失或注入失败（属打包/配置错误）
     */
    public static void apply(Configuration cfg, String language) {
        try {
            cfg.setSharedVariable(SHARED_VARIABLE_NAME, load(language));
        } catch (TemplateModelException e) {
            throw new IllegalStateException("注入模板文案共享变量失败: " + SHARED_VARIABLE_NAME, e);
        }
    }

    // ==================== 内部实现 ====================

    private static Map<String, Object> read(String language) {
        String resource = BASE_NAME + "_" + language + ".properties";
        Properties properties = new Properties();
        try (InputStream input = TemplateMessages.class.getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("模板文案资源未找到: " + resource);
            }
            try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
                properties.load(reader);
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取模板文案资源失败: " + resource, e);
        }
        Map<String, Object> root = new HashMap<>();
        for (String name : properties.stringPropertyNames()) {
            putNested(root, name, properties.getProperty(name));
        }
        return root;
    }

    /**
     * 将 {@code a.b.c = value} 展开为嵌套 Map 结构；中间层与叶子冲突时快速失败，
     * 避免字典键命名事故被静默覆盖。
     */
    private static void putNested(Map<String, Object> root, String key, String value) {
        String[] parts = key.split("\\.");
        Map<String, Object> current = root;
        for (int i = 0; i < parts.length - 1; i++) {
            Object existing = current.get(parts[i]);
            if (existing == null) {
                Map<String, Object> next = new HashMap<>();
                current.put(parts[i], next);
                current = next;
            } else if (existing instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> next = (Map<String, Object>) existing;
                current = next;
            } else {
                throw new IllegalStateException("模板文案键冲突: '" + key
                    + "' 与既有叶子键 '" + parts[i] + "' 层级重叠");
            }
        }
        String leaf = parts[parts.length - 1];
        if (current.containsKey(leaf) && !(current.get(leaf) instanceof String)) {
            throw new IllegalStateException("模板文案键冲突: '" + key
                + "' 与既有中间层键 '" + leaf + "' 层级重叠");
        }
        current.put(leaf, value);
    }

    @SuppressWarnings("unchecked")
    private static void merge(Map<String, Object> base, Map<String, Object> override) {
        for (Map.Entry<String, Object> entry : override.entrySet()) {
            Object existing = base.get(entry.getKey());
            if (existing instanceof Map && entry.getValue() instanceof Map) {
                merge((Map<String, Object>) existing, (Map<String, Object>) entry.getValue());
            } else {
                base.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
