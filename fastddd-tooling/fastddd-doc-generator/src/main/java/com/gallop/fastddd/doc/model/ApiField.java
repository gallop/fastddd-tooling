package com.gallop.fastddd.doc.model;

import lombok.Data;

/**
 * API字段模型
 * 表示DTO对象中的字段信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Data
public class ApiField {

    /**
     * 字段名
     */
    private String name;

    /**
     * 字段类型
     */
    private String type;

    /**
     * 完整类型名（含包名）
     */
    private String qualifiedTypeName;

    /**
     * 字段描述
     */
    private String description;

    /**
     * 是否为数组或集合
     */
    private boolean isArray;

    /**
     * 数组/集合的元素类型
     */
    private String elementType;

    /**
     * 是否为泛型
     */
    private boolean isGeneric;

    /**
     * 泛型类型参数
     */
    private String genericType;

    /**
     * 是否必需（来自验证注解如@NotNull）
     */
    private boolean required;

    /**
     * 示例值
     */
    private String example;

    /**
     * 最小值（数字类型）
     */
    private String minValue;

    /**
     * 最大值（数字类型）
     */
    private String maxValue;

    /**
     * 最小长度（字符串类型）
     */
    private Integer minLength;

    /**
     * 最大长度（字符串类型）
     */
    private Integer maxLength;

    /**
     * 正则表达式验证
     */
    private String pattern;
}
