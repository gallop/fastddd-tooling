package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 生成配置模板的配置契约校验（CFG-002 步骤 5）。
 *
 * <p>渲染 application.yml.ftl（单项目，全模块 + RICH）与 multi-channel/sub-app-application.yml.ftl，
 * 用 SnakeYAML 解析渲染产物，遍历键路径，断言：
 * <ul>
 *   <li>不存在已废弃根 {@code fast-ddd.*}；</li>
 *   <li>不存在未绑定键 {@code fastddd.application.*} / {@code fastddd.event.*}；</li>
 *   <li>SQL 监控阈值键为 {@code slow-query-threshold}（非拼写错误的 slow-threshold）；</li>
 *   <li>JWT secret 为环境变量引用（无字面回落值）；</li>
 *   <li>多端模板 http/cors 正确嵌套于 security 之下（{@code fastddd.security.http.*} / {@code fastddd.security.cors.*}）。</li>
 * </ul>
 *
 * @author gallop
 */
class ProjectConfigContractTest {

    private TemplateRenderer renderer;

    /** 框架实际提供 @ConfigurationProperties / @Value 绑定的 fastddd 二级域（白名单）。 */
    private static final Set<String> BOUND_FASTDDD_DOMAINS = Set.of(
            "security", "cache", "monitor", "search", "web", "jackson", "ai", "analyzer");

    @BeforeEach
    void setUp() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(TemplateContext.class, "/templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        // L4：注入文案字典（测试走缺省 zh，与历史断言的生成结果一致）
        TemplateMessages.apply(configuration, TemplateMessages.DEFAULT_LANGUAGE);

        renderer = new TemplateRenderer(configuration);
    }

    // ==================== 单项目模板 ====================

    @Test
    void singleProjectApplicationYml_shouldSatisfyConfigContract() throws Exception {
        Map<String, Object> vars = baseSingleProjectVars();
        // 全模块（含已删除 event —— 验证 event 块不再生成）；RICH 模型（验证 cqrs 块已删除）
        vars.put("modules", Set.of("security", "cache", "event", "monitor", "search"));
        vars.put("modelType", "RICH");

        String rendered = renderer.render("project/application.yml.ftl", vars);
        Map<String, Object> root = new Yaml().load(rendered);

        // D1：无已废弃根 fast-ddd
        assertThat(rendered).doesNotContain("fast-ddd:");

        List<String> fastdddPaths = collectDottedPaths(root, "fastddd");
        List<String> legacyPaths = collectDottedPaths(root, "fast-ddd");
        assertThat(legacyPaths).as("不应生成已废弃根 fast-ddd.*").isEmpty();

        // D3：未绑定键不得出现
        assertThat(fastdddPaths.stream().noneMatch(p -> p.startsWith("fastddd.application.")))
                .as("不应生成未绑定的 fastddd.application.cqrs.*").isTrue();
        assertThat(fastdddPaths.stream().noneMatch(p -> p.startsWith("fastddd.event.")))
                .as("不应生成未绑定的 fastddd.event.*").isTrue();

        // 白名单：所有 fastddd 二级域必须可绑定
        Set<String> domains = secondLevelDomains(fastdddPaths);
        assertThat(domains).isSubsetOf(BOUND_FASTDDD_DOMAINS);

        // D2：SQL 监控阈值键拼写正确
        assertThat(fastdddPaths).contains("fastddd.monitor.sql.slow-query-threshold");
        assertThat(rendered).doesNotContain("slow-threshold:");

        // D4：JWT secret 为环境变量引用（无字面回落）
        Object secret = nested(root, "fastddd", "security", "jwt", "secret");
        assertThat(secret).isEqualTo("${JWT_SECRET}");

        // D5：id-type 默认 assign_id（与 BasePO 的 ASSIGN_ID 一致）
        Object idType = nested(root, "mybatis-plus", "global-config", "db-config", "id-type");
        assertThat(idType).asString().contains("assign_id");
    }

    @Test
    void singleProjectApplicationYml_anemicMinimal_shouldStillBind() throws Exception {
        Map<String, Object> vars = baseSingleProjectVars();
        vars.put("modules", Set.of("security"));
        vars.put("modelType", "ANEMIC");

        String rendered = renderer.render("project/application.yml.ftl", vars);
        Map<String, Object> root = new Yaml().load(rendered);

        assertThat(collectDottedPaths(root, "fast-ddd")).isEmpty();
        assertThat(secondLevelDomains(collectDottedPaths(root, "fastddd")))
                .containsExactlyInAnyOrder("security");
    }

