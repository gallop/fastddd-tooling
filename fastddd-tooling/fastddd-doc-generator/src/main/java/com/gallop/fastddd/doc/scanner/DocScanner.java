package com.gallop.fastddd.doc.scanner;

import com.gallop.fastddd.doc.model.ApiDoc;

/**
 * API文档扫描器接口
 * 定义扫描Java源代码并生成API文档的契约
 *
 * @author gallop
 * @since 2025-01-10
 * @version 1.0.0-SNAPSHOT
 */
public interface DocScanner {

    /**
     * 扫描指定包路径下的所有类并生成API文档
     *
     * @param basePackage 基础包路径（如：com.gallop.fastddd.controller）
     * @param sourceRoot   源代码根目录（如：src/main/java）
     * @return API文档对象
     */
    ApiDoc scan(String basePackage, String sourceRoot);

    /**
     * 扫描指定包路径下的所有类并生成API文档
     * 使用默认源代码根目录
     *
     * @param basePackage 基础包路径
     * @return API文档对象
     */
    default ApiDoc scan(String basePackage) {
        return scan(basePackage, "src/main/java");
    }
}
