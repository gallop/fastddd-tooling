package com.gallop.fastddd.maven.plugin;

import com.gallop.fastddd.templates.TemplateContext;
import com.gallop.fastddd.templates.TemplateContext.DatabaseType;
import com.gallop.fastddd.templates.TemplateContext.ModelType;
import com.gallop.fastddd.templates.TemplateMessages;
import com.gallop.fastddd.templates.TemplateRenderer;
import com.gallop.fastddd.tooling.api.compat.CompatibilityMatrix;
import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * FastDDD 项目初始化 Maven 插件
 * <p>
 * 使用 FreeMarker 模板引擎生成完整的项目脚手架，
 * 与 Studio Initializr 共享同一套模板，产出 ~25 个文件。
 *
 * @author gallop
 * @since 2025-01-24
 */
@Mojo(name = "init-project", defaultPhase = LifecyclePhase.INITIALIZE, requiresProject = false)
public class InitProjectMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Parameter(property = "init.groupId", defaultValue = "com.gallop.project")
    private String groupId;

    @Parameter(property = "init.artifactId", defaultValue = "my-app")
    private String artifactId;

    @Parameter(property = "init.version", defaultValue = "1.0.0")
    private String version;

    @Parameter(property = "init.package", defaultValue = "com.gallop.project")
    private String packageName;

    @Parameter(property = "init.modelType", defaultValue = "ANEMIC")
    private String modelType;

    /**
     * 主键生成策略（AUTO/ASSIGN_ID/INPUT/NONE），渲染到生成项目 mybatis-plus id-type 默认值。
     */
    @Parameter(property = "init.idStrategy", defaultValue = "ASSIGN_ID")
    private String idStrategy;

    @Parameter(property = "init.databaseType", defaultValue = "mysql")
    private String databaseType;

    @Parameter(property = "init.enableCache", defaultValue = "false")
    private boolean enableCache;

    @Parameter(property = "init.enableEvent", defaultValue = "false")
    private boolean enableEvent;

    @Parameter(property = "init.enableSearch", defaultValue = "false")
    private boolean enableSearch;

    @Parameter(property = "init.enableMonitor", defaultValue = "false")
    private boolean enableMonitor;

    @Parameter(property = "init.enableSecurity", defaultValue = "false")
    private boolean enableSecurity;

    @Parameter(property = "init.outputDir")
    private File outputDir;

    @Parameter(property = "init.fastdddVersion")
    private String fastdddVersion;

    /**
     * 生成项目引用的 fastddd-maven-plugin 版本（Tooling 轴）。
     * <p>缺省取 {@code fastddd-version.properties} 的 {@code fastddd.tooling.version}
     * ——即当前插件自身 reactor 版本；与 {@code init.fastdddVersion}（Framework 轴）互不推导。
     */
    @Parameter(property = "init.fastdddToolingVersion")
    private String fastdddToolingVersion;

    @Parameter(property = "init.springBootVersion", defaultValue = "3.5.16")
    private String springBootVersion;

    @Parameter(property = "init.contexts")
    private String contexts;

    @Parameter(property = "init.tablePrefix", defaultValue = "t_")
    private String tablePrefix;

    /**
     * 生成内容语言（L4 模板国际化）：zh（缺省，兼容既有生成结果）或 en；
     * 控制脚手架文件（README/application.yml/聚合根注释等）内的文案语言。
     */
    @Parameter(property = "init.language", defaultValue = "zh")
    private String language;

    @Override
    public void execute() throws MojoExecutionException {
        getLog().info("========================================");
        getLog().info("FastDDD 项目初始化向导");
        getLog().info("========================================");

        fastdddVersion = resolveFastdddVersion();
        fastdddToolingVersion = resolveFastdddToolingVersion();
        resolveOutputDir();
        validateParameters();
        displayConfiguration();

        File projectDir = new File(outputDir, artifactId);
        if (projectDir.exists()) {
            throw new MojoExecutionException("目标目录已存在: " + projectDir.getAbsolutePath()
                + "\n请删除该目录或使用不同的 artifactId");
        }

        try {
            TemplateRenderer renderer = createTemplateRenderer();
            TemplateContext ctx = buildContext();

            generateProjectFiles(projectDir, renderer, ctx);
            generateContexts(projectDir, ctx, renderer);

            getLog().info("");
            getLog().info("========================================");
            getLog().info("项目生成成功！");
            getLog().info("========================================");
            getLog().info("项目位置: " + projectDir.getAbsolutePath());
            getLog().info("");
            getLog().info("进入项目目录:");
            getLog().info("  cd " + artifactId);
            getLog().info("");
            getLog().info("编译项目:");
            getLog().info("  mvn compile");
            getLog().info("");
            getLog().info("运行项目:");
            getLog().info("  mvn spring-boot:run");
            getLog().info("========================================");

        } catch (MojoExecutionException e) {
            cleanupOnFailure(projectDir);
            throw e;
        } catch (Exception e) {
            getLog().error("项目初始化失败: " + e.getMessage(), e);
            cleanupOnFailure(projectDir);
            throw new MojoExecutionException("项目初始化失败", e);
        }
    }

    // ==================== Template Engine ====================

    private TemplateRenderer createTemplateRenderer() throws MojoExecutionException {
        try {
            ClassLoader cl = getClass().getClassLoader();
            if (cl.getResource("templates/project/pom.xml.ftl") == null) {
                throw new MojoExecutionException(
                    "模板资源未找到: fastddd-templates JAR 可能未安装到本地仓库。请先执行 mvn install -pl fastddd-templates");
            }

            Configuration cfg = new Configuration(Configuration.VERSION_2_3_33);
            cfg.setClassLoaderForTemplateLoading(cl, "templates");
            cfg.setDefaultEncoding("UTF-8");
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            cfg.setLocalizedLookup(false);
            TemplateMessages.apply(cfg, language);
            return new TemplateRenderer(cfg);
        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("FreeMarker 初始化失败", e);
        }
    }

    private TemplateContext buildContext() {
        TemplateContext ctx = TemplateContext.builder()
            .groupId(groupId)
            .artifactId(artifactId)
            .version(version)
            .packageName(packageName)
            .modelType(ModelType.valueOf(modelType.toUpperCase()))
            .idStrategy(idStrategy != null ? idStrategy.toUpperCase() : "ASSIGN_ID")
            .databaseType(DatabaseType.valueOf(databaseType.toUpperCase()))
            .enableCQRS("RICH".equalsIgnoreCase(modelType))
            .modules(buildModuleSet())
            .fastdddVersion(fastdddVersion)
            .fastdddToolingVersion(fastdddToolingVersion)
            .javaVersion("21")
            .springBootVersion(springBootVersion)
            .author(System.getProperty("user.name", "FastDDD Generator"))
            .date(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            .build();
        ctx.calculatePackages();
        return ctx;
    }

    /**
     * 默认使用当前插件构建时写入的目标 Framework 版本；用户仍可通过
     * -Dinit.fastdddVersion 显式覆盖。
     *
     * <p>VER-001 步骤 5：解析后用 {@link CompatibilityMatrix} 校验该版本为已登记的 Framework 线；
     * 未登记仅告警（不 fail），保持生成链路在矩阵缺失时仍可用。Mojo 通道的目标 Framework 版本
     * 经 {@code fastddd-version.properties} 的 {@code @fastddd-framework.version@} 过滤取自
     * Tooling 构建所认证的上游 Framework 版本——不是 Tooling 自身 {@code revision}
     * （ECO-003 版本解耦：不根据 Tooling 版本推导 Framework 版本）。
     */
    private String resolveFastdddVersion() throws MojoExecutionException {
        String resolved;
        if (fastdddVersion != null && !fastdddVersion.isBlank()) {
            resolved = fastdddVersion;
        } else {
            resolved = versionProperties().getProperty("fastddd.version", "1.0.0-jdk21-SNAPSHOT");
        }
        validateFrameworkVersionAgainstMatrix(resolved);
        return resolved;
    }

    /**
     * 生成项目引用的 fastddd-maven-plugin 版本（Tooling 轴）：
     * -Dinit.fastdddToolingVersion 覆盖，缺省取 {@code fastddd-version.properties} 的
     * {@code fastddd.tooling.version}（经 {@code @revision@} 过滤 = 插件自身 reactor 版本）。
     * 与 Framework 轴互不推导。
     */
    private String resolveFastdddToolingVersion() throws MojoExecutionException {
        if (fastdddToolingVersion != null && !fastdddToolingVersion.isBlank()) {
            return fastdddToolingVersion;
        }
        return versionProperties().getProperty("fastddd.tooling.version", "1.0.0-jdk21-SNAPSHOT");
    }

    private Properties versionProperties() throws MojoExecutionException {
        Properties properties = new Properties();
        try (InputStream input = getClass().getClassLoader()
                .getResourceAsStream("fastddd-version.properties")) {
            if (input != null) {
                properties.load(input);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("读取 FastDDD 版本信息失败", e);
        }
        return properties;
    }

    private void validateFrameworkVersionAgainstMatrix(String resolved) {
        try {
            if (CompatibilityMatrix.load().manifestFor(resolved) == null) {
                getLog().warn("目标 Framework 版本 " + resolved
                        + " 未在兼容矩阵登记，生成项目可能引用未经验证的依赖组合");
            }
        } catch (RuntimeException e) {
            getLog().warn("兼容矩阵加载失败，跳过目标版本校验: " + e.getMessage());
        }
    }

    private Set<String> buildModuleSet() {
        Set<String> modules = new HashSet<>();
        if (enableSecurity) modules.add("security");
        if (enableCache)    modules.add("cache");
        if (enableEvent)    modules.add("event");
        if (enableSearch)   modules.add("search");
        if (enableMonitor)  modules.add("monitor");
        return modules;
    }

    // ==================== File Generation ====================

    private void generateProjectFiles(File projectDir, TemplateRenderer renderer, TemplateContext ctx)
            throws MojoExecutionException {
        try {
            createDirectories(projectDir);

            // Project-level templates (shared with Studio)
            writeFile(projectDir, "pom.xml",
                renderer.renderProjectTemplate("pom.xml.ftl", ctx));
            writeFile(projectDir, "src/main/resources/application.yml",
                renderer.renderProjectTemplate("application.yml.ftl", ctx));
            writeFile(projectDir, "src/main/resources/application-dev.yml",
                renderer.renderProjectTemplate("application-dev.yml.ftl", ctx));
            writeFile(projectDir, "src/main/resources/application-prod.yml",
                renderer.renderProjectTemplate("application-prod.yml.ftl", ctx));

            String pkgPath = ctx.getPackageName().replace('.', '/');
            String className = ctx.toPascalCase(artifactId);
            writeFile(projectDir,
                "src/main/java/" + pkgPath + "/" + className + ".java",
                renderer.renderProjectTemplate("Application.java.ftl", ctx));

            writeFile(projectDir, "README.md",
                renderer.renderProjectTemplate("README.md.ftl", ctx));
            writeFile(projectDir, "Dockerfile",
                renderer.renderProjectTemplate("Dockerfile.ftl", ctx));
            writeFile(projectDir, ".gitignore",
                renderer.renderProjectTemplate("gitignore.ftl", ctx));

            // Init SQL
            writeFile(projectDir, "src/main/resources/init-data.sql",
                renderer.renderProjectTemplate("init-data.sql.ftl", ctx));

            // Security RBAC scaffold
            if (ctx.getModules().contains("security")) {
                generateSecurityRbac(projectDir, renderer, ctx);
            }

            getLog().info("项目结构生成完成");

        } catch (MojoExecutionException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("项目生成失败", e);
        }
    }

    private void generateSecurityRbac(File projectDir, TemplateRenderer renderer, TemplateContext ctx)
            throws Exception {
        String pkgPath = ctx.getPackageName().replace('.', '/');

        writeFile(projectDir,
            "src/main/java/" + pkgPath + "/security/AppIdentityService.java",
            renderer.renderProjectTemplate("security/SimpleIdentityService.java.ftl", ctx));

        getLog().info("Security SPI 骨架生成完成 (TODO 模式，完整 RBAC 请通过 Studio 生成)");
    }

    // ==================== File Utilities ====================

    private void createDirectories(File projectDir) {
        String pkgPath = packageName.replace('.', '/');
        new File(projectDir, "src/main/java/" + pkgPath).mkdirs();
        new File(projectDir, "src/main/resources").mkdirs();
        new File(projectDir, "src/test/java/" + pkgPath).mkdirs();
        new File(projectDir, "src/test/resources").mkdirs();
    }

    private void writeFile(File projectDir, String relativePath, String content) throws IOException {
        File file = new File(projectDir, relativePath);
        file.getParentFile().mkdirs();
        Files.writeString(file.toPath(), content);
        getLog().debug("  生成: " + relativePath);
    }

    private void deleteRecursively(File dir) throws IOException {
        if (!dir.exists()) return;
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        Files.deleteIfExists(dir.toPath());
    }

    private void cleanupOnFailure(File projectDir) {
        if (projectDir == null || !projectDir.exists()) return;
        try {
            deleteRecursively(projectDir);
            getLog().info("已清理部分生成的文件: " + projectDir.getAbsolutePath());
        } catch (IOException cleanupEx) {
            getLog().warn("清理生成的文件失败: " + cleanupEx.getMessage());
        }
    }

    // ==================== Context Generation ====================

    private void generateContexts(File projectDir, TemplateContext baseCtx, TemplateRenderer renderer)
            throws MojoExecutionException {
        if (contexts == null || contexts.trim().isEmpty()) return;

        String[] contextNames = contexts.split(",");
        getLog().info("生成 " + contextNames.length + " 个限界上下文: " + contexts);

        for (String rawName : contextNames) {
            String trimmed = rawName.trim();
            if (trimmed.isEmpty()) continue;
            try {
                generateSingleContext(projectDir, baseCtx, renderer, trimmed);
            } catch (IOException e) {
                throw new MojoExecutionException(
                    "生成 context '" + trimmed + "' 失败: " + e.getMessage(), e);
            }
        }
    }

    private void generateSingleContext(File projectDir, TemplateContext baseCtx,
                                       TemplateRenderer renderer, String rawContextName)
            throws IOException, MojoExecutionException {
        String contextName = baseCtx.toPascalCase(rawContextName);
        String contextNameCamel = baseCtx.toCamelCase(rawContextName);
        String contextNameSnake = toSnakeCase(rawContextName);
        String contextPackage = baseCtx.getPackageName() + "." + contextNameCamel;
        String tableName = tablePrefix + contextNameSnake;

        // Build fresh variable map: base vars + context overrides
        Map<String, Object> vars = new HashMap<>(baseCtx.toTemplateVariables());
        vars.put("contextName", contextName);
        vars.put("contextNameCamel", contextNameCamel);
        vars.put("contextNameSnake", contextNameSnake);
        vars.put("contextPackage", contextPackage);
        vars.put("contextDomainPkg", contextPackage + ".domain");
        vars.put("contextApplicationPkg", contextPackage + ".application");
        vars.put("contextPersistencePkg", contextPackage + ".infrastructure.persistence");
        vars.put("contextInterfacesPkg", contextPackage + ".interfaces");
        vars.put("contextDtoPkg", contextPackage + ".interfaces.dto");
        vars.put("contextEventPkg", contextPackage + ".domain.event");
        vars.put("aggregateClassName", contextName);
        vars.put("aggregateVarName", contextNameCamel);
        vars.put("tableName", tableName);
        vars.put("enableEvent", baseCtx.getModules() != null && baseCtx.getModules().contains("event"));

        getLog().info("  生成上下文: " + contextName + " (package: " + contextPackage + ")");

        String srcMain = "src/main/java";
        String domainPath = pkgToPath(contextPackage + ".domain");
        String appPath = pkgToPath(contextPackage + ".application");
        String persistencePath = pkgToPath(contextPackage + ".infrastructure.persistence");
        String interfacesPath = pkgToPath(contextPackage + ".interfaces");
        String dtoPath = pkgToPath(contextPackage + ".interfaces.dto");
        String eventPath = pkgToPath(contextPackage + ".domain.event");

        // 1. AggregateRoot
        renderAndWrite(renderer, "project/context/AggregateRoot.ftl", vars,
            projectDir, srcMain + "/" + domainPath + "/" + contextName + ".java");

        // 2. Repository interface
        renderAndWrite(renderer, "project/context/Repository.ftl", vars,
            projectDir, srcMain + "/" + domainPath + "/" + contextName + "Repository.java");

        // 3. StatusEnum
        renderAndWrite(renderer, "project/context/StatusEnum.ftl", vars,
            projectDir, srcMain + "/" + domainPath + "/" + contextName + "Status.java");

        // 4. DomainEvent (only when enableEvent)
        if (Boolean.TRUE.equals(vars.get("enableEvent"))) {
            renderAndWrite(renderer, "project/context/DomainEvent.ftl", vars,
                projectDir, srcMain + "/" + eventPath + "/" + contextName + "CreatedEvent.java");
        }

        // 5. ApplicationService
        renderAndWrite(renderer, "project/context/ApplicationService.ftl", vars,
            projectDir, srcMain + "/" + appPath + "/" + contextName + "Service.java");

        // 6. PO
        renderAndWrite(renderer, "project/context/PO.ftl", vars,
            projectDir, srcMain + "/" + persistencePath + "/" + contextName + "PO.java");

        // 7. Mapper
        renderAndWrite(renderer, "project/context/Mapper.ftl", vars,
            projectDir, srcMain + "/" + persistencePath + "/" + contextName + "Mapper.java");

        // 8. Converter
        renderAndWrite(renderer, "project/context/Converter.ftl", vars,
            projectDir, srcMain + "/" + persistencePath + "/" + contextName + "Converter.java");

        // 9. RepositoryImpl
        renderAndWrite(renderer, "project/context/RepositoryImpl.ftl", vars,
            projectDir, srcMain + "/" + persistencePath + "/" + contextName + "RepositoryImpl.java");

        // 10. Controller
        renderAndWrite(renderer, "project/context/Controller.ftl", vars,
            projectDir, srcMain + "/" + interfacesPath + "/" + contextName + "Controller.java");

        // 11. DTO
        renderAndWrite(renderer, "project/context/DTO.ftl", vars,
            projectDir, srcMain + "/" + dtoPath + "/" + contextName + "Response.java");
    }

    private void renderAndWrite(TemplateRenderer renderer, String templateName,
                                Map<String, Object> vars, File projectDir, String relativePath)
            throws IOException {
        String content = renderer.render(templateName, vars);
        writeFile(projectDir, relativePath, content);
    }

    private String pkgToPath(String pkg) {
        return pkg.replace('.', '/');
    }

    private String toSnakeCase(String input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '-' || c == '_') {
                sb.append('_');
            } else if (Character.isUpperCase(c)) {
                if (i > 0) sb.append('_');
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    private boolean isValidJavaIdentifier(String name) {
        if (name == null || name.isEmpty()) return false;
        if (!Character.isJavaIdentifierStart(name.charAt(0))) return false;
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) return false;
        }
        return true;
    }

    private String toPascalCase(String input) {
        if (input == null || input.isEmpty()) return input;
        String[] parts = input.split("[-_]");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)));
                if (part.length() > 1) sb.append(part.substring(1));
            }
        }
        return sb.toString();
    }

    // ==================== Parameter Validation ====================

    private void resolveOutputDir() {
        if (outputDir == null) {
            if (project != null && project.getBasedir() != null) {
                outputDir = project.getBasedir();
            } else {
                outputDir = new File(System.getProperty("user.dir"));
            }
        }
    }

    private void validateParameters() throws MojoExecutionException {
        if (groupId == null || groupId.trim().isEmpty()) {
            throw new MojoExecutionException("groupId 不能为空");
        }
        if (artifactId == null || artifactId.trim().isEmpty()) {
            throw new MojoExecutionException("artifactId 不能为空");
        }
        if (version == null || version.trim().isEmpty()) {
            throw new MojoExecutionException("version 不能为空");
        }
        if (!"ANEMIC".equalsIgnoreCase(modelType) && !"RICH".equalsIgnoreCase(modelType)) {
            throw new MojoExecutionException("modelType 必须是 ANEMIC 或 RICH");
        }
        try {
            DatabaseType.valueOf(databaseType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("databaseType 必须是 mysql, postgresql 或 h2");
        }
        try {
            TemplateMessages.normalize(language);
        } catch (IllegalArgumentException e) {
            throw new MojoExecutionException("init.language " + e.getMessage());
        }
        if (packageName == null || packageName.trim().isEmpty()) {
            packageName = groupId;
        }
        if ("RICH".equalsIgnoreCase(modelType)) {
            getLog().info("RICH 模型自动启用 CQRS 模式");
        }

        // Validate contexts parameter
        if (contexts != null && !contexts.trim().isEmpty()) {
            if (!"RICH".equalsIgnoreCase(modelType)) {
                throw new MojoExecutionException(
                    "contexts 参数需要 modelType=RICH。ANEMIC 模型不支持限界上下文生成。");
            }
            Set<String> seen = new HashSet<>();
            for (String ctx : contexts.split(",")) {
                String trimmed = ctx.trim();
                if (trimmed.isEmpty()) continue;
                if (!isValidJavaIdentifier(trimmed)) {
                    throw new MojoExecutionException(
                        "无效的 context 名称: '" + trimmed + "'。必须是合法 Java 标识符。");
                }
                String pascal = toPascalCase(trimmed);
                if (!seen.add(pascal)) {
                    throw new MojoExecutionException(
                        "重复的 context 名称: '" + pascal + "'。请检查 -Dinit.contexts 参数。");
                }
            }
        }
    }

    private void displayConfiguration() {
        getLog().info("");
        getLog().info("项目配置:");
        getLog().info("  groupId       : " + groupId);
        getLog().info("  artifactId    : " + artifactId);
        getLog().info("  version       : " + version);
        getLog().info("  package       : " + packageName);
        getLog().info("  modelType     : " + modelType);
        getLog().info("  databaseType  : " + databaseType);
        getLog().info("  enableSecurity: " + enableSecurity);
        getLog().info("  enableCache   : " + enableCache);
        getLog().info("  enableEvent   : " + enableEvent);
        getLog().info("  enableSearch  : " + enableSearch);
        getLog().info("  enableMonitor : " + enableMonitor);
        getLog().info("  fastdddVersion (Framework): " + fastdddVersion);
        getLog().info("  fastdddToolingVersion (Tooling): " + fastdddToolingVersion);
        getLog().info("  contexts      : " + (contexts != null ? contexts : "(无)"));
        getLog().info("  tablePrefix   : " + tablePrefix);
        getLog().info("  language      : " + language);
        getLog().info("  输出目录       : " + outputDir.getAbsolutePath());
        getLog().info("");
    }
}
