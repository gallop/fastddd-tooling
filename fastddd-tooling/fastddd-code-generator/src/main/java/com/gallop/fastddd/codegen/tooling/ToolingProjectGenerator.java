package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.config.GeneratorConfig;
import com.gallop.fastddd.codegen.service.CodeGeneratorService;
import com.gallop.fastddd.tooling.api.GenerationException;
import com.gallop.fastddd.tooling.api.GenerationRequest;
import com.gallop.fastddd.tooling.api.GenerationResult;
import com.gallop.fastddd.tooling.api.ModelType;
import com.gallop.fastddd.tooling.api.ProjectGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Tooling 稳定契约 {@link ProjectGenerator} 的官方适配实现（SPLIT-003 Phase B）。
 *
 * <p>本类是<strong>薄适配层</strong>：将契约侧不可变 {@link GenerationRequest} 映射为生成引擎内部的
 * {@link GeneratorConfig}，委托既有 {@link CodeGeneratorService#generateCode} 完成实际生成，
 * 再把 {@code Map<String,String>} 结果封装为不可变 {@link GenerationResult}。</p>
 *
 * <p>设计约束：</p>
 * <ul>
 *   <li>不搬运 {@code GeneratorConfig} 的输出写入关切（outputDir / overwrite / generate* 开关）——
 *       SPLIT-003 禁止项"不把现有 GeneratorConfig 全量原样搬入 API"。这些仍属消费方实现层关切。</li>
 *   <li>主键策略经 {@code code} 字符串桥接（契约 {@link com.gallop.fastddd.tooling.api.IdStrategy} 与
 *       生成器内部 {@code codegen.constants.IdStrategy} 的 code 值一致：AUTO/ASSIGN_ID/INPUT/NONE），
 *       MyBatis-Plus {@code IdType} 解析保留在生成器实现内部，不出现在 API。</li>
 *   <li> {@link ModelType} 映射为 {@link GeneratorConfig.ModelType}（两枚举语义同构）。</li>
 * </ul>
 *
 * <p>消费方应注入 {@link ProjectGenerator}（契约）而非本具体类，从而与生成器内部包解耦。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolingProjectGenerator implements ProjectGenerator {

    private final CodeGeneratorService codeGeneratorService;

    @Override
    public GenerationResult generate(GenerationRequest request) {
        if (request == null) {
            throw new GenerationException("GenerationRequest 不能为空", null);
        }

        GeneratorConfig config = toGeneratorConfig(request);
        String tableName = request.getTable().getTableName();

        try {
            Map<String, String> files = codeGeneratorService.generateCode(
                    request.getTable(),
                    request.getPackageName(),
                    request.getModuleName(),
                    config);

            GenerationResult.Builder builder = GenerationResult.builder(tableName);
            if (files != null) {
                files.forEach(builder::addFile);
            }
            return builder.build();
        } catch (RuntimeException e) {
            // 透传 GenerationException 自身，避免重复包装
            if (e instanceof GenerationException) {
                throw e;
            }
            throw new GenerationException("代码生成失败: " + tableName, e);
        }
    }

    /**
     * 将契约请求映射为生成引擎内部配置（仅生成语义字段，不含输出写入关切）。
     */
    private static GeneratorConfig toGeneratorConfig(GenerationRequest request) {
        GeneratorConfig config = new GeneratorConfig();
        config.setModelType(request.getModelType() == ModelType.RICH
                ? GeneratorConfig.ModelType.RICH
                : GeneratorConfig.ModelType.ANEMIC);
        config.setEnableCQRS(request.isEnableCQRS());
        config.setUseLombok(request.isUseLombok());
        config.setUseSwagger(request.isUseSwagger());
        // 主键策略经 code 桥接（契约与内部枚举 code 值一致）
        config.setIdStrategy(request.getIdStrategy().getCode());
        // GEN-002：写模式经 code 桥接（默认 READ_ONLY，DTO 为 opt-in 写端点）
        config.setWriteMode(request.getWriteMode().getCode());
        config.setAuthor(request.getAuthor());
        return config;
    }
}