    /**
     * ID-007 步骤 5/7（C2）：idStrategy 真实驱动生成项目 mybatis-plus id-type 默认值。
     * 历史模板写死 {@code ${mybatis-plus.id-type:assign_id}}，所选 idStrategy 被丢弃；
     * 修复后 id-type 默认值随 idStrategy（ASSIGN_ID/AUTO/INPUT/NONE）渲染。证据级断言。
     */
    @Test
    void singleProjectApplicationYml_idStrategyDrivesIdType() throws Exception {
        Yaml yaml = new Yaml();

        // ASSIGN_ID（默认）→ assign_id
        Map<String, Object> varsAssign = baseSingleProjectVars();
        varsAssign.put("idStrategy", "ASSIGN_ID");
        Object idTypeAssign = nested(yaml.load(renderer.render("project/application.yml.ftl", varsAssign)),
                "mybatis-plus", "global-config", "db-config", "id-type");
        assertThat(idTypeAssign).asString().contains("assign_id");

        // AUTO → auto（证明 idStrategy 真实驱动，而非历史写死 assign_id）
        Map<String, Object> varsAuto = baseSingleProjectVars();
        varsAuto.put("idStrategy", "AUTO");
        Object idTypeAuto = nested(yaml.load(renderer.render("project/application.yml.ftl", varsAuto)),
                "mybatis-plus", "global-config", "db-config", "id-type");
        assertThat(idTypeAuto).asString().contains("auto");
        assertThat(idTypeAuto).asString().doesNotContain("assign_id");

        // INPUT → input；NONE → none
        Map<String, Object> varsInput = baseSingleProjectVars();
        varsInput.put("idStrategy", "INPUT");
        Object idTypeInput = nested(yaml.load(renderer.render("project/application.yml.ftl", varsInput)),
                "mybatis-plus", "global-config", "db-config", "id-type");
        assertThat(idTypeInput).asString().contains("input");

        Map<String, Object> varsNone = baseSingleProjectVars();
        varsNone.put("idStrategy", "NONE");
        Object idTypeNone = nested(yaml.load(renderer.render("project/application.yml.ftl", varsNone)),
                "mybatis-plus", "global-config", "db-config", "id-type");
        assertThat(idTypeNone).asString().contains("none");

        // 未传 idStrategy（TemplateContext 默认 ASSIGN_ID 的等价渲染）→ assign_id
        Map<String, Object> varsDefault = baseSingleProjectVars();
        Object idTypeDefault = nested(yaml.load(renderer.render("project/application.yml.ftl", varsDefault)),
                "mybatis-plus", "global-config", "db-config", "id-type");
        assertThat(idTypeDefault).asString().contains("assign_id");
    }

    /** GEN-001：生产 profile 不得重组用户确认的 URL/账号；连接池属性与显式类型一致。 */
    @Test
    void datasourceProfilesPreserveConfirmedConnectionAndMatchSelectedPool() throws Exception {
        Map<String, Object> hikariVars = baseSingleProjectVars();
        hikariVars.put("datasourceUrl", "jdbc:mysql://db.internal:3307/confirmed?useSSL=true");
        hikariVars.put("datasourceUsername", "confirmed_user");
        hikariVars.put("datasourcePassword", "confirmed_password");
        hikariVars.put("connectionPool", "HIKARI");

        String baseHikari = renderer.render("project/application.yml.ftl", hikariVars);
        String prodHikari = renderer.render("project/application-prod.yml.ftl", hikariVars);
        Map<String, Object> baseHikariYaml = new Yaml().load(baseHikari);
        Map<String, Object> prodHikariYaml = new Yaml().load(prodHikari);
        assertThat(nested(baseHikariYaml, "spring", "datasource", "url"))
                .isEqualTo("jdbc:mysql://db.internal:3307/confirmed?useSSL=true");
        assertThat(nested(baseHikariYaml, "spring", "datasource", "username")).isEqualTo("confirmed_user");
        assertThat(nested(baseHikariYaml, "spring", "datasource", "type"))
                .isEqualTo("com.zaxxer.hikari.HikariDataSource");
        assertThat(nested(prodHikariYaml, "spring", "datasource", "url")).isNull();
        assertThat(nested(prodHikariYaml, "spring", "datasource", "username")).isNull();
        assertThat(nested(prodHikariYaml, "spring", "datasource", "password")).isNull();
        assertThat(prodHikari).contains("hikari:").doesNotContain("druid:")
                .doesNotContain("jdbc:mysql://${DB_HOST");

        Map<String, Object> druidVars = new LinkedHashMap<>(hikariVars);
        druidVars.put("connectionPool", "DRUID");
        druidVars.put("groupId", "com.test");
        druidVars.put("version", "1.0.0-SNAPSHOT");
        druidVars.put("projectDescription", "test");
        druidVars.put("modelType", "ANEMIC");
        druidVars.put("modelTypeDisplayName", "贫血");
        druidVars.put("springBootVersion", "3.5.16");
        druidVars.put("javaVersion", "21");
        druidVars.put("fastdddVersion", "1.0.0-jdk21-SNAPSHOT");
        druidVars.put("fastdddToolingVersion", "1.0.0-jdk21-SNAPSHOT");
        druidVars.put("enableEvent", false);
        String baseDruid = renderer.render("project/application.yml.ftl", druidVars);
        String prodDruid = renderer.render("project/application-prod.yml.ftl", druidVars);
        String pomDruid = renderer.render("project/pom.xml.ftl", druidVars);
        assertThat(nested(new Yaml().load(baseDruid), "spring", "datasource", "type"))
                .isEqualTo("com.alibaba.druid.pool.DruidDataSource");
        assertThat(prodDruid).contains("druid:").doesNotContain("hikari:")
                .doesNotContain("jdbc:mysql://${DB_HOST");
        assertThat(pomDruid).contains("druid-spring-boot-3-starter");
    }

