package com.gallop.fastddd.templates;

import freemarker.template.Configuration;
import freemarker.template.TemplateExceptionHandler;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 旧调用约定的字典兜底回归（r2 最小修正）。
 * <p>
 * 背景：1.0.0 GA 的 {@link TemplateRenderer} 直接 {@code template.process(variables, writer)}，
 * 未经 {@link TemplateMessages#apply} 注入字典的既有用法（自建 {@link Configuration} 直接渲染
 * 含 {@code ${msg.*}} 的模板）因共享变量 {@code msg} 缺失而失败。
 * <p>
 * 修正语义：未配置字典时兜底默认中文；不覆盖变量中显式传入或共享变量已注入的英文/自定义字典；
 * 兜底只写本次渲染的局部副本，不回写 {@link Configuration}。
 *
 * @author gallop
 * @since 1.0.0
 */
class TemplateRendererDefaultMessagesTest {

    /**
     * 与 InitProjectMojo 同构的 Configuration（classpath:templates、UTF-8、RETHROW、关本地化查找），
     * 但不调用 TemplateMessages.apply——即旧调用约定。
     */
    private Configuration bareConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_32);
        cfg.setClassForTemplateLoading(TemplateContext.class, "/templates");
        cfg.setDefaultEncoding(StandardCharsets.UTF_8.name());
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLocalizedLookup(false);
        cfg.setLogTemplateExceptions(false);
        return cfg;
    }

    private Map<String, Object> applicationVariables() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("artifactId", "demo-app");
        vars.put("author", "regression");
        vars.put("className", "DemoApplication");
        vars.put("date", "2026-09-13");
        vars.put("mapperPackage", "com.example.demo.mapper");
        vars.put("packageName", "com.example.demo");
        vars.put("modelType", "ANEMIC");
        return vars;
    }

    /**
     * 旧用法（未注入字典）：渲染成功，兜底默认中文。
     */
    @Test
    void oldConventionRendersWithDefaultZhMessages() throws IOException {
        Configuration cfg = bareConfiguration();
        Map<String, Object> vars = applicationVariables();

        String out = new TemplateRenderer(cfg).render("project/Application.java.ftl", vars);

        assertThat(out).contains("package com.example.demo;");
        assertThat(out).contains("应用启动类");
        assertThat(out).doesNotContain("application bootstrap class");
    }

    /**
     * 旧用法（TemplateContext 路径）：兜底同样生效。
     */
    @Test
    void oldConventionRenderContextRendersWithDefaultZhMessages() throws IOException {
        Configuration cfg = bareConfiguration();
        TemplateContext context = TemplateContext.builder()
            .groupId("com.test")
            .artifactId("test-app")
            .version("1.0.0-SNAPSHOT")
            .packageName("com.test.app")
            .projectName("Test Application")
            .projectDescription("Test Application Description")
            .modelType(TemplateContext.ModelType.ANEMIC)
            .databaseType(TemplateContext.DatabaseType.MYSQL)
            .author("regression")
            .date("2026-09-13")
            .build();
        context.calculatePackages();

        String out = new TemplateRenderer(cfg).renderProjectTemplate("Application.java.ftl", context);

        assertThat(out).contains("应用启动类");
    }

    /**
     * 入口层注入英文：兜底不覆盖英文；渲染后共享变量仍为英文（未被兜底改写）。
     */
    @Test
    void englishMessagesNotOverriddenByFallback() throws IOException {
        Configuration cfg = bareConfiguration();
        TemplateMessages.apply(cfg, "en");

        String out = new TemplateRenderer(cfg).render("project/Application.java.ftl", applicationVariables());

        assertThat(out).contains("application bootstrap class");
        assertThat(out).doesNotContain("应用启动类");
        // 共享变量未被兜底覆盖（仍是注入的模型对象而非缺省中文字典）
        assertThat(cfg.getSharedVariable(TemplateMessages.SHARED_VARIABLE_NAME)).isNotNull();
    }

    /**
     * 调用方在变量中显式传入自定义字典：自定义优先（即使共享变量另有注入）。
     */
    @Test
    void customMessagesInVariablesWin() throws IOException {
        Configuration cfg = bareConfiguration();
        TemplateMessages.apply(cfg, "en");

        Map<String, Object> vars = applicationVariables();
        Map<String, Object> customMsg = new LinkedHashMap<>();
        Map<String, Object> customProj = new LinkedHashMap<>();
        Map<String, Object> customApplication = new LinkedHashMap<>();
        customApplication.put("classDesc", "CUSTOM-BOOTSTRAP-CLASS");
        customProj.put("application", customApplication);
        customMsg.put("proj", customProj);
        vars.put("msg", customMsg);

        String out = new TemplateRenderer(cfg).render("project/Application.java.ftl", vars);

        assertThat(out).contains("CUSTOM-BOOTSTRAP-CLASS");
        assertThat(out).doesNotContain("application bootstrap class");
        assertThat(out).doesNotContain("应用启动类");
        // 自定义字典为调用方所有，兜底不得修改传入变量
        assertThat(vars.get("msg")).isSameAs(customMsg);
    }

    /**
     * 共享变量上的自定义字典（非 TemplateMessages 注入路径）：同样不被兜底覆盖。
     */
    @Test
    void customSharedVariableMessagesNotOverridden() throws IOException, freemarker.template.TemplateModelException {
        Configuration cfg = bareConfiguration();
        Map<String, Object> customMsg = new LinkedHashMap<>();
        Map<String, Object> customProj = new LinkedHashMap<>();
        Map<String, Object> customApplication = new LinkedHashMap<>();
        customApplication.put("classDesc", "SHARED-CUSTOM-CLASS");
        customProj.put("application", customApplication);
        customMsg.put("proj", customProj);
        cfg.setSharedVariable(TemplateMessages.SHARED_VARIABLE_NAME, customMsg);

        String out = new TemplateRenderer(cfg).render("project/Application.java.ftl", applicationVariables());

        assertThat(out).contains("SHARED-CUSTOM-CLASS");
        assertThat(out).doesNotContain("应用启动类");
    }

    /**
     * 共享 Configuration 无语言污染：兜底渲染后配置上仍无 msg 共享变量；
     * 随后入口层注入英文，旧用法渲染得到英文（未被先前兜底固化为中文）。
     */
    @Test
    void fallbackDoesNotPolluteSharedConfiguration() throws IOException {
        Configuration cfg = bareConfiguration();
        TemplateRenderer renderer = new TemplateRenderer(cfg);
        Map<String, Object> vars = applicationVariables();

        // 第一次：兜底中文
        String zhOut = renderer.render("project/Application.java.ftl", vars);
        assertThat(zhOut).contains("应用启动类");
        // 兜底未回写共享变量
        assertThat(cfg.getSharedVariable(TemplateMessages.SHARED_VARIABLE_NAME)).isNull();

        // 第二次：入口层注入英文后，同一 Configuration 上旧用法得到英文
        TemplateMessages.apply(cfg, "en");
        String enOut = renderer.render("project/Application.java.ftl", applicationVariables());
        assertThat(enOut).contains("application bootstrap class");
        assertThat(enOut).doesNotContain("应用启动类");
    }

    /**
     * renderFromString 同样兜底：内联字符串模板引用 msg 时默认中文。
     */
    @Test
    void renderFromStringFallsBackToDefaultZh() throws IOException {
        Configuration cfg = bareConfiguration();
        Map<String, Object> vars = new HashMap<>();

        String out = new TemplateRenderer(cfg).renderFromString("desc=${msg.proj.application.classDesc}", vars);

        assertThat(out).isEqualTo("desc=应用启动类");
        assertThat(cfg.getSharedVariable(TemplateMessages.SHARED_VARIABLE_NAME)).isNull();
    }
}
