package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.config.GeneratorConfig;
import com.gallop.fastddd.codegen.service.CodeGeneratorService;
import com.gallop.fastddd.codegen.service.DatabaseMetadataService;
import com.gallop.fastddd.common.model.TableMetadata;
import com.gallop.fastddd.tooling.api.DatabaseIntrospector;
import com.gallop.fastddd.tooling.api.GenerationException;
import com.gallop.fastddd.tooling.api.GenerationRequest;
import com.gallop.fastddd.tooling.api.GenerationResult;
import com.gallop.fastddd.tooling.api.IdStrategy;
import com.gallop.fastddd.tooling.api.ModelType;
import com.gallop.fastddd.tooling.api.ProjectGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * SPLIT-003 Phase B：Tooling 契约官方实现装配测试。
 *
 * <p>三重职责：</p>
 * <ol>
 *   <li><b>类型装配</b>——锁定 {@link DatabaseMetadataService} 与 {@link ToolingProjectGenerator}
 *       实现稳定契约（消费方可面向接口编程）。</li>
 *   <li><b>请求映射</b>——锁定 {@link GenerationRequest} → {@link GeneratorConfig} 的字段映射
 *       （含 ModelType 同构映射、IdStrategy 经 code 桥接）。</li>
 *   <li><b>结果封装 / 异常透传</b>——{@code Map} → {@link GenerationResult}；引擎异常 → {@link GenerationException}。</li>
 * </ol>
 *
 * <p>用 Mockito 桩 {@link CodeGeneratorService}，避免拉起完整 Spring 上下文与模板引擎，保证测试确定性。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class ToolingProjectGeneratorTest {

    @Test
    @DisplayName("类型装配：具体实现可赋值给 Tooling 契约接口")
    void implementationsAreAssignableToContracts() {
        assertThat(DatabaseIntrospector.class).isAssignableFrom(DatabaseMetadataService.class);
        assertThat(ProjectGenerator.class).isAssignableFrom(ToolingProjectGenerator.class);
    }

    @Test
    @DisplayName("generate：请求→配置映射 + 结果封装（ModelType/IdStrategy 桥接）")
    void generate_mapsRequestAndWrapsResult() {
        CodeGeneratorService engine = mock(CodeGeneratorService.class);
        Map<String, String> stubFiles = new LinkedHashMap<>();
        stubFiles.put("domain/User.java", "class User {}");
        stubFiles.put("service/UserService.java", "interface UserService {}");
        when(engine.generateCode(any(TableMetadata.class), anyString(), anyString(), any(GeneratorConfig.class)))
                .thenReturn(stubFiles);

        ToolingProjectGenerator generator = new ToolingProjectGenerator(engine);

        TableMetadata table = new TableMetadata();
        table.setTableName("t_user");
        GenerationRequest request = GenerationRequest.builder(table, "com.example.app")
                .moduleName("user")
                .modelType(ModelType.RICH)
                .enableCQRS(true)
                .useLombok(false)
                .useSwagger(false)
                .idStrategy(IdStrategy.ASSIGN_ID)
                .author("alice")
                .build();

        GenerationResult result = generator.generate(request);

        // 结果封装
        assertThat(result.getTableName()).isEqualTo("t_user");
        assertThat(result.getFileCount()).isEqualTo(2);
        assertThat(result.getFiles()).containsKeys("domain/User.java", "service/UserService.java");

        // 请求→配置映射
        ArgumentCaptor<GeneratorConfig> captor = ArgumentCaptor.forClass(GeneratorConfig.class);
        org.mockito.Mockito.verify(engine).generateCode(
                org.mockito.ArgumentMatchers.eq(table),
                org.mockito.ArgumentMatchers.eq("com.example.app"),
                org.mockito.ArgumentMatchers.eq("user"),
                captor.capture());
        GeneratorConfig passed = captor.getValue();
        assertThat(passed.getModelType()).isEqualTo(GeneratorConfig.ModelType.RICH);
        assertThat(passed.isEnableCQRS()).isTrue();
        assertThat(passed.isUseLombok()).isFalse();
        assertThat(passed.isUseSwagger()).isFalse();
        assertThat(passed.getIdStrategy()).isEqualTo("ASSIGN_ID"); // 经 code 桥接
        assertThat(passed.getAuthor()).isEqualTo("alice");
    }

    @Test
    @DisplayName("generate：引擎异常被包装为 GenerationException（异常契约稳定）")
    void generate_wrapsEngineFailure() {
        CodeGeneratorService engine = mock(CodeGeneratorService.class);
        when(engine.generateCode(any(TableMetadata.class),
                org.mockito.ArgumentMatchers.nullable(String.class),
                org.mockito.ArgumentMatchers.nullable(String.class),
                any(GeneratorConfig.class)))
                .thenThrow(new IllegalStateException("模板渲染失败"));

        ToolingProjectGenerator generator = new ToolingProjectGenerator(engine);

        TableMetadata table = new TableMetadata();
        table.setTableName("t_order");
        GenerationRequest request = GenerationRequest.builder(table, "com.example.app").build();

        assertThatThrownBy(() -> generator.generate(request))
                .isInstanceOf(GenerationException.class)
                .hasMessageContaining("t_order")
                .hasRootCauseInstanceOf(IllegalStateException.class);
    }
}