    /**
     * ECO-007A（C5-F2/F3 修复契约）：
     * <ul>
     *   <li>数据库形态默认 HIKARI：pom 显式携带 {@code spring-boot-starter-jdbc}（引入 HikariCP），
     *       不依赖 fast-ddd-event/crud 的偶然传递（C4 后 crud 不再传递任何池实现）；</li>
     *   <li>显式 DRUID：starter 依赖带 {@code ${druid.version}} 属性引用 + 属性值（对齐 fastddd-bom 受管值；
     *       生成项目以 Boot parent 为父不经 BOM 受管，历史缺陷为无版本号不可解析）；</li>
     *   <li>监控阈值：按框架 {@code SqlMonitoringProperties.slowQueryThreshold}（long，毫秒）契约渲染纯数值，
     *       不带 Spring Duration 单位后缀（历史 {@code 1000ms} 绑定 NumberFormatException 致启动失败）。</li>
     * </ul>
     */
    @Test
    void pomPoolDependencyExplicit_andMonitorThresholdNumeric() throws Exception {
        // 默认 HIKARI：显式连接池依赖在场，无 Druid 残留
        Map<String, Object> hikari = pomRenderVars();
        String pomHikari = renderer.render("project/pom.xml.ftl", hikari);
        assertThat(pomHikari).contains("spring-boot-starter-jdbc");
        assertThat(pomHikari).doesNotContain("druid-spring-boot-3-starter");
        assertThat(pomHikari).doesNotContain("<druid.version>");

        // 显式 DRUID：starter + 属性化版本对（可解析），且不带 HIKARI 的 starter-jdbc
        Map<String, Object> druid = pomRenderVars();
        druid.put("connectionPool", "DRUID");
        String pomDruid = renderer.render("project/pom.xml.ftl", druid);
        assertThat(pomDruid).contains("druid-spring-boot-3-starter");
        assertThat(pomDruid).contains("<druid.version>1.2.28</druid.version>");
        assertThat(pomDruid).contains("<version>${druid.version}</version>");
        assertThat(pomDruid).doesNotContain("spring-boot-starter-jdbc");

        // 监控阈值：纯数值（long 毫秒契约），非 Duration 字面量
        Map<String, Object> monitor = baseSingleProjectVars();
        monitor.put("modules", Set.of("monitor"));
        String yml = renderer.render("project/application.yml.ftl", monitor);
        assertThat(yml).contains("slow-query-threshold: 1000");
        assertThat(yml).doesNotContain("slow-query-threshold: 1000ms");
    }

    /** pom.xml.ftl 渲染所需的最小变量集（对齐 GEN-001 用例的 DRUID 渲染变量面）。 */
    private Map<String, Object> pomRenderVars() {
        Map<String, Object> vars = new LinkedHashMap<>(baseSingleProjectVars());
        vars.put("groupId", "com.test");
        vars.put("version", "1.0.0-SNAPSHOT");
        vars.put("projectDescription", "test");
        vars.put("modelType", "ANEMIC");
        vars.put("modelTypeDisplayName", "贫血");
        vars.put("springBootVersion", "3.5.16");
        vars.put("javaVersion", "21");
        vars.put("fastdddVersion", "1.0.0-jdk21-SNAPSHOT");
        vars.put("fastdddToolingVersion", "1.0.0-jdk21-SNAPSHOT");
        vars.put("enableEvent", false);
        return vars;
    }

