package com.gallop.fastddd.tooling.api;

/**
 * 领域模型风格（SPLIT-003 规范归属）。
 *
 * <p>历史上同一 ANEMIC/RICH 概念重复定义于 4 处：
 * {@code codegen.GeneratorConfig.ModelType}、{@code templates.TemplateContext.ModelType}、
 * {@code studio.UnifiedProjectRequest.ModelType}、{@code common.enums.DomainModelType}。
 * SPLIT-003 将 Tooling 侧的规范归属收敛到本类型；各消费点逐步以本类型为单一事实源。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public enum ModelType {

    /** 贫血模型（默认）：PO + Service + Controller，传统分层架构。 */
    ANEMIC("贫血模型", "PO + Service + Controller，传统分层架构"),

    /** 充血模型：AggregateRoot + ApplicationService + Repository + CQRS，DDD 风格。 */
    RICH("充血模型", "AggregateRoot + ApplicationService + Repository + CQRS，DDD 风格");

    private final String displayName;
    private final String description;

    ModelType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 按名称（大小写不敏感）解析；非法/空值返回 {@link #ANEMIC}（默认）。
     */
    public static ModelType fromCode(String code) {
        if (code == null || code.isBlank()) {
            return ANEMIC;
        }
        for (ModelType type : values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return ANEMIC;
    }

    public static boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        for (ModelType type : values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
