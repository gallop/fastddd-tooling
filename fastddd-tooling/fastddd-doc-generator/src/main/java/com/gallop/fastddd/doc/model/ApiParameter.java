package com.gallop.fastddd.doc.model;

import lombok.Data;

/**
 * API参数模型
 * 表示方法参数的信息（路径变量、查询参数、请求头等）
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Data
public class ApiParameter {

    /**
     * 参数名
     */
    private String name;

    /**
     * 参数类型（简单类型或完整类名）
     */
    private String type;

    /**
     * 参数描述
     */
    private String description;

    /**
     * 参数位置（路径变量、查询参数、请求头等）
     */
    private ParameterLocation location;

    /**
     * 是否必需
     */
    private boolean required;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 是否允许为空
     */
    private boolean nullable;

    /**
     * 参数示例值
     */
    private String example;

    /**
     * 参数位置枚举
     */
    public enum ParameterLocation {
        /**
         * 路径变量 (@PathVariable)
         */
        PATH,

        /**
         * 查询参数 (@RequestParam)
         */
        QUERY,

        /**
         * 请求头 (@RequestHeader)
         */
        HEADER,

        /**
         * Cookie参数 (@CookieValue)
         */
        COOKIE,

        /**
         * 请求体 (@RequestBody) - 实际上应该用ApiBody表示
         */
        BODY,

        /**
         * 未知位置
         */
        UNKNOWN;

        /**
         * 获取显示名称
         */
        public String getDisplayName() {
            return switch (this) {
                case PATH -> "路径参数";
                case QUERY -> "查询参数";
                case HEADER -> "请求头";
                case COOKIE -> "Cookie";
                case BODY -> "请求体";
                case UNKNOWN -> "未知";
            };
        }
    }
}
