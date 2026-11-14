package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.CodeGeneratorAutoConfiguration;
import com.gallop.fastddd.common.model.ColumnMetadata;
import com.gallop.fastddd.common.model.TableMetadata;
import com.gallop.fastddd.tooling.api.GenerationRequest;
import com.gallop.fastddd.tooling.api.GenerationResult;
import com.gallop.fastddd.tooling.api.ModelType;
import com.gallop.fastddd.tooling.api.ProjectGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SPLIT-004 步骤 7：经 {@link ProjectGenerator} 契约的端到端生成集成测试。
 *
 * <p>既有测试（{@code ToolingProjectGeneratorTest} 用 mock、{@code CodeGeneratorServiceTest} mock
 * 模板渲染）均未走真实模板渲染。本测试用真实自动配置装配（{@link CodeGeneratorAutoConfiguration} 经
 * {@code AutoConfigurations.of} 加载，发布真实 {@code TemplateEngineConfig}/{@code CodeGeneratorService}/
 * {@code ToolingProjectGenerator}，FreeMarker 从 classpath 读取真实模板），按 Studio 同款方式构造
 * {@link GenerationRequest}，断言真实生成的代码层与内容。</p>
 *
 * <p>同时固化 §16.25 的行为保留修正：{@code useLombok} 开关经契约透传到模板——开启时实体含 {@code @Data}，
 * 关闭时不含。若未来有人误把 GenerationRequest 的 useLombok 默认（false）当成 Studio 默认，本测试会失败。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class ProjectGeneratorIntegrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CodeGeneratorAutoConfiguration.class));

    /** 构造与 Studio 一致的最小 user 表元数据。 */
    private static TableMetadata userTable() {
        TableMetadata table = new TableMetadata();
        table.setTableName("t_user");
        table.setTableComment("用户表");
        table.setEntityName("User");

        List<ColumnMetadata> cols = new ArrayList<>();
        ColumnMetadata id = new ColumnMetadata();
        id.setColumnName("id");
        id.setFieldName("id");
        id.setJavaType("Long");
        id.setPrimaryKey(true);
        id.setColumnComment("主键");
        cols.add(id);

        ColumnMetadata username = new ColumnMetadata();
        username.setColumnName("username");
        username.setFieldName("username");
        username.setJavaType("String");
        username.setColumnComment("用户名");
        cols.add(username);

        table.setColumns(cols);
        return table;
    }

    private static GenerationRequest request(TableMetadata table, boolean useLombok) {
        return GenerationRequest.builder(table, "com.example.demo")
                .moduleName("demo")
                .modelType(ModelType.ANEMIC)
                .useLombok(useLombok)
                .useSwagger(true)
                .author("StudioIntegration")
                .build();
    }

    /**
     * ANEMIC 生成经契约产出全部持久化/服务/接口层，路径与 {@code CodeGeneratorService} 既定布局一致。
     */
    @Test
    void anemicGenerationProducesAllLayersThroughContract() {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            GenerationResult result = generator.generate(request(userTable(), true));
            Map<String, String> files = result.getFiles();

            assertThat(files).isNotEmpty();
            String base = "backend/src/main/java/com/example/demo/";
            assertThat(files).containsKeys(
                    base + "entity/User.java",
                    base + "mapper/UserMapper.java",
                    base + "service/UserService.java",
                    base + "service/impl/UserServiceImpl.java",
                    base + "controller/UserController.java");

            String entity = files.get(base + "entity/User.java");
            assertThat(entity).contains("public class User extends BasePO");
        });
    }

    /**
     * useLombok 经契约透传到模板。Entity.ftl 以 {@code @Getter/@Setter} 为基线（恒渲染），
     * useLombok 仅额外门控 {@code @Builder}（含其 import）。故以 {@code @Builder} 的有/无作为开关透传断言——
     * 固化 §16.25 行为保留修正（GeneratorConfig 默认 true，GenerationRequest 默认 false，Studio 显式补 true）。
     */
    @Test
    void useLombokFlagPropagatesToRenderedEntity() {
        runner.run(context -> {
            ProjectGenerator generator = context.getBean(ProjectGenerator.class);
            String base = "backend/src/main/java/com/example/demo/entity/User.java";

            Map<String, String> withLombok = generator.generate(request(userTable(), true)).getFiles();
            assertThat(withLombok.get(base))
                    .as("useLombok=true 应渲染 @Builder").contains("@Builder");

            Map<String, String> withoutLombok = generator.generate(request(userTable(), false)).getFiles();
            assertThat(withoutLombok.get(base))
                    .as("useLombok=false 不应渲染 @Builder").doesNotContain("@Builder");
        });
    }
}
