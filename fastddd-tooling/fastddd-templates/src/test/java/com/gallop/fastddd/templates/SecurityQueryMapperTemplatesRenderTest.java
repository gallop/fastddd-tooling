package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityQueryMapper 模板「自定义安全五表」渲染回归测试（T8.64 F-⑥ 盲区闭合）。
 *
 * <p>该模板仅在设计中五张安全正名表齐备（USER/ROLE/PERMISSION/USER_ROLE/
 * ROLE_PERMISSION_TABLE）时由 studio SecuritySpiRenderer 以 Map 直传 queryVars
 * 渲染（12 键：五表名+六列名+packageName）。历史模板持有 2 处错误转义形
 * {@code #${r"${userId}"}}（# 留在 raw 串外）：渲染期通过 → 生成项目带病交付 →
 * MyBatis 运行期将 {@code ${userId}} 作字符串替换后残留裸 {@code #}
 * （MySQL 行注释符）截断 SQL → 鉴权查询必炸（先在缺陷，bf7eb2b 初始提交即有，
 * T8.63 push 前独立复审发现①）。修复=整个占位符进 raw 串
 * {@code ${r"#{userId}"}}（= T8.63 F-⑤ 正形）。
 *
 * <p>本测试以 SecuritySpiRenderer 同款 queryVars 直渲染模板，断言 MyBatis
 * 绑定占位符字面输出+12 变量全替换。
 *
 * @author gallop
 * @since 1.0.5
 */
class SecurityQueryMapperTemplatesRenderTest {

    private TemplateRenderer renderer;

    @BeforeEach
    void setUp() throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(TemplateContext.class, "/templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);

        // L4：注入文案字典（测试走缺省 zh，与 TemplateRendererTest 一致）
        TemplateMessages.apply(configuration, TemplateMessages.DEFAULT_LANGUAGE);

        renderer = new TemplateRenderer(configuration);
    }

    /** SecuritySpiRenderer 同款 queryVars（12 键；perm_code 取非默认值以证真插值）。 */
    private static Map<String, Object> queryVars() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("packageName", "com.spi.security");
        vars.put("userTableName", "t_user");
        vars.put("roleTableName", "t_role");
        vars.put("permissionTableName", "t_permission");
        vars.put("userRoleTableName", "t_user_role");
        vars.put("rolePermissionTableName", "t_role_permission");
        vars.put("roleCodeColumn", "role_code");
        vars.put("permissionCodeColumn", "perm_code");
        vars.put("userRoleUserFkColumn", "user_id");
        vars.put("userRoleRoleFkColumn", "role_id");
        vars.put("rolePermissionRoleFkColumn", "role_id");
        vars.put("rolePermissionPermFkColumn", "permission_id");
        return vars;
    }

    private String render() throws IOException {
        return renderer.render("project/security/SecurityQueryMapper.java.ftl", queryVars());
    }

    /** 字面计数（Pattern.quote 免正则元字符歧义）。 */
    private static int count(String haystack, String needle) {
        return haystack.split(Pattern.quote(needle), -1).length - 1;
    }

    /** FreeMarker 痕迹负断言：成品不得残留插值/指令/转义语法。 */
    private static void assertNoFreemarkerResidue(String result) {
        assertThat(result).doesNotContain("${r\"#{");
        assertThat(result).doesNotContain("<#");
        assertThat(result).doesNotContain("</#");
        assertThat(result).doesNotContain("${");
    }

    @Test
    void rendersBothQueriesWithLiteralMyBatisPlaceholder() throws IOException {
        String result = render();

        assertThat(result).contains("package com.spi.security.mapper;");
        assertThat(result).contains("public interface SecurityQueryMapper {");
        assertThat(result).contains("List<String> selectPermissionCodesByUserId(@Param(\"userId\") Long userId);");
        assertThat(result).contains("List<String> selectRoleCodesByUserId(@Param(\"userId\") Long userId);");

        // MyBatis 绑定占位符字面输出（修复前渲染产物为 #${userId}：# 后非 { 即非绑定，
        // ${userId} 被 MyBatis 字符串替换后残留裸 # 截断 SQL）
        assertThat(result).contains("WHERE u.id = #{userId} AND u.deleted = 0");
        assertThat(count(result, "WHERE u.id = #{userId} AND u.deleted = 0")).isEqualTo(2);
        assertThat(count(result, "#{userId}")).isEqualTo(2);

        // 缺陷形负锚：不得残留 #${（# 裸露在插值外的旧转义形残影）
        assertThat(result).doesNotContain("#${");

        assertNoFreemarkerResidue(result);
    }

    @Test
    void rendersAllTwelveQueryVarsIntoSql() throws IOException {
        String result = render();

        // 五表名+六列名全替换（queryVars 12 键逐一落 SQL）
        assertThat(result).contains("SELECT DISTINCT p.perm_code FROM t_user u");
        assertThat(result).contains("INNER JOIN t_user_role ur ON u.id = ur.user_id AND ur.deleted = 0");
        assertThat(result).contains("INNER JOIN t_role_permission rp ON ur.role_id = rp.role_id AND rp.deleted = 0");
        assertThat(result).contains("INNER JOIN t_permission p ON rp.permission_id = p.id AND p.deleted = 0");
        assertThat(result).contains("SELECT DISTINCT r.role_code FROM t_user u");
        assertThat(result).contains("INNER JOIN t_role r ON ur.role_id = r.id AND r.deleted = 0");

        assertNoFreemarkerResidue(result);
    }
}
