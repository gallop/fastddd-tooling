package com.gallop.fastddd.doc;

import com.gallop.fastddd.doc.config.DocGeneratorProperties;
import com.gallop.fastddd.doc.generator.DocGenerator;
import com.gallop.fastddd.doc.model.ApiDoc;
import com.gallop.fastddd.doc.scanner.DocScanner;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * FastDoc门面类
 * 提供简单的API用于生成API文档
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Slf4j
@Component
public class FastDoc {

    private final DocScanner docScanner;
    private final DocGenerator docGenerator;
    private final DocGeneratorProperties properties;

    public FastDoc(DocScanner docScanner, DocGenerator docGenerator, DocGeneratorProperties properties) {
        this.docScanner = docScanner;
        this.docGenerator = docGenerator;
        this.properties = properties;
    }

    /**
     * 扫描并生成文档（使用配置的输出目录）
     *
     * @param basePackage 要扫描的基础包
     */
    public void generate(String basePackage) {
        generate(basePackage, properties.getOutputDir());
    }

    /**
     * 扫描并生成文档
     *
     * @param basePackage 要扫描的基础包
     * @param outputDir   输出目录
     */
    public void generate(String basePackage, String outputDir) {
        try {
            log.info("FastDoc: 开始扫描包 {} 并生成文档到 {}", basePackage, outputDir);

            // 扫描源代码
            ApiDoc apiDoc = docScanner.scan(basePackage);

            // 生成文档
            docGenerator.generate(apiDoc, outputDir);

            log.info("FastDoc: 文档生成成功");
        } catch (Exception e) {
            log.error("FastDoc: 文档生成失败", e);
            throw new RuntimeException("文档生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 扫描并生成文档（指定源码根目录）
     *
     * @param basePackage 要扫描的基础包
     * @param sourceRoot  源码根目录
     * @param outputDir   输出目录
     */
    public void generate(String basePackage, String sourceRoot, String outputDir) {
        try {
            log.info("FastDoc: 开始扫描包 {} (源码目录: {}) 并生成文档到 {}", basePackage, sourceRoot, outputDir);

            // 扫描源代码
            ApiDoc apiDoc = docScanner.scan(basePackage, sourceRoot);

            // 生成文档
            docGenerator.generate(apiDoc, outputDir);

            log.info("FastDoc: 文档生成成功");
        } catch (Exception e) {
            log.error("FastDoc: 文档生成失败", e);
            throw new RuntimeException("文档生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 仅扫描不生成文档（用于调试）
     *
     * @param basePackage 要扫描的基础包
     * @return 扫描得到的API文档数据
     */
    public ApiDoc scan(String basePackage) {
        return docScanner.scan(basePackage);
    }

    /**
     * 仅扫描不生成文档（用于调试，指定源码根目录）
     *
     * @param basePackage 要扫描的基础包
     * @param sourceRoot  源码根目录
     * @return 扫描得到的API文档数据
     */
    public ApiDoc scan(String basePackage, String sourceRoot) {
        return docScanner.scan(basePackage, sourceRoot);
    }
}
