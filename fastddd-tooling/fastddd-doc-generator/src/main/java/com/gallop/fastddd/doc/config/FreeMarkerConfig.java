package com.gallop.fastddd.doc.config;

import com.gallop.fastddd.templates.TemplateMessages;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.nio.charset.StandardCharsets;

/**
 * FreeMarker配置类
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
public class FreeMarkerConfig {

    /**
     * 生成文档的语言（L4 模板国际化）：zh（缺省）或 en。
     * <p>Spring 托管场景经 {@code fastdoc.language} 属性绑定（与 doc-maven-plugin 的
     * {@code -Dfastdoc.language} 同名）；非 Spring 场景经 {@link #setLanguage(String)} 注入。
     */
    @Value("${fastdoc.language:zh}")
    private String language = TemplateMessages.DEFAULT_LANGUAGE;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    /**
     * 配置FreeMarker
     */
    @Bean
    public Configuration freemarkerConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);

        // 设置模板加载路径（从classpath加载）
        cfg.setClassForTemplateLoading(this.getClass(), "/templates");

        // 设置默认编码
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());

        // 设置模板异常处理器
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);

        // 禁止自动转义（我们输出的是Markdown，不需要HTML转义）
        cfg.setAutoEscapingPolicy(Configuration.DISABLE_AUTO_ESCAPING_POLICY);

        // 设置对象包装器（使用默认包装器）
        cfg.setObjectWrapper(new freemarker.template.DefaultObjectWrapper(Configuration.VERSION_2_3_32));

        // 注入模板文案字典（L4）：api-doc 模板内以 ${msg.doc.xxx} 引用
        TemplateMessages.apply(cfg, language);

        return cfg;
    }
}
