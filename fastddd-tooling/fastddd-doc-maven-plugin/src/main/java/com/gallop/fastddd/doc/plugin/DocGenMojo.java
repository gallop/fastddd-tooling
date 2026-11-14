package com.gallop.fastddd.doc.plugin;

import com.gallop.fastddd.doc.FastDoc;
import com.gallop.fastddd.doc.config.DocGeneratorProperties;
import com.gallop.fastddd.templates.TemplateMessages;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * FastDoc Maven插件Mojo
 * 用于在Maven构建过程中生成API文档
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Mojo(name = "generate", defaultPhase = LifecyclePhase.PROCESS_CLASSES)
public class DocGenMojo extends AbstractMojo {

    private static final Logger log = LoggerFactory.getLogger(DocGenMojo.class);

    /**
     * 要扫描的基础包路径
     * 例如：com.gallop.fastddd.controller
     */
    @Parameter(property = "scanPackage", required = true)
    private String scanPackage;

    /**
     * 源代码根目录
     * 默认：${project.basedir}/src/main/java
     */
    @Parameter(property = "sourceRoot", defaultValue = "${project.basedir}/src/main/java")
    private File sourceRoot;

    /**
     * 输出目录
     * 默认：${project.basedir}/docs/api
     */
    @Parameter(property = "outputDir", defaultValue = "${project.basedir}/docs/api")
    private File outputDir;

    /**
     * 项目名称
     */
    @Parameter(property = "projectName", defaultValue = "${project.name}")
    private String projectName;

    /**
     * 项目版本
     */
    @Parameter(property = "projectVersion", defaultValue = "${project.version}")
    private String projectVersion;

    /**
     * 项目描述
     */
    @Parameter(property = "projectDescription", defaultValue = "${project.description}")
    private String projectDescription;

    /**
     * 是否生成单个文件
     * 默认：false（每个控制器一个文件）
     */
    @Parameter(property = "singleFile", defaultValue = "false")
    private boolean singleFile;

    /**
     * 是否包含示例数据
     */
    @Parameter(property = "includeExamples", defaultValue = "true")
    private boolean includeExamples;

    /**
     * 文档格式
     * 默认：MARKDOWN
     */
    @Parameter(property = "format", defaultValue = "MARKDOWN")
    private DocGeneratorProperties.DocFormat format;

    /**
     * 生成文档的语言（L4 模板国际化）：zh（缺省，兼容既有生成结果）或 en。
     */
    @Parameter(property = "fastdoc.language", defaultValue = "zh")
    private String language;

    /**
     * 是否跳过文档生成
     */
    @Parameter(property = "skip", defaultValue = "false")
    private boolean skip;

