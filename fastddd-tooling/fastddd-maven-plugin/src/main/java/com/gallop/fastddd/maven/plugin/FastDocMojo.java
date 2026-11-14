package com.gallop.fastddd.maven.plugin;

import com.gallop.fastddd.fastdoc.FastDocService;
import com.gallop.fastddd.fastdoc.FastDocTemplateService;
import com.gallop.fastddd.fastdoc.FastDocExportService;
import com.gallop.fastddd.fastdoc.model.DatabaseDocMetadata;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import javax.sql.DataSource;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * FastDoc数据库文档生成Maven插件
 * 
 * @author gallop
 * @since 2025-01-10 16:30:00
 */
@Mojo(name = "generate-doc", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class FastDocMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    /**
     * 数据库连接URL
     */
    @Parameter(property = "fastdoc.url", required = true)
    private String url;

    /**
     * 数据库用户名
     */
    @Parameter(property = "fastdoc.username", required = true)
    private String username;

    /**
     * 数据库密码
     */
    @Parameter(property = "fastdoc.password", required = true)
    private String password;

    /**
     * 数据库驱动类名
     */
    @Parameter(property = "fastdoc.driverClassName")
    private String driverClassName;

    /**
     * 输出目录
     */
    @Parameter(property = "fastdoc.outputDir", defaultValue = "${project.build.directory}/fastdoc")
    private String outputDir;

    /**
     * 输出格式：markdown, html, both
     */
    @Parameter(property = "fastdoc.format", defaultValue = "both")
    private String format;

    /**
     * 是否包含系统表
     */
    @Parameter(property = "fastdoc.includeSystemTables", defaultValue = "false")
    private boolean includeSystemTables;

    /**
     * 表名过滤模式（支持通配符）
     */
    @Parameter(property = "fastdoc.tablePattern")
    private String tablePattern;

    /**
     * 是否生成单独的表文档
     */
    @Parameter(property = "fastdoc.generateSeparateTableDocs", defaultValue = "true")
    private boolean generateSeparateTableDocs;

    /**
     * 文档标题
     */
    @Parameter(property = "fastdoc.title")
    private String title;

    /**
     * 文档描述
     */
    @Parameter(property = "fastdoc.description")
    private String description;

    /**
     * 作者信息
     */
    @Parameter(property = "fastdoc.author", defaultValue = "FastDDD")
    private String author;

    /**
     * 版本信息
     */
    @Parameter(property = "fastdoc.version")
    private String version;

    private FastDocService fastDocService;
    private FastDocTemplateService templateService;
    private FastDocExportService exportService;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("开始生成FastDoc数据库文档...");
        
        try {
            // 初始化服务
            initializeServices();
            
            // 验证参数
            validateParameters();
            
            // 创建输出目录
            createOutputDirectory();
            
            // 提取数据库元数据
            DatabaseDocMetadata databaseMetadata = fastDocService.extractDatabaseMetadata();
            
            getLog().info(String.format("数据库: %s, 表数量: %d, 总列数: %d", 
                databaseMetadata.getDatabaseName(), 
                databaseMetadata.getTableCount(), 
                databaseMetadata.getTotalColumnCount()));
            
            // 生成时间戳
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            
            // 根据格式生成文档
            if ("markdown".equalsIgnoreCase(format) || "md".equalsIgnoreCase(format)) {
                generateMarkdownDocs(databaseMetadata, timestamp);
            } else if ("html".equalsIgnoreCase(format)) {
                generateHtmlDocs(databaseMetadata, timestamp);
            } else {
                // 默认生成两种格式
                generateMarkdownDocs(databaseMetadata, timestamp);
                generateHtmlDocs(databaseMetadata, timestamp);
            }
            
            getLog().info("FastDoc数据库文档生成完成！");
            getLog().info("输出目录: " + outputDir);
            
        } catch (Exception e) {
            getLog().error("生成FastDoc文档时发生错误: " + e.getMessage(), e);
            throw new MojoExecutionException("FastDoc文档生成失败", e);
        }
    }

    /**
     * 初始化服务
     */
    private void initializeServices() {
        // 创建数据源
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName(driverClassName);
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);
        
        DataSource dataSource = new HikariDataSource(config);
        
        // 初始化服务
        this.fastDocService = new FastDocService(dataSource);
        this.templateService = new FastDocTemplateService();
        this.exportService = new FastDocExportService(fastDocService, templateService);
    }

    /**
     * 验证参数
     */
    private void validateParameters() throws MojoExecutionException {
        if (url == null || url.trim().isEmpty()) {
            throw new MojoExecutionException("数据库连接URL不能为空");
        }
        
        if (username == null || username.trim().isEmpty()) {
            throw new MojoExecutionException("数据库用户名不能为空");
        }
        
        if (password == null) {
            throw new MojoExecutionException("数据库密码不能为空");
        }
        
        if (format != null && !format.matches("(?i)(markdown|html|both)")) {
            throw new MojoExecutionException("不支持的输出格式: " + format + "，支持的格式: markdown, html, both");
        }
        
        // 自动检测驱动类名
        if (driverClassName == null || driverClassName.trim().isEmpty()) {
            driverClassName = detectDriverClassName(url);
        }
        
        // 设置默认标题
        if (title == null || title.trim().isEmpty()) {
            title = project.getName() + " 数据库文档";
        }
        
        // 设置默认版本
        if (version == null || version.trim().isEmpty()) {
            version = project.getVersion();
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
     * 生成Markdown文档
     */
    private void generateMarkdownDocs(DatabaseDocMetadata databaseMetadata, String timestamp) throws Exception {
        getLog().info("生成Markdown格式文档...");
        
        // 生成数据库总览文档
        String databaseDoc = templateService.generateMarkdownDoc(databaseMetadata);
        String databaseFileName = String.format("database_%s_%s.md", 
            databaseMetadata.getDatabaseName(), timestamp);
        
        exportService.saveToFile(databaseDoc, outputDir, databaseFileName);
        getLog().info("已生成数据库文档: " + databaseFileName);
        
        // 生成单独的表文档
        if (generateSeparateTableDocs) {
            databaseMetadata.getTables().forEach(table -> {
                try {
                    String tableDoc = templateService.generateTableMarkdownDoc(table);
                    String tableFileName = String.format("table_%s_%s.md", 
                        table.getTableName(), timestamp);
                    
                    exportService.saveToFile(tableDoc, outputDir, tableFileName);
                    getLog().debug("已生成表文档: " + tableFileName);
                } catch (Exception e) {
                    getLog().warn("生成表 " + table.getTableName() + " 的文档时发生错误: " + e.getMessage());
                }
            });
        }
    }

    /**
     * 生成HTML文档
     */
    private void generateHtmlDocs(DatabaseDocMetadata databaseMetadata, String timestamp) throws Exception {
        getLog().info("生成HTML格式文档...");
        
        // 生成数据库总览文档
        String databaseDoc = templateService.generateHtmlDoc(databaseMetadata);
        String databaseFileName = String.format("database_%s_%s.html", 
            databaseMetadata.getDatabaseName(), timestamp);
        
        exportService.saveToFile(databaseDoc, outputDir, databaseFileName);
        getLog().info("已生成数据库HTML文档: " + databaseFileName);
        
        // 生成单独的表文档
        if (generateSeparateTableDocs) {
            databaseMetadata.getTables().forEach(table -> {
                try {
                    String tableDoc = templateService.generateTableHtmlDoc(table);
                    String tableFileName = String.format("table_%s_%s.html", 
                        table.getTableName(), timestamp);
                    
                    exportService.saveToFile(tableDoc, outputDir, tableFileName);
                    getLog().debug("已生成表HTML文档: " + tableFileName);
                } catch (Exception e) {
                    getLog().warn("生成表 " + table.getTableName() + " 的HTML文档时发生错误: " + e.getMessage());
                }
            });
        }
    }
}