package com.gallop.fastddd.doc.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * API控制器模型
 * 表示一个Spring @RestController类的文档信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Data
public class ApiController {

    /**
     * 控制器类名
     */
    private String className;

    /**
     * 控制器简单类名（不含包名）
     */
    private String simpleClassName;

    /**
     * 完整限定名（含包名）
     */
    private String qualifiedName;

    /**
     * 控制器描述（从Javadoc或注解中提取）
     */
    private String description;

    /**
     * 基础路径（来自@RequestMapping）
     */
    private String basePath;

    /**
     * 所属包名
     */
    private String packageName;

    /**
     * 该控制器的所有API端点
     */
    private List<ApiMethod> methods = new ArrayList<>();

    /**
     * 添加端点方法
     */
    public void addMethod(ApiMethod method) {
        if (methods == null) {
            methods = new ArrayList<>();
        }
        methods.add(method);
    }

    /**
     * 获取完整的基础路径
     * 如果basePath为空，返回"/"
     */
    public String getFullPath() {
        if (basePath == null || basePath.isEmpty()) {
            return "/";
        }
        return basePath.startsWith("/") ? basePath : "/" + basePath;
    }
}