    /**
     * 是否在失败时跳过
     */
    @Parameter(property = "failOnError", defaultValue = "true")
    private boolean failOnError;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            log.info("FastDoc: 文档生成已跳过（skip=true）");
            return;
        }

        try {
            log.info("FastDoc Maven Plugin - 开始生成API文档");
            log.info("扫描包: {}", scanPackage);
            log.info("源码目录: {}", sourceRoot.getAbsolutePath());
            log.info("输出目录: {}", outputDir.getAbsolutePath());
            log.info("项目名称: {}", projectName);
            log.info("项目版本: {}", projectVersion);

            // 创建并配置FastDoc服务
            FastDocService fastDocService = new FastDocService(language);
            fastDocService.setScanPackage(scanPackage);
            fastDocService.setSourceRoot(sourceRoot.getAbsolutePath());
            fastDocService.setOutputDir(outputDir.getAbsolutePath());
            fastDocService.setProjectName(projectName);
            fastDocService.setProjectVersion(projectVersion);
            fastDocService.setProjectDescription(projectDescription);
            fastDocService.setSingleFile(singleFile);
            fastDocService.setIncludeExamples(includeExamples);
            fastDocService.setFormat(format);

            // 执行文档生成
            fastDocService.generate();

            log.info("FastDoc: API文档生成成功！");
            log.info("输出目录: {}", outputDir.getAbsolutePath());

        } catch (Exception e) {
            log.error("FastDoc: API文档生成失败", e);
            if (failOnError) {
                throw new MojoExecutionException("API文档生成失败: " + e.getMessage(), e);
            } else {
                log.warn("FastDoc: 文档生成失败，但failOnError=false，继续构建");
            }
        }
    }

    /**
     * FastDoc服务封装类
     * 在Maven插件环境中直接使用doc-generator组件
     */
    private static class FastDocService {
        private String scanPackage;
        private String sourceRoot = "src/main/java";
        private String outputDir = "docs/api";
        private String projectName = "API";
        private String projectVersion = "1.0.0";
        private String projectDescription = "";
        private boolean singleFile = false;
        private boolean includeExamples = true;
        private DocGeneratorProperties.DocFormat format = DocGeneratorProperties.DocFormat.MARKDOWN;

        // 直接使用doc-generator的核心组件
        private final com.gallop.fastddd.doc.scanner.JavaDocScanner docScanner;
        private final com.gallop.fastddd.doc.generator.DocGenerator docGenerator;
        private final com.gallop.fastddd.doc.config.DocGeneratorProperties properties;

        /**
         * @param language 文档文案语言（zh/en，经 {@link TemplateMessages} 注入模板共享变量）
         */
        public FastDocService(String language) {
            // 初始化组件
            com.gallop.fastddd.doc.parser.DescriptionExtractor descriptionExtractor =
                    new com.gallop.fastddd.doc.parser.DescriptionExtractor(
                            new com.gallop.fastddd.doc.parser.JavadocParser());

            com.gallop.fastddd.doc.parser.AnnotationParser annotationParser =
                    new com.gallop.fastddd.doc.parser.AnnotationParser(
                            new com.gallop.fastddd.doc.parser.TypeExtractor(),
                            descriptionExtractor);

            this.docScanner = new com.gallop.fastddd.doc.scanner.JavaDocScanner(
                    descriptionExtractor,
                    annotationParser,
                    new com.gallop.fastddd.doc.parser.TypeExtractor());

            freemarker.template.Configuration freemarkerConfig = new freemarker.template.Configuration(freemarker.template.Configuration.VERSION_2_3_32);
            freemarkerConfig.setClassForTemplateLoading(this.getClass(), "/templates");
            freemarkerConfig.setDefaultEncoding("UTF-8");
            freemarkerConfig.setTemplateExceptionHandler(freemarker.template.TemplateExceptionHandler.RETHROW_HANDLER);
            freemarkerConfig.setAutoEscapingPolicy(freemarker.template.Configuration.DISABLE_AUTO_ESCAPING_POLICY);
            TemplateMessages.apply(freemarkerConfig, language);

            this.properties = new com.gallop.fastddd.doc.config.DocGeneratorProperties();
            this.docGenerator = new com.gallop.fastddd.doc.generator.DocGenerator(freemarkerConfig, properties);
        }

        public void generate() throws Exception {
            // 配置属性
            properties.setEnabled(true);
            properties.setOutputDir(outputDir);
            properties.setSingleFile(singleFile);
            properties.setProjectName(projectName);
            properties.setProjectVersion(projectVersion);
            properties.setProjectDescription(projectDescription);
            properties.setIncludeExamples(includeExamples);
            properties.setFormat(format);

            // 扫描并生成
            com.gallop.fastddd.doc.model.ApiDoc apiDoc = docScanner.scan(scanPackage, sourceRoot);
            docGenerator.generate(apiDoc, outputDir);
        }

        public void setScanPackage(String scanPackage) {
            this.scanPackage = scanPackage;
        }

        public void setSourceRoot(String sourceRoot) {
            this.sourceRoot = sourceRoot;
        }

        public void setOutputDir(String outputDir) {
            this.outputDir = outputDir;
        }

        public void setProjectName(String projectName) {
            this.projectName = projectName;
        }

        public void setProjectVersion(String projectVersion) {
            this.projectVersion = projectVersion;
        }

        public void setProjectDescription(String projectDescription) {
            this.projectDescription = projectDescription;
        }

        public void setSingleFile(boolean singleFile) {
            this.singleFile = singleFile;
        }

        public void setIncludeExamples(boolean includeExamples) {
            this.includeExamples = includeExamples;
        }

        public void setFormat(DocGeneratorProperties.DocFormat format) {
            this.format = format;
        }
    }
}
