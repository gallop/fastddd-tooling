package com.gallop.fastddd.tooling.api;

import com.gallop.fastddd.common.model.TableMetadata;

import java.util.Objects;

/**
 * 代码生成请求（SPLIT-003 结构化契约）。
 *
 * <p>取代历史 {@code CodeGeneratorService.generateCode(TableMetadata, String, String, GeneratorConfig)}
 * 的零散形参与 {@code GeneratorConfig} 大杂烩。仅承载<strong>生成语义</strong>所需的字段（表元数据、
 * 包/模块、模型风格、开关、主键策略、作者），<strong>不</strong>搬运 {@code GeneratorConfig} 的输出写入
 * 关切（outputDir / overwrite / generateEntity 等留给消费方实现）——符合 SPLIT-003 禁止项
 * "不把现有 GeneratorConfig 全量原样搬入 API"。</p>
 *
 * <p>不可变值对象，经 {@link Builder} 构造。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public final class GenerationRequest {

    private final TableMetadata table;
    private final String packageName;
    private final String moduleName;
    private final ModelType modelType;
    private final boolean enableCQRS;
    private final boolean useLombok;
    private final boolean useSwagger;
    private final IdStrategy idStrategy;
    private final WriteMode writeMode;
    private final String author;

    private GenerationRequest(Builder b) {
        this.table = Objects.requireNonNull(b.table, "table 元数据不能为空");
        this.packageName = Objects.requireNonNull(b.packageName, "packageName 不能为空");
        this.moduleName = b.moduleName;
        this.modelType = b.modelType != null ? b.modelType : ModelType.ANEMIC;
        this.enableCQRS = b.enableCQRS;
        this.useLombok = b.useLombok;
        this.useSwagger = b.useSwagger;
        this.idStrategy = b.idStrategy != null ? b.idStrategy : IdStrategy.AUTO;
        // GEN-002：默认只读（安全姿态），DTO 写为 opt-in
        this.writeMode = b.writeMode != null ? b.writeMode : WriteMode.READ_ONLY;
        this.author = b.author != null ? b.author : "FastDDD";
    }

    public TableMetadata getTable() { return table; }
    public String getPackageName() { return packageName; }
    public String getModuleName() { return moduleName; }
    public ModelType getModelType() { return modelType; }
    public boolean isEnableCQRS() { return enableCQRS; }
    public boolean isUseLombok() { return useLombok; }
    public boolean isUseSwagger() { return useSwagger; }
    public IdStrategy getIdStrategy() { return idStrategy; }
    public WriteMode getWriteMode() { return writeMode; }
    public String getAuthor() { return author; }

    public static Builder builder(TableMetadata table, String packageName) {
        return new Builder(table, packageName);
    }

    /** 可变构建器，{@link #build()} 产出不可变 {@link GenerationRequest}。 */
    public static final class Builder {
        private final TableMetadata table;
        private final String packageName;
        private String moduleName;
        private ModelType modelType;
        private boolean enableCQRS;
        private boolean useLombok;
        private boolean useSwagger;
        private IdStrategy idStrategy;
        private WriteMode writeMode;
        private String author;

        private Builder(TableMetadata table, String packageName) {
            this.table = table;
            this.packageName = packageName;
        }

        public Builder moduleName(String moduleName) { this.moduleName = moduleName; return this; }
        public Builder modelType(ModelType modelType) { this.modelType = modelType; return this; }
        public Builder enableCQRS(boolean enableCQRS) { this.enableCQRS = enableCQRS; return this; }
        public Builder useLombok(boolean useLombok) { this.useLombok = useLombok; return this; }
        public Builder useSwagger(boolean useSwagger) { this.useSwagger = useSwagger; return this; }
        public Builder idStrategy(IdStrategy idStrategy) { this.idStrategy = idStrategy; return this; }
        public Builder writeMode(WriteMode writeMode) { this.writeMode = writeMode; return this; }
        public Builder author(String author) { this.author = author; return this; }

        public GenerationRequest build() { return new GenerationRequest(this); }
    }
}
