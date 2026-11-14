package com.gallop.fastddd.doc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * API文档生成器配置属性
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Data
@Component
@ConfigurationProperties(prefix = "fastddd.doc")
public class DocGeneratorProperties {

    /**
     * 是否启用文档生成
     */
    private boolean enabled = true;

    /**
     * 输出目录（相对于项目根目录）
     */
    private String outputDir = "docs/api";

    /**
     * 是否生成单个文件还是每个控制器一个文件
     */
    private boolean singleFile = false;

    /**
     * 项目名称
     */
    private String projectName = "FastDDD API";

    /**
     * 项目版本
     */
    private String projectVersion = "1.0.0";

    /**
     * 项目描述
     */
    private String projectDescription = "";

    /**
     * 模板路径（可选，默认使用内置模板）
     */
    private String templatePath = "";

    /**
     * 是否包含示例数据
     */
    private boolean includeExamples = true;

    /**
     * 文档格式（markdown, html, json）
     */
    private DocFormat format = DocFormat.MARKDOWN;

    /**
     * 文档格式枚举
     */
    public enum DocFormat {
        MARKDOWN,
        HTML,
        JSON
    }
}
