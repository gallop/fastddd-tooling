package com.gallop.fastddd.doc.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * API请求/响应体模型
 * 表示请求体或响应体的结构信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Data
public class ApiBody {

    /**
     * 类型名称（类名）
     */
    private String typeName;

    /**
     * 完整限定名
     */
    private String qualifiedName;

    /**
     * 简短描述
     */
    private String description;

    /**
     * 内容类型（如 application/json）
     */
    private String contentType;

    /**
     * 是否为数组或集合类型
     */
    private boolean isArray;

    /**
     * 数组/集合的元素类型（如果isArray为true）
     */
    private String elementType;

    /**
     * 是否为泛型类型
     */
    private boolean isGeneric;

    /**
     * 泛型参数类型列表
     */
    private List<String> genericTypeParameters = new ArrayList<>();

    /**
     * 是否为基础类型（String, Integer等）
     */
    private boolean isPrimitive;

    /**
     * 示例JSON数据
     */
    private String exampleJson;

    /**
     * 字段列表（如果是DTO对象）
     */
    private List<ApiField> fields = new ArrayList<>();

    /**
     * 添加字段
     */
    public void addField(ApiField field) {
        if (fields == null) {
            fields = new ArrayList<>();
        }
        fields.add(field);
    }

    /**
     * 获取显示类型名
     * 如果是数组，返回"List<T>"格式
     */
    public String getDisplayName() {
        if (isArray && elementType != null) {
            return "List<" + elementType + ">";
        } else if (isGeneric && !genericTypeParameters.isEmpty()) {
            StringBuilder sb = new StringBuilder(typeName);
            sb.append("<");
            for (int i = 0; i < genericTypeParameters.size(); i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(genericTypeParameters.get(i));
            }
            sb.append(">");
            return sb.toString();
        }
        return typeName;
    }
}
