package com.gallop.fastddd.maven.plugin;

import com.gallop.fastddd.codegen.service.DatabaseMetadataService;
import com.gallop.fastddd.codegen.service.CodeGeneratorService;
import com.gallop.fastddd.codegen.service.TemplateProcessorService;
import com.gallop.fastddd.codegen.config.TemplateEngineConfig;
import com.gallop.fastddd.common.model.TableMetadata;
import com.gallop.fastddd.codegen.config.GeneratorConfig;
import com.gallop.fastddd.codegen.config.DatabaseConfig;
import com.gallop.fastddd.templates.TemplateMessages;
import com.gallop.fastddd.tooling.api.IdStrategy;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.sql.SQLException;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.ArrayList;

/**
 * FastDDD代码生成Maven插件
 * 
 * @author gallop
 * @since 2025-01-10 16:45:00
 */
@Mojo(name = "generate-code", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class CodeGenMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * 数据库连接URL
     */
    @Parameter(property = "codegen.url", required = true)
    private String url;

    /**
     * 数据库用户名
     */
    @Parameter(property = "codegen.username", required = true)
    private String username;

    /**
     * 数据库密码
     */
    @Parameter(property = "codegen.password", required = true)
    private String password;

    /**
     * 数据库驱动类名
     */
    @Parameter(property = "codegen.driverClassName")
    private String driverClassName;

    /**
     * 输出目录
     */
    @Parameter(property = "codegen.outputDir", defaultValue = "${project.build.directory}/generated-sources/fastddd")
    private String outputDir;

    /**
     * 基础包名
     */
    @Parameter(property = "codegen.basePackage", required = true)
    private String basePackage;

    /**
     * 表名列表（逗号分隔，为空则生成所有表）
     */
    @Parameter(property = "codegen.tables")
    private String tables;

    /**
     * 表名前缀（生成时会去掉此前缀）
     */
    @Parameter(property = "codegen.tablePrefix")
    private String tablePrefix;

    /**
     * 是否生成实体类
     */
    @Parameter(property = "codegen.generateEntity", defaultValue = "true")
    private boolean generateEntity;

    /**
     * 是否生成Repository接口
     */
    @Parameter(property = "codegen.generateRepository", defaultValue = "true")
    private boolean generateRepository;

    /**
     * 是否生成Service接口和实现
     */
    @Parameter(property = "codegen.generateService", defaultValue = "true")
    private boolean generateService;

    /**
     * 是否生成Controller
     */
    @Parameter(property = "codegen.generateController", defaultValue = "true")
    private boolean generateController;

    /**
     * 是否生成DTO类
     */
    @Parameter(property = "codegen.generateDto", defaultValue = "true")
    private boolean generateDto;

    /**
     * 是否生成Mapper接口（MyBatis）
     */
    @Parameter(property = "codegen.generateMapper", defaultValue = "false")
    private boolean generateMapper;

    /**
     * 是否覆盖已存在的文件
     */
    @Parameter(property = "codegen.overwrite", defaultValue = "false")
    private boolean overwrite;

    /**
     * 作者信息
     */
    @Parameter(property = "codegen.author", defaultValue = "FastDDD")
    private String author;

    /**
     * 模块名称
     */
    @Parameter(property = "codegen.moduleName")
    private String moduleName;

    /**
     * 是否使用Lombok注解
     */
    @Parameter(property = "codegen.useLombok", defaultValue = "true")
    private boolean useLombok;

    /**
     * 是否使用Swagger注解
     */
    @Parameter(property = "codegen.useSwagger", defaultValue = "true")
    private boolean useSwagger;

    /**
     * 是否使用JPA注解
     */
    @Parameter(property = "codegen.useJpa", defaultValue = "true")
    private boolean useJpa;

    /**
     * 是否使用验证注解
     */
    @Parameter(property = "codegen.useValidation", defaultValue = "true")
    private boolean useValidation;

    /**
     * 模型类型（ANEMIC 或 RICH）
     */
    @Parameter(property = "codegen.modelType", defaultValue = "ANEMIC")
    private String modelType;

    /**
     * 是否启用 CQRS
     */
    @Parameter(property = "codegen.enableCQRS", defaultValue = "false")
    private boolean enableCQRS;

    /**
     * 主键生成策略（AUTO 数据库自增 / ASSIGN_ID 雪花算法 / INPUT 手动输入 / NONE 不指定）。
     * 经生成器写入产物模型的 idStrategy 变量，并指导全局 mybatis-plus id-type 默认值（见 project/application.yml.ftl）。
     */
    @Parameter(property = "codegen.idStrategy", defaultValue = "AUTO")
    private String idStrategy;

    /**
     * 生成代码注释/文案的语言（L4 模板国际化）：zh（缺省，兼容既有生成结果）或 en；
     * 仅影响生成产物中的注释与说明文案，不改变代码结构。
     */
    @Parameter(property = "codegen.language", defaultValue = "zh")
    private String language;

    private DatabaseMetadataService databaseMetadataService;
    private CodeGeneratorService codeGeneratorService;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("开始执行FastDDD代码生成...");
        
        try {
            // 初始化服务
            initializeServices();
            
            // 验证参数
            validateParameters();
            
            // 创建输出目录
            createOutputDirectory();
            
            // 生成代码
            generateCode();
            
            getLog().info("FastDDD代码生成完成！");
            getLog().info("输出目录: " + outputDir);
            
        } catch (Exception e) {
            getLog().error("代码生成时发生错误: " + e.getMessage(), e);
            throw new MojoExecutionException("代码生成失败", e);
        }
    }

    /**
     * 初始化服务
     */
    private void initializeServices() {
        // 创建数据库配置
        DatabaseConfig databaseConfig = new DatabaseConfig();
        databaseConfig.setUrl(url);
        databaseConfig.setUsername(username);
        databaseConfig.setPassword(password);
        databaseConfig.setDriverClassName(driverClassName);
        databaseConfig.setTablePrefix(tablePrefix);
        
        this.databaseMetadataService = new DatabaseMetadataService(databaseConfig);
        // Mojo 非 Spring 托管，手动装配模板处理服务（修复历史 null TemplateProcessorService：
        // 旧实现 new CodeGeneratorService(metadataService, null, dbConfig) 触达模板渲染即 NPE，§16.24 G）
        try {
            TemplateEngineConfig templateEngineConfig = new TemplateEngineConfig("classpath:templates", language);
            TemplateProcessorService templateProcessorService = new TemplateProcessorService(templateEngineConfig);
            this.codeGeneratorService = new CodeGeneratorService(databaseMetadataService, templateProcessorService, databaseConfig);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("初始化 FreeMarker 模板引擎失败", e);
        }
    }

    /**
     * 验证参数
     */
    private void validateParameters() throws MojoExecutionException {
        // 语言参数校验（与 init-project 行为一致：非法值快速失败并给出可选值提示）
        try {
            TemplateMessages.normalize(language);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("codegen.language " + e.getMessage());
        }

        if (url == null || url.trim().isEmpty()) {
            throw new MojoExecutionException("数据库连接URL不能为空");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new MojoExecutionException("数据库用户名不能为空");
        }
        
        if (password == null) {
            throw new MojoExecutionException("数据库密码不能为空");
        }
        
        if (basePackage == null || basePackage.trim().isEmpty()) {
            throw new MojoExecutionException("基础包名不能为空");
        }
        
        // 自动检测驱动类名
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            driverClassName = detectDriverClassName(url);
        }
        
        // 设置默认模块名称
        if (moduleName == null || moduleName.trim().isEmpty()) {
            moduleName = project.getArtifactId();
        }
    }

    /**
     * 根据URL自动检测驱动类名
     */
    private String detectDriverClassName(String url) {
        if (url.startsWith("jdbc:mysql:")) {
            return "com.mysql.cj.jdbc.Driver";
        } else if (url.startsWith("jdbc:postgresql:")) {
            return "org.postgresql.Driver";
        } else if (url.startsWith("jdbc:oracle:")) {
            return "oracle.jdbc.OracleDriver";
        } else if (url.startsWith("jdbc:sqlserver:")) {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        } else if (url.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        } else {
            getLog().warn("无法自动检测数据库驱动，请手动指定 driverClassName 参数");
            return "com.mysql.cj.jdbc.Driver"; // 默认使用MySQL驱动
        }
    }

    /**
     * 创建输出目录
     */
    private void createOutputDirectory() throws MojoExecutionException {
        File outputDirectory = new File(outputDir);
        if (!outputDirectory.exists()) {
            boolean created = outputDirectory.mkdirs();
            if (!created) {
                throw new MojoExecutionException("无法创建输出目录: " + outputDir);
            }
        }
        
        getLog().info("输出目录: " + outputDirectory.getAbsolutePath());
    }

    /**
     * 生成代码
     */
    private void generateCode() throws SQLException, MojoExecutionException {
        getLog().info("连接数据库: " + url);
        
        // 获取表元数据
        List<TableMetadata> tableMetadataList;
        if (tables != null && !tables.trim().isEmpty()) {
            // 生成指定表
            List<String> tableNames = Arrays.asList(tables.split(","));
            tableMetadataList = new ArrayList<>();
            for (String tableName : tableNames) {
                tableName = tableName.trim();
                try {
                    TableMetadata tableMetadata = databaseMetadataService.getTable(tableName);
                    if (tableMetadata != null) {
                        tableMetadataList.add(tableMetadata);
                    } else {
                        getLog().warn("表 " + tableName + " 不存在");
                    }
                } catch (Exception e) {
                    getLog().warn("获取表 " + tableName + " 的元数据失败: " + e.getMessage());
                }
            }
        } else {
            // 生成所有表
            tableMetadataList = databaseMetadataService.getAllTables();
        }

        if (tableMetadataList.isEmpty()) {
            getLog().warn("没有找到任何表，跳过代码生成");
            return;
        }

        // 创建生成器配置
        GeneratorConfig config = createGeneratorConfig();

        // 为每个表生成代码
        for (TableMetadata tableMetadata : tableMetadataList) {
            try {
                generateCodeForTable(tableMetadata, config);
                getLog().info("成功生成表 " + tableMetadata.getTableName() + " 的代码");
            } catch (Exception e) {
                getLog().error("生成表 " + tableMetadata.getTableName() + " 的代码失败: " + e.getMessage(), e);
                throw new MojoExecutionException("代码生成失败", e);
            }
        }

        getLog().info("代码生成完成");
    }

    /**
     * 创建生成器配置
     */
    private GeneratorConfig createGeneratorConfig() {
        GeneratorConfig config = new GeneratorConfig();
        config.setBasePackage(basePackage);
        config.setOutputDir(outputDir);
        config.setAuthor(author);
        config.setModuleName(moduleName);
        config.setOverwrite(overwrite);

        // 设置生成选项
        config.setGenerateEntity(generateEntity);
        config.setGenerateRepository(generateRepository);
        config.setGenerateService(generateService);
        config.setGenerateController(generateController);
        config.setGenerateDto(generateDto);
        config.setGenerateMapper(generateMapper);

        // 设置注解选项
        config.setUseLombok(useLombok);
        config.setUseSwagger(useSwagger);
        config.setUseJpa(useJpa);
        config.setUseValidation(useValidation);

        // 设置模型类型
        if (modelType != null && !modelType.trim().isEmpty()) {
            try {
                config.setModelType(GeneratorConfig.ModelType.valueOf(modelType.toUpperCase()));
            } catch (IllegalArgumentException e) {
                getLog().warn("无效的 modelType: " + modelType + "，使用默认值 ANEMIC");
                config.setModelType(GeneratorConfig.ModelType.ANEMIC);
            }
        } else {
            config.setModelType(GeneratorConfig.ModelType.ANEMIC);
        }

        // 设置 CQRS
        config.setEnableCQRS(enableCQRS);

        // 设置主键策略（校验非法值，与 Studio UnifiedProjectService 行为一致：警告并回退 AUTO）
        if (idStrategy != null && !idStrategy.isBlank() && !IdStrategy.isValid(idStrategy)) {
            getLog().warn("无效的 idStrategy: " + idStrategy + "，使用默认值 AUTO");
        }
        config.setIdStrategy(IdStrategy.fromCode(idStrategy).getCode());

        return config;
    }

    /**
     * 为单个表生成代码
     */
    private void generateCodeForTable(TableMetadata tableMetadata, GeneratorConfig config) throws Exception {
        // 使用支持充血模型的 generateCode 方法
        Map<String, String> codeFiles = codeGeneratorService.generateCode(
            tableMetadata.getTableName(),
            config.getBasePackage(),
            config.getModuleName(),
            config
        );
        
        // 将生成的代码写入文件
        for (Map.Entry<String, String> entry : codeFiles.entrySet()) {
            String fileName = entry.getKey();
            String content = entry.getValue();
            
            File outputFile = new File(config.getOutputDir(), fileName);
            outputFile.getParentFile().mkdirs();
            
            try (java.io.FileWriter writer = new java.io.FileWriter(outputFile, java.nio.charset.StandardCharsets.UTF_8)) {
                writer.write(content);
            }
            
            getLog().info("Generated: " + outputFile.getAbsolutePath());
        }
    }
}
