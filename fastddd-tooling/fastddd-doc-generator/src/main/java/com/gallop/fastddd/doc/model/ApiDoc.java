package com.gallop.fastddd.doc.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * API文档根对象
 * 表示整个API文档的根节点，包含所有控制器的文档信息
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
@Data
public class ApiDoc {

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目版本
     */
    private String projectVersion;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 基础包路径（扫描的根包）
     */
    private String basePackage;

    /**
     * 生成的日期时间
     */
    private LocalDateTime generatedAt;

    /**
     * 所有API控制器列表
     */
    private List<ApiController> controllers = new ArrayList<>();

    /**
     * 添加控制器
     */
    public void addController(ApiController controller) {
        if (controllers == null) {
            controllers = new ArrayList<>();
        }
        controllers.add(controller);
    }

    /**
     * 获取所有端点数量
     */
    public int getTotalEndpoints() {
        return controllers.stream()
                .mapToInt(controller -> controller.getMethods() == null ? 0 : controller.getMethods().size())
                .sum();
    }
}
