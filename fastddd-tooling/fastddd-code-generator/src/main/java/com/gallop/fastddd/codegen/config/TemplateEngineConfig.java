package com.gallop.fastddd.codegen.config;

import com.gallop.fastddd.templates.TemplateMessages;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * FreeMarker模板引擎配置
 *
 * @author gallop
 * @date 2023-10-10
 */
@Slf4j
@Component
public class TemplateEngineConfig {

    @Value("${fastddd.codegen.template-path:classpath:templates}")
    private String templatePath;

    /**
     * 生成代码的语言（L4 模板国际化）：zh（缺省，兼容既有生成结果）或 en。
     * <p>文案经 {@link TemplateMessages} 注入 FreeMarker 共享变量 {@code msg}。
     */
    @Value("${fastddd.codegen.language:zh}")
    private String language = TemplateMessages.DEFAULT_LANGUAGE;

    @Getter
    private Configuration configuration;

    /**
     * Spring 使用的默认构造器（字段由 {@code @Value} 注入，{@link #freemarkerConfiguration()} 由容器以 {@code @Bean} 触发）。
     */
    public TemplateEngineConfig() {
    }

    /**
     * 非 Spring 场景（如 Maven 插件 {@code CodeGenMojo}）使用的独立构造器：
     * 直接给定模板路径并完成 FreeMarker {@link Configuration} 初始化（语言缺省 zh）。
     *
     * @param templatePath 模板路径，前缀 {@code classpath:} 表示类路径加载，否则按目录加载
     * @throws IOException 模板加载失败
     */
    public TemplateEngineConfig(String templatePath) throws IOException {
        this(templatePath, TemplateMessages.DEFAULT_LANGUAGE);
    }

    /**
     * 非 Spring 场景（如 Maven 插件 {@code CodeGenMojo}）使用的独立构造器：
     * 直接给定模板路径与生成语言，并完成 FreeMarker {@link Configuration} 初始化。
     *
     * @param templatePath 模板路径，前缀 {@code classpath:} 表示类路径加载，否则按目录加载
     * @param language     生成语言（zh/en，非法值快速失败）
     * @throws IOException 模板加载失败
     */
    public TemplateEngineConfig(String templatePath, String language) throws IOException {
        this.templatePath = templatePath;
        this.language = language;
        this.configuration = freemarkerConfiguration();
    }

    @Bean
    @org.springframework.context.annotation.Primary
    public Configuration freemarkerConfiguration() throws IOException {
        log.info("初始化FreeMarker模板引擎，模板路径: {}", templatePath);
        
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        
        // 设置模板加载路径
        if (templatePath.startsWith("classpath:")) {
            String path = templatePath.substring("classpath:".length());
            cfg.setClassLoaderForTemplateLoading(getClass().getClassLoader(), path);
        } else {
            cfg.setDirectoryForTemplateLoading(new File(templatePath));
        }
        
        // 设置默认编码
        cfg.setDefaultEncoding("UTF-8");
        
        // 设置异常处理器
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        
        // 不使用本地化查找
        cfg.setLocalizedLookup(false);

        // 注入模板文案字典（L4）：模板内以 ${msg.xxx.yyy} 引用，语言由 fastddd.codegen.language 决定
        TemplateMessages.apply(cfg, language);

        this.configuration = cfg;
        return cfg;
    }
}