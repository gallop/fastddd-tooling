package com.gallop.fastddd.doc.generator;

import com.gallop.fastddd.doc.config.DocGeneratorProperties;
import com.gallop.fastddd.doc.model.ApiController;
import com.gallop.fastddd.doc.model.ApiDoc;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * API文档生成器
 * 使用FreeMarker模板引擎生成API文档
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
public class DocGenerator {

    private final Configuration freemarkerConfig;
    private final DocGeneratorProperties properties;

    public DocGenerator(Configuration freemarkerConfig, DocGeneratorProperties properties) {
        this.freemarkerConfig = freemarkerConfig;
        this.properties = properties;
    }

    /**
     * 生成文档
     *
     * @param apiDoc API文档数据
     * @param outputDir 输出目录
     * @throws IOException     文件操作异常
     * @throws TemplateException 模板处理异常
     */
    public void generate(ApiDoc apiDoc, String outputDir) throws IOException, TemplateException {
        if (!properties.isEnabled()) {
            log.info("文档生成功能已禁用");
            return;
        }

        requireMarkdown();
        log.info("开始生成API文档，输出目录: {}", outputDir);

        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 设置项目信息
        apiDoc.setProjectName(properties.getProjectName());
        apiDoc.setProjectVersion(properties.getProjectVersion());
        apiDoc.setDescription(properties.getProjectDescription());

        if (properties.isSingleFile()) {
            // 生成单个文档文件
            generateSingleFile(apiDoc, outputPath);
        } else {
            // 为每个控制器生成单独的文档文件
            generateMultipleFiles(apiDoc, outputPath);
        }

        // 生成索引文件
        generateIndexFile(apiDoc, outputPath);

        log.info("API文档生成完成，共生成 {} 个控制器文档", apiDoc.getControllers().size());
    }

    /**
     * 生成单个文档文件
     */
    private void generateSingleFile(ApiDoc apiDoc, Path outputPath) throws IOException, TemplateException {
        String fileName = "API-Documentation.md";
        File outputFile = outputPath.resolve(fileName).toFile();

        // 加载模板
        Template template = freemarkerConfig.getTemplate("api-doc-all.md.ftl");

        // 准备数据模型
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("apiDoc", apiDoc);
        dataModel.put("controllers", apiDoc.getControllers());
        dataModel.put("generatedAt", apiDoc.getGeneratedAt());

        // 生成文档
        try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
            template.process(dataModel, writer);
        }

        log.info("生成文档: {}", outputFile.getAbsolutePath());
    }

    /**
     * 为每个控制器生成单独的文档文件
     */
    private void generateMultipleFiles(ApiDoc apiDoc, Path outputPath) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("api-doc.md.ftl");

        for (ApiController controller : apiDoc.getControllers()) {
            // 生成文件名：UserController.md
            String fileName = controller.getClassName() + ".md";
            File outputFile = outputPath.resolve(fileName).toFile();

            // 准备数据模型
            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("controller", controller);
            dataModel.put("generatedAt", apiDoc.getGeneratedAt());

            // 生成文档
            try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
                template.process(dataModel, writer);
            }

            log.info("生成文档: {}", outputFile.getAbsolutePath());
        }
    }

    /**
     * 生成索引文件
     */
    private void generateIndexFile(ApiDoc apiDoc, Path outputPath) throws IOException {
        StringBuilder indexContent = new StringBuilder();

        indexContent.append("# API Documentation Index\n\n");
        indexContent.append("**Project:** ").append(apiDoc.getProjectName()).append("\n\n");
        indexContent.append("**Version:** ").append(apiDoc.getProjectVersion()).append("\n\n");
        indexContent.append("**Generated:** ").append(apiDoc.getGeneratedAt()).append("\n\n");
        indexContent.append("**Total Controllers:** ").append(apiDoc.getControllers().size()).append("\n\n");
        indexContent.append("**Total Endpoints:** ").append(apiDoc.getTotalEndpoints()).append("\n\n");

        if (apiDoc.getDescription() != null && !apiDoc.getDescription().isEmpty()) {
            indexContent.append("> ").append(apiDoc.getDescription()).append("\n\n");
        }

        indexContent.append("---\n\n");
        indexContent.append("## Controllers\n\n");

        for (ApiController controller : apiDoc.getControllers()) {
            String fileName = properties.isSingleFile()
                    ? "API-Documentation.md" : controller.getClassName() + ".md";
            int endpointCount = controller.getMethods() != null ? controller.getMethods().size() : 0;

            indexContent.append("- [")
                    .append(controller.getClassName())
                    .append("](")
                    .append(fileName)
                    .append(")");

            if (controller.getDescription() != null && !controller.getDescription().isEmpty()) {
                indexContent.append(" - ").append(controller.getDescription());
            }

            indexContent.append(" (")
                    .append(endpointCount)
                    .append(" endpoints)")
                    .append("\n");
        }

        // 写入索引文件
        File indexFile = outputPath.resolve("README.md").toFile();
        try (FileWriter writer = new FileWriter(indexFile, StandardCharsets.UTF_8)) {
            writer.write(indexContent.toString());
        }

        log.info("生成索引文件: {}", indexFile.getAbsolutePath());
    }

    /**
     * 生成指定控制器的文档
     */
    public void generateController(ApiController controller, String outputDir) throws IOException, TemplateException {
        if (!properties.isEnabled()) {
            log.info("文档生成功能已禁用");
            return;
        }

        requireMarkdown();
        log.info("生成控制器文档: {}", controller.getClassName());

        // 创建输出目录
        Path outputPath = Paths.get(outputDir);
        Files.createDirectories(outputPath);

        // 加载模板
        Template template = freemarkerConfig.getTemplate("api-doc.md.ftl");

        // 准备数据模型
        Map<String, Object> dataModel = new HashMap<>();
        dataModel.put("controller", controller);
        dataModel.put("generatedAt", java.time.LocalDateTime.now());

        // 生成文件
        String fileName = controller.getClassName() + ".md";
        File outputFile = outputPath.resolve(fileName).toFile();

        try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
            template.process(dataModel, writer);
        }

        log.info("生成文档: {}", outputFile.getAbsolutePath());
    }

    private void requireMarkdown() {
        if (properties.getFormat() != DocGeneratorProperties.DocFormat.MARKDOWN) {
            throw new IllegalArgumentException("Only MARKDOWN output is supported; requested: " + properties.getFormat());
        }
    }
}
