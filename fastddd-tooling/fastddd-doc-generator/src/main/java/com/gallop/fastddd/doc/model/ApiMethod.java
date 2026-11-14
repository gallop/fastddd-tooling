package com.gallop.fastddd.doc.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * API端点方法模型
 * 表示一个REST端点的完整信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Data
public class ApiMethod {

    /**
     * 方法名
     */
    private String methodName;

    /**
     * HTTP方法类型（GET, POST, PUT, DELETE等）
     */
    private HttpMethod httpMethod;

    /**
     * 端点路径（相对路径）
     */
    private String path;

    /**
     * 完整URL路径（包含控制器的基础路径）
     */
    private String fullPath;

    /**
     * 端点描述
     */
    private String description;

    /**
     * 端点摘要（简短描述）
     */
    private String summary;

    /**
     * 请求参数列表（路径变量、查询参数）
     */
    private List<ApiParameter> parameters = new ArrayList<>();

    /**
     * 请求体信息
     */
    private ApiBody requestBody;

    /**
     * 响应体信息
     */
    private ApiBody responseBody;

    /**
     * 返回类型
     */
    private String returnType;

    /**
     * 是否已废弃
     */
    private boolean deprecated;

    /**
     * 添加参数
     */
    public void addParameter(ApiParameter parameter) {
        if (parameters == null) {
            parameters = new ArrayList<>();
        }
        parameters.add(parameter);
    }

    /**
     * HTTP方法枚举
     */
    public enum HttpMethod {
        GET,
        POST,
        PUT,
        DELETE,
        PATCH,
        HEAD,
        OPTIONS,
        TRACE;

        /**
         * 从字符串解析HTTP方法
         */
        public static HttpMethod fromString(String method) {
            if (method == null) {
                return GET;
            }
            try {
                return HttpMethod.valueOf(method.toUpperCase());
            } catch (IllegalArgumentException e) {
                return GET;
            }
        }
    }
}
