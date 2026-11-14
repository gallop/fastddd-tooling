package com.gallop.fastddd.codegen.constants;

import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 主键生成策略枚举
 * <p>定义代码生成器支持的主键生成策略类型，用于生成MyBatis Plus的@TableId注解。</p>
 *
 * <p>策略对比：</p>
 * <ul>
 *   <li>{@link #AUTO} - 数据库自增，适合中小型单体应用，简单可靠</li>
 *   <li>{@link #ASSIGN_ID} - 雪花算法，适合分布式/微服务场景，保证全局唯一</li>
 *   <li>{@link #INPUT} - 手动输入，用于业务主键如订单号</li>
 *   <li>{@link #NONE} - 不指定，使用MyBatis Plus全局配置</li>
 * </ul>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 * // 1. 在代码生成配置中使用
 * GeneratorConfig config = new GeneratorConfig();
 * config.setIdStrategy(IdStrategy.ASSIGN_ID.getCode());
 *
 * // 2. 获取对应的MyBatis Plus IdType
 * IdType idType = IdStrategy.ASSIGN_ID.getIdType();
 *
 * // 3. 在Entity模板中使用
 * @TableId(type = IdType.ASSIGN_ID)
 * private Long id;
 * }</pre>
 *
 * @author FastDDD Framework Team
 * @since 1.0.1
 * @see com.baomidou.mybatisplus.annotation.TableId
 * @see com.baomidou.mybatisplus.annotation.IdType
 * @deprecated SPLIT-003：跨产品的主键策略规范定义已上移至 Tooling 稳定契约
 * {@code com.gallop.fastddd.tooling.api.IdStrategy}（API 为唯一规范来源，不含 MyBatis-Plus 依赖）。
 * 本枚举保留供生成器内部 IdType 桥接使用，消费方应改用 API 枚举；完整消费面迁移见 SPLIT-004。
 */
@Deprecated
public enum IdStrategy {

    /**
     * 数据库自增
     * <p>生成的注解：@TableId(type = IdType.AUTO)</p>
     * <p>适用场景：中小型单体应用，单库单表</p>
     * <p>优点：简单可靠，数据库原生支持，索引友好</p>
     * <p>缺点：分库分表场景可能产生ID冲突</p>
     */
    AUTO("AUTO", "数据库自增", IdType.AUTO, "适合中小型单体应用"),

    /**
     * 雪花算法
     * <p>生成的注解：@TableId(type = IdType.ASSIGN_ID)</p>
     * <p>适用场景：分布式系统、微服务架构、分库分表</p>
     * <p>优点：全局唯一，趋势递增，高性能（客户端生成）</p>
     * <p>缺点：依赖系统时钟，ID长度较长</p>
     */
    ASSIGN_ID("ASSIGN_ID", "雪花算法", IdType.ASSIGN_ID, "适合微服务/分库分表场景"),

    /**
     * 手动输入
     * <p>生成的注解：@TableId(type = IdType.INPUT)</p>
     * <p>适用场景：特殊业务主键（如订单号、流水号）</p>
     * <p>优点：可自定义ID格式，包含业务含义</p>
     * <p>缺点：需要应用层保证唯一性</p>
     */
    INPUT("INPUT", "手动输入", IdType.INPUT, "特殊业务主键（如订单号）"),

    /**
     * 不指定
     * <p>生成的注解：@TableId</p>
     * <p>适用场景：使用MyBatis Plus全局配置</p>
     * <p>优点：灵活，可在全局配置中统一管理</p>
     * <p>缺点：配置分散，不够直观</p>
     */
    NONE("NONE", "不指定", null, "使用全局配置");

    /**
     * 策略代码（用于模板变量和API传递）
     */
    private final String code;

    /**
     * 策略描述（用于UI显示）
     */
    private final String description;

    /**
     * 对应的MyBatis Plus IdType
     */
    private final IdType idType;

    /**
     * 使用场景说明
     */
    private final String usageScenario;

    /**
     * 构造函数
     *
     * @param code 策略代码
     * @param description 策略描述
     * @param idType MyBatis Plus IdType
     * @param usageScenario 使用场景
     */
    IdStrategy(String code, String description, IdType idType, String usageScenario) {
        this.code = code;
        this.description = description;
        this.idType = idType;
        this.usageScenario = usageScenario;
    }

    /**
     * 获取策略代码
     *
     * @return 策略代码，如 "AUTO", "ASSIGN_ID"
     */
    public String getCode() {
        return code;
    }

    /**
     * 获取策略描述
     *
     * @return 策略描述，如 "数据库自增"
     */
    public String getDescription() {
        return description;
    }

    /**
     * 获取对应的MyBatis Plus IdType
     *
     * @return IdType枚举值，NONE策略返回null
     */
    public IdType getIdType() {
        return idType;
    }

    /**
     * 获取使用场景说明
     *
     * @return 使用场景描述
     */
    public String getUsageScenario() {
        return usageScenario;
    }

    /**
     * 根据代码获取枚举实例
     *
     * @param code 策略代码
     * @return 对应的枚举实例，如果未找到返回AUTO
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

    /**
     * 验证策略代码是否有效
     *
     * @param code 策略代码
     * @return 如果代码有效返回true，否则返回false
     */
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
