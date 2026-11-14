package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 安全 SPI 六模板 String 签名渲染回归测试（T8.83 F2' 断链闭合）。
 *
 * <p>框架 fast-ddd-security-starter 1.0.x SPI 真值：{@code SecurityIdentityService
 * .getUserInfo(String)}、{@code SecurityPermissionService.getPermissionsByUserId(String)/
 * getRolesByUserId(String)}、{@code SecurityUserDetails.userId} 与 {@code
 * SecurityUserInfo.id} 均为 String。历史六模板持 Long 签名/直传/强转形：tooling
 * 仓自身测试全绿（渲染面无断言）但生成项目编译必炸（T8.81 S3 十错/S6 联动面）。
 * 修复=Demo 典范形：SPI 方法 String 签名、mapper 调用点 {@code Long.valueOf(userId)}
 * （Mapper 保持 Long 不动）、builder 传值 {@code String.valueOf(...)}（T8.82R 对抗
 * 核验收口六模板清单）。
 *
 * <p>本测试直渲染六模板+MCB 双 pom，断言正锚（签名/适配点/builder 值）与负锚
 * （{@code (Long userId)}、未适配直传 {@code ByUserId(userId)}、显式强转
 * {@code (Long) user.getId()} 残影归零）。
 *
 * @author gallop
 * @since 1.0.6
 */
class SecuritySpiSignatureTemplatesRenderTest {

    private TemplateRenderer renderer;

