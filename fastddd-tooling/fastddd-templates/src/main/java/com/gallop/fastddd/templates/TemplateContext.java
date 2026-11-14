package com.gallop.fastddd.templates;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 统一的模板上下文
 * <p>
 * 用于规范所有模板生成工具（Code Generator、Web Initializr、Maven Plugin）的模板变量
 *
 * @author FastDDD Generator
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateContext {

    // ==================== 项目基本信息 ====================

    /**
     * Group ID（Maven 坐标）
     * <p>示例：com.example
     */
    private String groupId;

    /**
     * Artifact ID（Maven 坐标）
     * <p>示例：my-fastddd-app
     */
    private String artifactId;

    /**
     * 项目版本
     * <p>示例：1.0.0-SNAPSHOT
     */
    private String version;

    /**
     * 基础包名
     * <p>示例：com.example.app
     */
    private String packageName;

    /**
     * 项目名称
     * <p>示例：My FastDDD Application
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDescription;

    // ==================== 模型类型配置 ====================

    /**
     * 领域模型类型
     */
    private ModelType modelType;

    /**
     * 主键生成策略（AUTO/ASSIGN_ID/INPUT/NONE）
     * <p>项目级配置：渲染到生成项目的 mybatis-plus global-config db-config id-type 默认值
     * （见 project/application.yml.ftl），与所选 ID 策略保持一致。每实体不生成 @TableId（BasePO 继承 id）。
     */
    private String idStrategy;

    /**
     * 数据库类型
     */
    private DatabaseType databaseType;

    /**
     * 数据源URL
     */
    private String datasourceUrl;

    /**
     * 数据源用户名
     */
    private String datasourceUsername;

    /**
     * 数据源密码
     */
    private String datasourcePassword;

    /** 生成项目的连接池：HIKARI（默认）或 DRUID。 */
    @Builder.Default
    private String connectionPool = "HIKARI";

    /**
     * 数据库名称（从URL中提取）
     */
    private String databaseName;

    /**
     * 选中的模块列表
     */
    private Set<String> modules;

    /**
     * 是否启用 CQRS（充血模型专用）
     */
    private boolean enableCQRS;

    // ==================== 包路径（根据模型类型自动计算）====================

    /**
     * 领域层包名（RICH 模型）
     * <p>示例：com.example.app.domain
     */
    private String domainPackage;

    /**
     * 应用层包名（RICH 模型）
     * <p>示例：com.example.app.application
     */
    private String applicationPackage;

    /**
     * 基础设施层包名（RICH 模型）
     * <p>示例：com.example.app.infrastructure.persistence
     */
    private String infrastructurePackage;

    /**
     * 接口层包名（RICH 模型）
     * <p>示例：com.example.app.interfaces
     */
    private String interfacesPackage;

    /**
     * 实体层包名（ANEMIC 模型）
     * <p>示例：com.example.app.entity
     */
    private String entityPackage;

    /**
     * 服务层包名（ANEMIC 模型）
     * <p>示例：com.example.app.service
     */
    private String servicePackage;

    /**
     * 控制器层包名（ANEMIC 模型）
     * <p>示例：com.example.app.controller
     */
    private String controllerPackage;

    /**
     * Mapper 层包名（ANEMIC 模型）
     * <p>示例：com.example.app.mapper
     */
    private String mapperPackage;

    // ==================== 版本信息 ====================

    /**
     * 目标 Framework 版本（生成项目依赖的 io.github.gallop.fastddd:* 制品版本）
     * <p>示例：1.0.0-jdk21-SNAPSHOT
     * <p>与 {@link #fastdddToolingVersion} 分属两条独立版本轴：Framework 版本不推导
     * Tooling 插件版本，反之亦然（ECO-003 版本解耦在生成通道的落地）。
     */
    private String fastdddVersion;

    /**
     * 目标 Tooling 版本（生成项目引用的 fastddd-maven-plugin 版本）
     * <p>示例：1.0.0-jdk21-SNAPSHOT
     * <p>未设置时回落到与 {@link #fastdddVersion} 相同的字面量基线；生成通道
     * （Studio / Maven Plugin / Archetype）应显式注入经认证的 Tooling 版本。
     */
    private String fastdddToolingVersion;

    /**
     * Java 版本
     * <p>示例：17
     */
    private String javaVersion;

    /**
     * Spring Boot 版本
     * <p>示例：3.2.0
     */
    private String springBootVersion;

    // ==================== 元信息 ====================

    /**
     * 作者
     * <p>示例：FastDDD Generator
     */
    private String author;

    /**
     * 日期
     * <p>格式：yyyy-MM-dd
     */
    private String date;

    /**
     * 当前时间戳
     */
    private long timestamp;

    // ==================== 扩展属性 ====================

    /**
     * 自定义属性
     * <p>用于扩展模板变量
     */
    private Map<String, Object> customProperties;

    // ==================== 枚举定义 ====================

    /**
     * 领域模型类型
     *
     * @deprecated SPLIT-003：跨产品的模型类型规范定义已上移至 Tooling 稳定契约
     * {@code com.gallop.fastddd.tooling.api.ModelType}（API 为唯一规范来源）。本嵌套枚举保留供
     * 模板上下文向后兼容，消费方应改用 API 枚举；完整消费面迁移见 SPLIT-004。
     */
    @Deprecated
    public enum ModelType {
        /**
         * 贫血模型（Anemic Domain Model）
         * <p>特点：PO + Service + Controller，业务逻辑在 Service 层
         */
        ANEMIC("贫血模型", "Anemic Domain Model"),

        /**
         * 充血模型（Rich Domain Model）
         * <p>特点：AggregateRoot + CQRS，业务逻辑在领域层
         */
        RICH("充血模型", "Rich Domain Model");

        private final String displayName;
        private final String description;

        ModelType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 数据库类型
     */
    public enum DatabaseType {
        /**
         * MySQL 数据库
         */
        MYSQL("MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://localhost:3306/", "mysql-connector-j"),

        /**
         * PostgreSQL 数据库
         */
        POSTGRESQL("PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://localhost:5432/", "postgresql"),

        /**
         * H2 内存数据库
         */
        H2("H2", "org.h2.Driver", "jdbc:h2:mem:", "h2");

        private final String displayName;
        private final String driverClassName;
        private final String urlPrefix;
        private final String artifactId;

        DatabaseType(String displayName, String driverClassName, String urlPrefix, String artifactId) {
            this.displayName = displayName;
            this.driverClassName = driverClassName;
            this.urlPrefix = urlPrefix;
            this.artifactId = artifactId;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDriverClassName() {
            return driverClassName;
        }

        public String getUrlPrefix() {
            return urlPrefix;
        }

        public String getArtifactId() {
            return artifactId;
        }
    }

    // ==================== 工厂方法 ====================

    /**
     * 创建默认的模板上下文
     */
    public static TemplateContext createDefault() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return TemplateContext.builder()
            .groupId("com.example")
            .artifactId("my-fastddd-app")
            .version("1.0.0-SNAPSHOT")
            .packageName("com.example.app")
            .projectName("My FastDDD Application")
            .projectDescription("FastDDD Application - ANEMIC Model")
            .modelType(ModelType.ANEMIC)
            .idStrategy("ASSIGN_ID")
            .databaseType(DatabaseType.MYSQL)
            .modules(new HashSet<>())
            .enableCQRS(false)
            .fastdddVersion("1.0.0-jdk21-SNAPSHOT")
            .fastdddToolingVersion("1.0.0-jdk21-SNAPSHOT")
            .javaVersion("21")
            .springBootVersion("3.5.16")
            .author("FastDDD Generator")
            .date(today)
            .timestamp(System.currentTimeMillis())
            .customProperties(new HashMap<>())
            .build();
    }

    /**
     * 根据模型类型计算包路径
     * <p>调用此方法后，domainPackage、applicationPackage 等字段会自动填充
     */
    public TemplateContext calculatePackages() {
        String basePackage = this.packageName;

        if (this.modelType == ModelType.RICH) {
            // 充血模型包结构
            this.domainPackage = basePackage + ".domain";
            this.applicationPackage = basePackage + ".application";
            this.infrastructurePackage = basePackage + ".infrastructure.persistence";
            this.interfacesPackage = basePackage + ".interfaces";
            // 充血模型的实体在基础设施层
            this.entityPackage = basePackage + ".infrastructure.persistence";
        } else {
            // 贫血模型包结构
            this.entityPackage = basePackage + ".entity";
            this.servicePackage = basePackage + ".service";
            this.controllerPackage = basePackage + ".controller";
            this.mapperPackage = basePackage + ".mapper";
        }

        return this;
    }

    /**
     * 添加自定义属性
     */
    public TemplateContext addCustomProperty(String key, Object value) {
        if (this.customProperties == null) {
            this.customProperties = new HashMap<>();
        }
        this.customProperties.put(key, value);
        return this;
    }

    /**
     * 获取自定义属性
     */
    public Object getCustomProperty(String key) {
        return customProperties != null ? customProperties.get(key) : null;
    }

    /**
     * 获取自定义属性（带默认值）
     */
    @SuppressWarnings("unchecked")
    public <T> T getCustomProperty(String key, T defaultValue) {
        Object value = getCustomProperty(key);
        return value != null ? (T) value : defaultValue;
    }

    /**
     * 转换为 FreeMarker 模板变量 Map
     * <p>这是给 FreeMarker Template 使用的标准数据结构
     */
    public Map<String, Object> toTemplateVariables() {
        Map<String, Object> variables = new HashMap<>();

        // 基本信息
        variables.put("groupId", groupId);
        variables.put("artifactId", artifactId);
        variables.put("version", version);
        variables.put("packageName", packageName);
        variables.put("projectName", projectName != null ? projectName : artifactId);
        variables.put("projectDescription", projectDescription);
        variables.put("className", toPascalCase(artifactId != null ? artifactId : "App"));

        // 配置
        variables.put("modelType", modelType != null ? modelType.name() : "ANEMIC");
        variables.put("modelTypeDisplayName", modelType != null ? modelType.getDisplayName() : "贫血模型");
        variables.put("idStrategy", idStrategy != null ? idStrategy : "ASSIGN_ID");
        variables.put("databaseType", databaseType != null ? databaseType.name() : "MYSQL");
        variables.put("databaseDisplayName", databaseType != null ? databaseType.getDisplayName() : "MySQL");
        variables.put("enableCQRS", enableCQRS);
        variables.put("modules", modules != null ? modules : new HashSet<>());

        // 数据源配置
        variables.put("datasourceUrl", datasourceUrl);
        variables.put("datasourceUsername", datasourceUsername != null ? datasourceUsername : "root");
        variables.put("datasourcePassword", datasourcePassword);
        variables.put("connectionPool", connectionPool != null ? connectionPool : "HIKARI");
        variables.put("databaseName", databaseName != null ? databaseName : artifactId);

        // 包路径
        variables.put("domainPackage", domainPackage);
        variables.put("applicationPackage", applicationPackage);
        variables.put("infrastructurePackage", infrastructurePackage);
        variables.put("interfacesPackage", interfacesPackage);
        variables.put("entityPackage", entityPackage);
        variables.put("servicePackage", servicePackage);
        variables.put("controllerPackage", controllerPackage);
        variables.put("mapperPackage", mapperPackage);

        // 版本信息（Framework 与 Tooling 两条独立版本轴，见字段级 javadoc）
        variables.put("fastdddVersion", fastdddVersion != null ? fastdddVersion : "1.0.0-jdk21-SNAPSHOT");
        variables.put("fastdddToolingVersion",
                fastdddToolingVersion != null ? fastdddToolingVersion : "1.0.0-jdk21-SNAPSHOT");
        variables.put("javaVersion", javaVersion != null ? javaVersion : "21");
        variables.put("springBootVersion", springBootVersion != null ? springBootVersion : "3.5.16");

        // 元信息
        variables.put("author", author != null ? author : "FastDDD Generator");
        String dateValue = date != null ? date : LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        variables.put("date", dateValue);
        variables.put("year", dateValue.substring(0, 4)); // Extract year from yyyy-MM-dd
        variables.put("timestamp", timestamp > 0 ? timestamp : System.currentTimeMillis());

        // 数据库驱动信息
        if (databaseType != null) {
            variables.put("databaseDriver", databaseType.getDriverClassName());
            variables.put("databaseUrlPrefix", databaseType.getUrlPrefix());
            variables.put("databaseArtifactId", databaseType.getArtifactId());
        }

        // 自定义属性
        if (customProperties != null) {
            variables.putAll(customProperties);
        }

        return variables;
    }

    /**
     * 创建包路径格式（用于目录结构）
     * <p>示例：com.example.app -> com/example/app
     */
    public String getPackageInPathFormat(String packageName) {
        return packageName != null ? packageName.replace('.', '/') : "";
    }

    /**
     * 获取实体名称的 Pascal 格式
     * <p>示例：user_info -> UserInfo, test-app -> TestApp
     */
    public String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // 先将连字符替换为下划线，然后统一处理
        String normalized = input.replace("-", "_");
        String[] parts = normalized.split("_");
        StringBuilder result = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                result.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) {
                    result.append(part.substring(1).toLowerCase());
                }
            }
        }

        return result.toString();
    }

    /**
     * 获取实体名称的 camel 格式
     * <p>示例：user_info -> userInfo
     */
    public String toCamelCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String pascal = toPascalCase(input);
        if (pascal.isEmpty()) {
            return pascal;
        }

        return Character.toLowerCase(pascal.charAt(0)) + pascal.substring(1);
    }

    @Override
    public String toString() {
        return "TemplateContext{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", packageName='" + packageName + '\'' +
                ", modelType=" + modelType +
                ", databaseType=" + databaseType +
                ", fastdddVersion='" + fastdddVersion + '\'' +
                ", fastdddToolingVersion='" + fastdddToolingVersion + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                '}';
    }
}
