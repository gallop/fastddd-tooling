package com.gallop.fastddd.tooling.api;

/**
 * 主键生成策略（SPLIT-003 规范归属，去 MyBatis-Plus 依赖）。
 *
 * <p>取代历史 {@code codegen.constants.IdStrategy}（其构造引用 {@code com.baomidou.mybatisplus.annotation.IdType}，
 * 与 Tooling API "纯 Java 契约"性质冲突）。本类型仅保留与 ORM 无关的策略语义（代码/描述/使用场景）；
 * 策略到具体 ORM {@code IdType} 的映射由生成器实现侧（{@code fastddd-code-generator}）维护，
 * 不泄漏进 API 二进制面。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public enum IdStrategy {

    /** 数据库自增，适合中小型单体应用。 */
    AUTO("AUTO", "数据库自增", "适合中小型单体应用"),

    /** 雪花算法，适合分布式/微服务/分库分表场景。 */
    ASSIGN_ID("ASSIGN_ID", "雪花算法", "适合微服务/分库分表场景"),

    /** 手动输入，用于业务主键（如订单号、流水号）。 */
    INPUT("INPUT", "手动输入", "特殊业务主键（如订单号）"),

    /** 不指定，使用 ORM 全局配置。 */
    NONE("NONE", "不指定", "使用全局配置");

    private final String code;
    private final String description;
    private final String usageScenario;

    IdStrategy(String code, String description, String usageScenario) {
        this.code = code;
        this.description = description;
        this.usageScenario = usageScenario;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getUsageScenario() {
        return usageScenario;
    }

    /**
     * 按代码解析；空/非法返回 {@link #AUTO}（默认）。
     */
    public static IdStrategy fromCode(String code) {
        if (code == null || code.isBlank()) {
            return AUTO;
        }
        for (IdStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return strategy;
            }
        }
        return AUTO;
    }

    public static boolean isValid(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }
        for (IdStrategy strategy : values()) {
            if (strategy.code.equals(code)) {
                return true;
            }
        }
        return false;
    }
}
