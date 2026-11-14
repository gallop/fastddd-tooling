package com.gallop.fastddd.codegen.config;

import com.gallop.fastddd.codegen.constants.IdStrategy;
import lombok.Data;

/**
 * 代码生成器配置
 *
 * @author gallop
 * @date 2025-01-10
 */
@Data
public class GeneratorConfig {

    /**
     * 模型类型
     */
    private ModelType modelType = ModelType.ANEMIC;

    /**
     * 是否启用 CQRS
     */
    private boolean enableCQRS = false;

    /**
     * 主键生成策略
     * <p>支持的策略类型：</p>
     * <ul>
     *   <li>{@code AUTO} - 数据库自增，适合中小型单体应用（默认）</li>
     *   <li>{@code ASSIGN_ID} - 雪花算法，适合分布式/微服务场景</li>
     *   <li>{@code INPUT} - 手动输入，用于业务主键如订单号</li>
     *   <li>{@code NONE} - 不指定，使用MyBatis Plus全局配置</li>
     * </ul>
     *
     * @see IdStrategy
     * @since 1.0.1
     */
    private String idStrategy = IdStrategy.AUTO.getCode();

    /**
     * 写端点生成模式（GEN-002）。
     * <p>默认 {@code READ_ONLY}（只读安全姿态）；{@code DTO} 生成 DTO 写请求体 + 写端点。</p>
     *
     * @see com.gallop.fastddd.tooling.api.WriteMode
     * @since 1.0.0-jdk21
     */
    private String writeMode = "READ_ONLY";

    /**
     * 输出目录
     */
    private String outputDir;
    
    /**
     * 基础包名
     */
    private String basePackage;
    
    /**
     * 模块名称
     */
    private String moduleName;
    
    /**
     * 作者
     */
    private String author = "FastDDD";
    
    /**
     * 是否生成实体类
     */
    private boolean generateEntity = true;
    
    /**
     * 是否生成Repository
     */
    private boolean generateRepository = true;
    
    /**
     * 是否生成Service
     */
    private boolean generateService = true;
    
    /**
     * 是否生成Controller
     */
    private boolean generateController = true;
    
    /**
     * 是否生成DTO
     */
    private boolean generateDto = true;
    
    /**
     * 是否生成Mapper
     */
    private boolean generateMapper = false;
    
    /**
     * 是否覆盖已存在的文件
     */
    private boolean overwrite = false;
    
    /**
     * 是否使用Lombok
     */
    private boolean useLombok = true;
    
    /**
     * 是否使用Swagger
     */
    private boolean useSwagger = true;
    
    /**
     * 是否使用JPA
     */
    private boolean useJpa = true;
    
    /**
     * 是否使用Validation
     */
    private boolean useValidation = true;

    /**
     * 模型类型枚举
     *
     * @deprecated SPLIT-003：跨产品的模型类型规范定义已上移至 Tooling 稳定契约
     * {@code com.gallop.fastddd.tooling.api.ModelType}（API 为唯一规范来源）。本嵌套枚举保留供
     * 生成器内部使用，消费方应改用 API 枚举；完整消费面迁移见 SPLIT-004。
     */
    @Deprecated
    public enum ModelType {
        /**
         * 贫血模型（默认）
         * 生成：PO + Service + Controller
         */
        ANEMIC,

        /**
         * 充血模型
         * 生成：Entity + ApplicationService + Repository + CQRS
         */
        RICH
    }
}