    // ==================== 多端模板 ====================

    @Test
    void multiChannelSubAppYml_shouldNestHttpAndCorsUnderSecurity() throws Exception {
        Map<String, Object> subApp = new LinkedHashMap<>();
        subApp.put("port", "8081");
        subApp.put("moduleName", "customer-app");
        subApp.put("channel", "CUSTOMER");
        subApp.put("corsOrigins", "http://localhost:3000");
        subApp.put("wechatMiniAppId", "wx-test-appid");
        subApp.put("wechatMiniSecret", "");
        subApp.put("packageName", "com.example.customer");

        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("subApp", subApp);
        vars.put("databaseType", "MYSQL");
        vars.put("databaseName", "mydb");
        vars.put("basePackage", "com.example");

        String rendered = renderer.render("project/multi-channel/sub-app-application.yml.ftl", vars);
        Map<String, Object> root = new Yaml().load(rendered);

        // D1：无已废弃根
        assertThat(rendered).doesNotContain("fast-ddd:");
        assertThat(collectDottedPaths(root, "fast-ddd")).isEmpty();

        List<String> fastdddPaths = collectDottedPaths(root, "fastddd");

        // D6（核心）：http 与 cors 必须嵌套在 security 之下，否则绑定到不存在的 fastddd.http / fastddd.cors
        assertThat(fastdddPaths).anyMatch(p -> p.startsWith("fastddd.security.http."));
        assertThat(fastdddPaths).anyMatch(p -> p.startsWith("fastddd.security.cors."));
        assertThat(fastdddPaths.stream().noneMatch(p -> p.equals("fastddd.http") || p.startsWith("fastddd.http.")
                || p.equals("fastddd.cors") || p.startsWith("fastddd.cors.")))
                .as("http/cors 不得作为 fastddd 顶级域（须嵌套于 security）").isTrue();

        // wechat-mini 经 @Value 绑定，应位于 security 之下
        assertThat(fastdddPaths).anyMatch(p -> p.startsWith("fastddd.security.wechat-mini."));

        // JWT secret 环境变量引用
        assertThat(nested(root, "fastddd", "security", "jwt", "secret")).isEqualTo("${JWT_SECRET}");

        // id-type 默认 assign_id
        assertThat(nested(root, "mybatis-plus", "global-config", "db-config", "id-type"))
                .asString().contains("assign_id");
    }

    // ==================== 辅助 ====================

    private Map<String, Object> baseSingleProjectVars() {
        Map<String, Object> vars = new LinkedHashMap<>();
        vars.put("artifactId", "test-app");
        vars.put("packageName", "com.test.app");
        vars.put("databaseType", "MYSQL");
        vars.put("databaseName", "test-app");
        vars.put("databaseDriver", "com.mysql.cj.jdbc.Driver");
        vars.put("datasourceUrl", "");
        return vars;
    }

    /** 递归收集所有以 {@code rootKey} 为起点的点分键路径。 */
    @SuppressWarnings("unchecked")
    private static List<String> collectDottedPaths(Map<String, Object> root, String rootKey) {
        List<String> paths = new ArrayList<>();
        Object node = root.get(rootKey);
        if (node == null) {
            return paths;
        }
        walk(rootKey, node, paths);
        return paths;
    }

    @SuppressWarnings("unchecked")
    private static void walk(String prefix, Object node, List<String> out) {
        out.add(prefix);
        if (node instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> e : map.entrySet()) {
                walk(prefix + "." + e.getKey(), e.getValue(), out);
            }
        }
        // List/标量不再下钻（列表元素不是属性键）
    }

    /** 从 fastddd.* 路径集合中取二级域（如 security/cache/monitor）。 */
    private static Set<String> secondLevelDomains(List<String> fastdddPaths) {
        Set<String> domains = new java.util.LinkedHashSet<>();
        for (String p : fastdddPaths) {
            String[] parts = p.split("\\.");
            if (parts.length >= 2) {
                domains.add(parts[1]);
            }
        }
        return domains;
    }

    /** 按点分路径取嵌套值。 */
    @SuppressWarnings("unchecked")
    private static Object nested(Map<String, Object> root, String... path) {
        Object node = root;
        for (String segment : path) {
            if (!(node instanceof Map<?, ?>)) {
                return null;
            }
            node = ((Map<String, Object>) node).get(segment);
            if (node == null) {
                return null;
            }
        }
        return node;
    }
}
