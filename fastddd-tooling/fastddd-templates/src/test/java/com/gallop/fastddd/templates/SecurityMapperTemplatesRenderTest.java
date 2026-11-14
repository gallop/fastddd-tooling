package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 安全 Mapper 模板「无 RBAC 正名表」渲染回归测试（T8.63 F-⑤ 盲区闭合）。
 *
 * <p>Sys*Mapper 三模板仅在设计中不存在 RBAC 正名表时渲染（existing.contains
 * skip 不触发）；历史 IT 恒以五张 RBAC 正名表走 skip 臂——模板从未真渲染。
 * 2026-10-14 162 实弹：Security 勾选+无 RBAC 表 → MyBatis {@code #{param}} 占位
 * 被 FreeMarker 数值插值解析 → InvalidReferenceException → 任务 FAILED
 * （task 2110182776456187906）。修复=模板内占位改 raw 字符串插值形
 * {@code ${r"#{param}"}}。
 *
 * <p>本测试以「无 table 元数据」上下文直渲染三模板（= 无 RBAC 表形态），
 * 断言 MyBatis 占位符字面输出。
 *
 * @author gallop
 * @since 1.0.4
 */
class SecurityMapperTemplatesRenderTest {

    private TemplateRenderer renderer;
    private TemplateContext context;

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

        // 「无 RBAC 正名表」形态：上下文不含任何 table 元数据，仅项目级变量
        context = TemplateContext.builder()
            .groupId("com.test")
            .artifactId("test-app")
            .version("1.0.0-SNAPSHOT")
            .packageName("com.test.app")
            .projectName("Test Application")
            .projectDescription("Test Application Description")
            .modelType(TemplateContext.ModelType.ANEMIC)
            .databaseType(TemplateContext.DatabaseType.MYSQL)
            .modules(new HashSet<>(Arrays.asList("web", "crud", "security")))
            .enableCQRS(false)
            .fastdddVersion("1.0.0-jdk21-SNAPSHOT")
            .javaVersion("21")
            .springBootVersion("3.5.16")
            .author("Test Author")
            .date("2026-10-14")
            .build()
            .calculatePackages();
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
    void rendersSysUserMapperWithLiteralMyBatisPlaceholders() throws IOException {
        String result = renderer.renderProjectTemplate("security/SysUserMapper.java.ftl", context);

        assertThat(result).contains("package com.test.app.mapper;");
        assertThat(result).contains("public interface SysUserMapper extends BaseRepository<SysUser>");
        assertThat(result).contains("SysUser selectByUsername(@Param(\"username\") String username);");

        // MyBatis 占位符字面输出（修复前此处抛 InvalidReferenceException: Failed at: #{username}）
        assertThat(result).contains("@Select(\"SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0\")");
        assertThat(result).contains("WHERE ur.user_id = #{userId} AND r.deleted = 0 AND ur.deleted = 0");
        assertThat(result).contains("WHERE email = #{email} AND deleted = 0");
        assertThat(result).contains("WHERE phone = #{phone} AND deleted = 0");
        assertThat(result).contains("WHERE status = #{status} AND deleted = 0");

        // 占位符 multiplicity：username=1 / userId=2 / email=1 / phone=1 / status=1 = 6 处
        assertThat(count(result, "#{username}")).isEqualTo(1);
        assertThat(count(result, "#{userId}")).isEqualTo(2);
        assertThat(count(result, "#{email}")).isEqualTo(1);
        assertThat(count(result, "#{phone}")).isEqualTo(1);
        assertThat(count(result, "#{status}")).isEqualTo(1);
        assertThat(result).doesNotContain("#{roleCode}");

        assertNoFreemarkerResidue(result);
    }

    @Test
    void rendersSysRoleMapperWithLiteralMyBatisPlaceholders() throws IOException {
        String result = renderer.renderProjectTemplate("security/SysRoleMapper.java.ftl", context);

        assertThat(result).contains("package com.test.app.mapper;");
        assertThat(result).contains("public interface SysRoleMapper extends BaseRepository<SysRole>");
        assertThat(result).contains("SysRole selectByRoleCode(@Param(\"roleCode\") String roleCode);");

        assertThat(result).contains("@Select(\"SELECT * FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0\")");
        assertThat(result).contains("WHERE ur.user_id = #{userId} AND r.deleted = 0 AND ur.deleted = 0");
        assertThat(result).contains("WHERE status = #{status} AND deleted = 0");
        assertThat(result).contains("WHERE role_id = #{roleId} AND deleted = 0");

        // 占位符 multiplicity：roleCode=1 / userId=1 / status=1 / roleId=1 = 4 处
        assertThat(count(result, "#{roleCode}")).isEqualTo(1);
        assertThat(count(result, "#{userId}")).isEqualTo(1);
        assertThat(count(result, "#{status}")).isEqualTo(1);
        assertThat(count(result, "#{roleId}")).isEqualTo(1);
        assertThat(result).doesNotContain("#{username}");

        assertNoFreemarkerResidue(result);
    }

    @Test
    void rendersSysPermissionMapperWithLiteralMyBatisPlaceholders() throws IOException {
        String result = renderer.renderProjectTemplate("security/SysPermissionMapper.java.ftl", context);

        assertThat(result).contains("package com.test.app.mapper;");
        assertThat(result).contains("public interface SysPermissionMapper extends BaseRepository<SysPermission>");
        assertThat(result).contains("SysPermission selectByPermissionCode(@Param(\"permissionCode\") String permissionCode);");

        assertThat(result).contains("@Select(\"SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0\")");
        assertThat(result).contains("WHERE rp.role_id = #{roleId} AND p.deleted = 0 AND rp.deleted = 0");
        assertThat(result).contains("WHERE resource_type = #{resourceType} AND deleted = 0");
        assertThat(result).contains("WHERE parent_id = #{parentId} AND deleted = 0");
        assertThat(result).contains("WHERE ur.user_id = #{userId} AND p.deleted = 0 AND rp.deleted = 0 AND ur.deleted = 0");

        // 占位符 multiplicity：permissionCode=1 / roleId=1 / resourceType=1 / parentId=1 / userId=1 = 5 处
        assertThat(count(result, "#{permissionCode}")).isEqualTo(1);
        assertThat(count(result, "#{roleId}")).isEqualTo(1);
        assertThat(count(result, "#{resourceType}")).isEqualTo(1);
        assertThat(count(result, "#{parentId}")).isEqualTo(1);
        assertThat(count(result, "#{userId}")).isEqualTo(1);

        assertNoFreemarkerResidue(result);
    }
}
