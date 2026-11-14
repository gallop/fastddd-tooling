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
 * AppIdentityService 同名类互斥先在锚（T8.111 复审 NOTE#4 补锚）。
 *
 * <p>SimpleIdentityService.java.ftl（maven-plugin initProject 简单模式脚手架,
 * InitProjectMojo :333）与 multi-channel/AppIdentityService.java.ftl（多端模式
 * 渲染）产物同名同类：均落 {@code ${packageName}.security} 包、类名均为
 * AppIdentityService、均注册 SecurityIdentityService Bean——两模式互斥是唯一
 * 防线（同一工程共现即同 FQCN 重复类编译错）。先在形态零锚：任一模板改名或
 * 漂移均无红面。本锚令碰撞形恒可见（双模板类声明互锁）,并顺锚 Simple 桩
 * String SPI 真值（F2' 六模板外第 7 面 SPI 实现,历史未锚——桩签名漂移即
 * 生成项目编译错而 tooling 全绿）。</p>
 *
 * @author gallop
 * @since 1.0.6
 */
class AppIdentityFqcnExclusionAnchorTest {

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

    /** FreeMarker 痕迹负断言：成品不得残留插值/指令语法。 */
    private static void assertNoFreemarkerResidue(String result) {
        assertThat(result).doesNotContain("<#");
        assertThat(result).doesNotContain("</#");
        assertThat(result).doesNotContain("${");
    }

    @Test
    void simpleIdentityRendersStringSpiStub() throws IOException {
        String result = render("project/security/SimpleIdentityService.java.ftl", baseVars());

        // Simple 桩同 implements SecurityIdentityService——SPI 真值 String（F2'
        // 六模板外第 7 面,历史零锚）;UnsupportedOperationException 桩形锚（提示
        // 文案走 i18n,throw 形恒在）
        assertThat(result).contains("public class AppIdentityService implements SecurityIdentityService {");
        assertThat(result).contains("public SecurityUserDetails loadUserByUsername(String username)");
        assertThat(result).contains("public SecurityUserInfo getUserInfo(String userId)");
        assertThat(result).contains("throw new UnsupportedOperationException(");
        assertThat(result).doesNotContain("(Long userId)");
        assertThat(result).doesNotContain("(Long username)");
        assertNoFreemarkerResidue(result);
    }

    @Test
    void simpleAndMultiChannelShareFqcnDemandingModeExclusion() throws IOException {
        // 同一 packageName 变量位——两模板产物 FQCN 恒同（<pkg>.security.AppIdentityService）
        Map<String, Object> vars = new HashMap<>();
        vars.put("packageName", "com.spi");
        String simple = render("project/security/SimpleIdentityService.java.ftl", vars);
        vars.put("userEntityPackage", "com.spi.mall.user");
        vars.put("userEntityName", "UserAccount");
        vars.put("userMapperPackage", "com.spi.mall.user");
        vars.put("userMapperName", "UserAccountMapper");
        vars.put("userMapperFieldName", "userAccountMapper");
        vars.put("appLabel", "mall-app");
        String multiChannel = render("project/security/multi-channel/AppIdentityService.java.ftl", vars);

        assertThat(simple).contains("package com.spi.security;");
        assertThat(multiChannel).contains("package com.spi.security;");
        // 同名类互斥先在：两模式产物同类名——同一工程共现即重复类编译错;
        // 任一侧改名本锚即红（改名须双侧同觉,互斥契约恒可见）
        assertThat(simple).contains("public class AppIdentityService implements SecurityIdentityService {");
        assertThat(multiChannel).contains("public class AppIdentityService implements SecurityIdentityService, CodeLoginIdentityService {");
        // 互斥面注记：multi-channel 侧多实现 CodeLoginIdentityService（非互换形）
        assertThat(simple).doesNotContain("CodeLoginIdentityService");
        assertNoFreemarkerResidue(simple);
        assertNoFreemarkerResidue(multiChannel);
    }
}
