package com.gallop.fastddd.tooling.api;

/**
 * 代码生成主入口契约（SPLIT-003）。
 *
 * <p>消费者（Studio、Maven Plugin、未来扩展）经此接口触发单表代码生成，而非直接绑定生成器内部
 * 的 {@code CodeGeneratorService} 具体类。实现由 {@code fastddd-code-generator} 提供（或经适配层）。</p>
 *
 * <p>契约语义：{@link #generate(GenerationRequest)} 对同一 {@link GenerationRequest} 返回确定的
 * {@link GenerationResult}（路径 → 内容映射）；不写入磁盘（持久化由消费方决定）。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public interface ProjectGenerator {

    /**
     * 按请求生成单表代码文件。
     *
     * @param request 生成请求（表元数据、包/模块、模型风格、开关、主键策略、作者）
     * @return 结构化生成结果（相对路径 → 文件内容）
     * @throws NullPointerException request 或其必填字段为空
     * @throws com.gallop.fastddd.tooling.api.GenerationException 生成过程中的语义错误
     */
    GenerationResult generate(GenerationRequest request);
}
