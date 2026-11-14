package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 模板渲染器
 * <p>
 * 提供便捷的模板渲染功能，统一 FreeMarker 模板的使用方式。
 * <p>
 * 模板文案经共享变量 {@code msg}（{@link TemplateMessages#apply}）注入；对未注入字典的
 * 既有调用约定（直接 {@code new TemplateRenderer(configuration).render(...)}），渲染时
 * 兜底注入默认中文（{@link TemplateMessages#DEFAULT_LANGUAGE}）字典的局部副本——
 * 不回写 {@link Configuration}，亦不覆盖调用方已显式传入或入口层已注入的
 * 英文/自定义 {@code msg}，避免共享配置被兜底值污染。
 *
 * @author FastDDD Generator
 * @since 1.0.0
 */
@Slf4j
public class TemplateRenderer {

    private final Configuration configuration;

    /**
     * 默认语言（zh）字典的进程级缓存：深不可变副本，供旧调用约定的渲染兜底复用。
     */
    private static volatile Map<String, Object> defaultMessages;

    /**
     * 构造函数
     *
     * @param configuration FreeMarker 配置
     */
    public TemplateRenderer(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 渲染模板
     *
     * @param templateName 模板名称（相对于 templates 目录）
     * @param context 模板上下文
     * @return 渲染结果
     * @throws IOException 模板渲染失败
     */
    public String render(String templateName, TemplateContext context) throws IOException {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(withDefaultMessages(context.toTemplateVariables()), writer);
            return writer.toString();
        } catch (TemplateException e) {
            log.error("模板渲染失败: templateName={}, error={}", templateName, e.getMessage(), e);
            throw new IOException("模板渲染失败: " + templateName, e);
        }
    }

    /**
     * 渲染模板（直接传入变量 Map）
     *
     * @param templateName 模板名称
     * @param variables 模板变量
     * @return 渲染结果
     * @throws IOException 模板渲染失败
     */
    public String render(String templateName, java.util.Map<String, Object> variables) throws IOException {
        try {
            Template template = configuration.getTemplate(templateName);
            StringWriter writer = new StringWriter();
            template.process(withDefaultMessages(variables), writer);
            return writer.toString();
        } catch (TemplateException e) {
            log.error("模板渲染失败: templateName={}, error={}", templateName, e.getMessage(), e);
            throw new IOException("模板渲染失败: " + templateName, e);
        }
    }

    /**
     * 从字符串渲染模板
     *
     * @param templateContent 模板内容
     * @param variables 模板变量
     * @return 渲染结果
     * @throws IOException 模板渲染失败
     */
    public String renderFromString(String templateContent, java.util.Map<String, Object> variables) throws IOException {
        try {
            freemarker.template.Template template = new Template("inline", new StringReader(templateContent), configuration);
            StringWriter writer = new StringWriter();
            template.process(withDefaultMessages(variables), writer);
            return writer.toString();
        } catch (TemplateException e) {
            log.error("字符串模板渲染失败: error={}", e.getMessage(), e);
            throw new IOException("字符串模板渲染失败", e);
        }
    }

    /**
     * 旧调用约定的字典兜底：仅在以下两个条件同时不成立时，向本次渲染的变量副本注入默认中文字典。
     * <ul>
     *   <li>调用方已在变量中显式传入 {@code msg}（自定义字典）；</li>
     *   <li>入口层已通过 {@link TemplateMessages#apply} 在 {@link Configuration} 上注入共享变量
     *       {@code msg}（zh / en / 自定义）。</li>
     * </ul>
     * 兜底值只进入本次渲染的局部副本，绝不回写 {@link Configuration}——共享配置不会因兜底
     * 而被默认语言污染，同一配置上的其他渲染不受影响。
     *
     * @param variables 模板变量
     * @return 实际传给 FreeMarker 的变量（兜底时为副本）
     */
    private Map<String, Object> withDefaultMessages(Map<String, Object> variables) {
        if (variables.containsKey(TemplateMessages.SHARED_VARIABLE_NAME)) {
            return variables;
        }
        if (configuration.getSharedVariable(TemplateMessages.SHARED_VARIABLE_NAME) != null) {
            return variables;
        }
        Map<String, Object> withFallback = new HashMap<>(variables);
        withFallback.put(TemplateMessages.SHARED_VARIABLE_NAME, defaultMessages());
        return withFallback;
    }

    /**
     * 默认语言字典的懒加载缓存（双重检查锁）；{@link TemplateMessages#load} 的深不可变包装，
     * 供并发渲染共享只读。
     *
     * @return 默认中文字典（不可变）
     */
    private static Map<String, Object> defaultMessages() {
        Map<String, Object> cached = defaultMessages;
        if (cached == null) {
            synchronized (TemplateRenderer.class) {
                cached = defaultMessages;
                if (cached == null) {
                    cached = deepUnmodifiable(TemplateMessages.load(TemplateMessages.DEFAULT_LANGUAGE));
                    defaultMessages = cached;
                }
            }
        }
        return cached;
    }

    /**
     * 递归包装为不可变 Map（字典结构为字符串与嵌套 Map，无其他集合类型）。
     *
     * @param source 待包装的字典
     * @return 深不可变副本
     */
    private static Map<String, Object> deepUnmodifiable(Map<String, Object> source) {
        Map<String, Object> copy = new HashMap<>(source.size());
        source.forEach((key, value) -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> nested = value instanceof Map ? (Map<String, Object>) value : null;
            copy.put(key, nested != null ? deepUnmodifiable(nested) : value);
        });
        return java.util.Collections.unmodifiableMap(copy);
    }

    /**
     * 渲染项目级模板
     *
     * @param templateName 模板名称（pom.xml, application.yml, Application.java, README.md）
     * @param context 模板上下文
     * @return 渲染结果
     * @throws IOException 模板渲染失败
     */
    public String renderProjectTemplate(String templateName, TemplateContext context) throws IOException {
        return render("project/" + templateName, context);
    }

    /**
     * 渲染贫血模型模板
     *
     * @param templateName 模板名称
     * @param context 模板上下文（需包含 table 元数据）
     * @return 渲染结果
     * @throws IOException 模板渲染失败
     */
    public String renderAnemicTemplate(String templateName, TemplateContext context) throws IOException {
        return render("anemic/" + templateName, context);
    }

    /**
     * 渲染充血模型模板
     *
     * @param templateName 模板名称
     * @param context 模板上下文（需包含 table 元数据）
     * @return 渲染结果
     * @throws IOException 模板渲染失败
     */
    public String renderRichTemplate(String templateName, TemplateContext context) throws IOException {
        return render("rich/" + templateName, context);
    }
}