    @BeforeEach
    void setUp() throws IOException {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_32);
        configuration.setClassForTemplateLoading(TemplateContext.class, "/templates");
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        TemplateMessages.apply(configuration, TemplateMessages.DEFAULT_LANGUAGE);
        renderer = new TemplateRenderer(configuration);
    }

    private static Map<String, Object> baseVars() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("packageName", "com.spi.security");
        return vars;
    }

    private String render(String template, Map<String, Object> vars) throws IOException {
        return renderer.render(template, vars);
    }

    /** SPI String 签名与 Long 适配负锚（六模板共性断言）。 */
    private static void assertNoLongSignatureResidue(String result) {
        assertThat(result).doesNotContain("(Long userId)");
        assertThat(result).doesNotContain("(Long) user.getId()");
        assertThat(result).doesNotContain("(Long) entity.getId()");
    }

    /** FreeMarker 痕迹负断言：成品不得残留插值/指令语法。 */
    private static void assertNoFreemarkerResidue(String result) {
        assertThat(result).doesNotContain("<#");
        assertThat(result).doesNotContain("</#");
        assertThat(result).doesNotContain("${");
    }

    @Test
    void rbacIdentityRendersStringSpiSignature() throws IOException {
        String result = render("project/security/RbacIdentityServiceImpl.java.ftl", baseVars());

        assertThat(result).contains("public SecurityUserInfo getUserInfo(String userId)");
        assertThat(result).contains("sysUserMapper.selectById(Long.valueOf(userId))");
        assertThat(result).contains(".userId(String.valueOf(user.getId()))");
        assertThat(result).contains(".id(String.valueOf(user.getId()))");
        assertThat(result).doesNotContain("ByUserId(userId)");
        assertNoLongSignatureResidue(result);
        assertNoFreemarkerResidue(result);
    }

    @Test
    void rbacPermissionRendersStringSpiSignatureWithLongAdaptation() throws IOException {
        String result = render("project/security/RbacPermissionServiceImpl.java.ftl", baseVars());

        assertThat(result).contains("public List<String> getPermissionsByUserId(String userId)");
        assertThat(result).contains("public List<String> getRolesByUserId(String userId)");
        assertThat(result).contains("sysUserMapper.selectPermissionsByUserId(Long.valueOf(userId))");
        assertThat(result).contains("sysRoleMapper.selectByUserId(Long.valueOf(userId))");
        assertThat(result).doesNotContain("ByUserId(userId)");
        assertNoLongSignatureResidue(result);
        assertNoFreemarkerResidue(result);
    }

    @Test
    void userDetailsRendersStringAuthoritiesParam() throws IOException {
        Map<String, Object> vars = baseVars();
        vars.put("author", "gallop");
        vars.put("date", "2026-10-20");
        String result = render("project/UserDetailsServiceImpl.java.ftl", vars);

        // SecurityUserDetails.userId 真值 String（框架 :25）——:55 调用点实参 String
        // 传 Long 形参即编译错，本断言锁私参 String 形
        assertThat(result).contains("private Collection<GrantedAuthority> getUserAuthorities(String userId)");
        assertThat(result).doesNotContain("getUserAuthorities(Long userId)");
        assertNoFreemarkerResidue(result);
    }

    @Test
    void customIdentityRendersStringSpiSignature() throws IOException {
        Map<String, Object> vars = baseVars();
        vars.put("entityClassName", "Member");
        vars.put("entityClassNameLower", "member");
        vars.put("usernameColumn", "username");
        vars.put("usernameGetter", "getUsername");
        vars.put("passwordGetter", "getPassword");
        vars.put("statusGetter", "getStatus");
        String result = render("project/security/CustomIdentityServiceImpl.java.ftl", vars);

        assertThat(result).contains("public SecurityUserInfo getUserInfo(String userId)");
        assertThat(result).contains("memberMapper.selectById(Long.valueOf(userId))");
        assertThat(result).contains(".userId(String.valueOf(entity.getId()))");
        assertThat(result).contains(".id(String.valueOf(entity.getId()))");
        assertThat(result).doesNotContain("ByUserId(userId)");
        assertNoLongSignatureResidue(result);
        assertNoFreemarkerResidue(result);
    }

    @Test
    void customPermissionKeepsLongMapperWithLongValueOfAdapter() throws IOException {
        String result = render("project/security/CustomPermissionServiceImpl.java.ftl", baseVars());

        assertThat(result).contains("public List<String> getPermissionsByUserId(String userId)");
        assertThat(result).contains("public List<String> getRolesByUserId(String userId)");
        // SecurityQueryMapper 接口保持 Long（模板自产接口，非框架 SPI）——适配点在调用侧
        assertThat(result).contains("securityQueryMapper.selectPermissionCodesByUserId(Long.valueOf(userId))");
        assertThat(result).contains("securityQueryMapper.selectRoleCodesByUserId(Long.valueOf(userId))");
        assertThat(result).doesNotContain("ByUserId(userId)");
        assertNoLongSignatureResidue(result);
        assertNoFreemarkerResidue(result);
    }

    @Test
    void appIdentityRendersStringSpiSignatureWithoutLongCasts() throws IOException {
        Map<String, Object> vars = baseVars();
        vars.put("userEntityPackage", "com.spi.mall.user");
        vars.put("userEntityName", "UserAccount");
        vars.put("userMapperPackage", "com.spi.mall.user");
        vars.put("userMapperName", "UserAccountMapper");
        vars.put("userMapperFieldName", "userAccountMapper");
        vars.put("appLabel", "mall-app");
        String result = render("project/security/multi-channel/AppIdentityService.java.ftl", vars);

        // implements SecurityIdentityService——getUserInfo(String) 不覆写即编译错（S3 同形）
        assertThat(result).contains("public SecurityUserInfo getUserInfo(String userId)");
        assertThat(result).contains("userAccountMapper.selectById(Long.valueOf(userId))");
        assertThat(result).contains(".id(String.valueOf(user.getId()))");
        assertThat(result).contains(".userId(String.valueOf(user.getId()))");
        assertThat(result).doesNotContain("ByUserId(userId)");
        assertNoLongSignatureResidue(result);
        assertNoFreemarkerResidue(result);
    }

    @Test
    void commonSharedPomCarriesWebAndCrudVersionless() throws IOException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("groupId", "com.spi.mall");
        vars.put("artifactId", "mall");
        vars.put("version", "1.0.0");
        String result = render("project/multi-channel/common-shared-pom.xml.ftl", vars);

        // F3' 断链：anemic 产物（Entity swagger/@TableName、Controller web）经 MCB 死配置
        // 路由落 common-shared——历史 pom 仅 common+model+lombok，web/crud 全缺
        assertThat(result).contains("<artifactId>fast-ddd-common</artifactId>");
        assertThat(result).contains("<artifactId>fast-ddd-model</artifactId>");
        assertThat(result).contains("<artifactId>fast-ddd-crud</artifactId>");
        assertThat(result).contains("<artifactId>fast-ddd-web</artifactId>");
        // versionless：依赖段零 <version>（parent dm :55-64 管版）——parent 块 ${version} 除外
        String deps = result.substring(result.indexOf("<dependencies>"));
        assertThat(deps).doesNotContain("<version>");
        assertNoFreemarkerResidue(result);
    }

    @Test
    void commonDomainPomCarriesWebVersionless() throws IOException {
        Map<String, Object> vars = new HashMap<>();
        vars.put("groupId", "com.spi.mall");
        vars.put("artifactId", "mall");
        vars.put("version", "1.0.0");
        vars.put("domainName", "order");
        String result = render("project/multi-channel/common-domain-pom.xml.ftl", vars);

        // T8.82R 遗漏④：domain 模块（表名 user/order/... extractDomain :540-551）同落
        // Controller+@Schema，历史 pom=common-shared+crud+lombok 无 web——与 shared 同批补齐
        assertThat(result).contains("<artifactId>common-shared</artifactId>");
        assertThat(result).contains("<artifactId>fast-ddd-crud</artifactId>");
        assertThat(result).contains("<artifactId>fast-ddd-web</artifactId>");
        String deps = result.substring(result.indexOf("<dependencies>"));
        assertThat(deps).doesNotContain("<version>");
        assertNoFreemarkerResidue(result);
    }

    @Test
    void sysMappersKeepLongParamsCoupledToSpiStringAdapters() throws IOException {
        // T8.111（复审 NOTE#1 补锚）：「Mapper 保持 Long」边界的渲染级锚——上测锚
        // SPI 调用点 Long.valueOf(userId)，仅当 Mapper 形参恒 Long 时生成项目编译
        // 成立；历史渲染面零锚（T8.63 测仅锚 #{} 字面与 String 形签名），后续若
        // cargo-cult F2' 清扫误改 Mapper Long→String，tooling 全绿而生成项目必炸
        Map<String, Object> sysVars = baseVars();
        sysVars.put("author", "gallop");
        sysVars.put("date", "2026-10-25");
        String userMapper = render("project/security/SysUserMapper.java.ftl", sysVars);
        String roleMapper = render("project/security/SysRoleMapper.java.ftl", sysVars);
        String permissionMapper = render("project/security/SysPermissionMapper.java.ftl", sysVars);

        assertThat(userMapper).contains("List<SysRole> selectRolesByUserId(@Param(\"userId\") Long userId);");
        assertThat(userMapper).contains("List<String> selectPermissionsByUserId(@Param(\"userId\") Long userId);");
        assertThat(roleMapper).contains("List<SysRole> selectByUserId(@Param(\"userId\") Long userId);");
        assertThat(roleMapper).contains("int countPermissionsByRoleId(@Param(\"roleId\") Long roleId);");
        assertThat(permissionMapper).contains("List<SysPermission> selectByRoleId(@Param(\"roleId\") Long roleId);");
        assertThat(permissionMapper).contains("List<SysPermission> selectByParentId(@Param(\"parentId\") Long parentId);");
        assertThat(permissionMapper).contains("List<SysPermission> selectByUserId(@Param(\"userId\") Long userId);");

        // SecurityQueryMapper（Custom 模板自产接口,非框架 SPI）同边界同锚——
        // CustomPermissionServiceImpl 调用点 Long.valueOf 已锚（上测）,接口侧同锚
        Map<String, Object> queryVars = baseVars();
        queryVars.put("userTableName", "t_user");
        queryVars.put("userRoleTableName", "t_user_role");
        queryVars.put("userRoleUserFkColumn", "user_id");
        queryVars.put("userRoleRoleFkColumn", "role_id");
        queryVars.put("rolePermissionTableName", "t_role_permission");
        queryVars.put("rolePermissionRoleFkColumn", "role_permission_id");
        queryVars.put("rolePermissionPermFkColumn", "permission_id");
        queryVars.put("permissionTableName", "t_permission");
        queryVars.put("permissionCodeColumn", "permission_code");
        queryVars.put("roleTableName", "t_role");
        queryVars.put("roleCodeColumn", "role_code");
        String queryMapper = render("project/security/SecurityQueryMapper.java.ftl", queryVars);
        assertThat(queryMapper).contains("List<String> selectPermissionCodesByUserId(@Param(\"userId\") Long userId);");
        assertThat(queryMapper).contains("List<String> selectRoleCodesByUserId(@Param(\"userId\") Long userId);");

        // 负锚：userId 形参不得 String 化（cargo-cult 清扫防线）;Mapper 系 Long
        // 源头不得自带适配（Long.valueOf 只属于 SPI 调用点,双适配即编译错）
        for (String result : new String[] {userMapper, roleMapper, permissionMapper, queryMapper}) {
            assertThat(result).doesNotContain("String userId");
            assertThat(result).doesNotContain("Long.valueOf");
            assertNoFreemarkerResidue(result);
        }
    }
}
