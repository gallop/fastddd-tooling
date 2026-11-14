package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.CodeGeneratorAutoConfiguration;
import com.gallop.fastddd.tooling.api.DatabaseIntrospectorFactory;
import com.gallop.fastddd.tooling.api.ProjectGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SPLIT-004 步骤 5/6：验证 codegen 自动配置经 Spring Boot 3 标准机制自注册，并发布 Tooling 契约 bean。
 *
 * <p>历史注册走 {@code spring.factories} 的 {@code EnableAutoConfiguration} 键——Spring Boot 3.0+ 已停止
 * 从该键加载自动配置，故 Studio 此前必须显式 {@code @Import(CodeGeneratorAutoConfiguration.class)} 才能拿到 bean。
 * 本任务将注册迁至 {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}
 * （全仓其余 14 个模块的统一做法），Studio 即可移除显式 @Import、仅按契约类型注入。</p>
 *
 * <p>本测试双重锁定：①imports 文件存在且登记了本自动配置（防止文件名/内容拼写错误导致的静默失效）；
 * ②加载该自动配置后，{@link ProjectGenerator} 与 {@link DatabaseIntrospectorFactory} 两个契约 bean 实际发布
 * （Studio 的两个注入点可解析）。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class CodegenAutoConfigurationRegistrationTest {

    private static final String IMPORTS_RESOURCE =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CodeGeneratorAutoConfiguration.class));

    /**
     * imports 文件存在、且登记 {@link CodeGeneratorAutoConfiguration}——证明标准自注册机制就位
     * （本断言读类路径资源，确认文件已随构建打包进 {@code target/classes}）。
     *
     * <p>注意：不在此断言旧 {@code spring.factories} 内容——该文件已从源删除（git 证据），
     * 且 {@code getResource} 会扫描整条类路径，本地 m2 中历史安装的旧 codegen jar 残留会干扰判定，
     * 故仅以 imports 文件存在 + bean 实际发布作为迁移完成的可执行证据。</p>
     */
    @Test
    void autoConfigurationIsRegisteredViaBoot3ImportsFile() throws IOException {
        URL imports = getClass().getClassLoader().getResource(IMPORTS_RESOURCE);
        assertThat(imports)
                .as("Boot3 AutoConfiguration.imports 必须存在（codegen 已迁离 spring.factories）")
                .isNotNull();

        List<String> lines = Arrays.stream(
                        new String(imports.openStream().readAllBytes(), StandardCharsets.UTF_8).split("\\R"))
                .map(String::trim)
                .filter(s -> !s.isEmpty() && !s.startsWith("#"))
                .collect(Collectors.toList());
        assertThat(lines)
                .as("imports 文件须登记 CodeGeneratorAutoConfiguration")
                .contains(CodeGeneratorAutoConfiguration.class.getName());
    }

    /**
     * 加载自动配置后，两个 Tooling 契约 bean 实际发布——Studio 按 {@link ProjectGenerator}/
     * {@link DatabaseIntrospectorFactory} 注入可解析，无需命名具体实现。
     */
    @Test
    void autoConfigurationPublishesToolingContractBeans() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(ProjectGenerator.class);
            assertThat(context).hasSingleBean(DatabaseIntrospectorFactory.class);
        });
    }

    /**
     * 发布的 bean 即官方适配实现（ToolingProjectGenerator / CodegenDatabaseIntrospectorFactory）——
     * 锁定契约→实现的装配路径，替换实现需显式覆盖 bean 定义。
     */
    @Test
    void publishedBeansAreTheCodegenAdapters() {
        runner.run(context -> {
            assertThat(context.getBean(ProjectGenerator.class))
                    .isInstanceOf(ToolingProjectGenerator.class);
            assertThat(context.getBean(DatabaseIntrospectorFactory.class))
                    .isInstanceOf(CodegenDatabaseIntrospectorFactory.class);
        });
    }
}
