package com.gallop.fastddd.fastdoc.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * FastDoc HTTP 导出安全配置（ECO-RELEASE-04 / S3，A4 阻断项 A3 整改）
 *
 * <p>背景：HTTP 导出端点（{@code /database/export}、{@code /table/{tableName}/export}、
 * {@code /tables/export}）的 {@code exportDir} 请求参数此前无任何校验，
 * 直达 {@link com.gallop.fastddd.fastdoc.FastDocExportService} 的任意目录写（A4 实证 PATH_TRAVERSAL
 * 完整利用链）。本配置为 HTTP 导出端点提供<b>可信根目录</b>约束：所有经 HTTP 触发的文件创建/写入
 * 都必须落在 rootDir 之内。
 *
 * <p>边界：仅约束不可信的 HTTP 输入。fastdoc-core 的本地/CLI 用法（直接调用
 * {@code FastDocExportService} / {@code saveToFile} 的可信本地配置）行为不变，不引入新的权限体系。
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
@ConfigurationProperties(prefix = "fastddd.fastdoc.export")
public class FastDocExportProperties {

    /**
     * HTTP 导出可信根目录。
     * <p>为空时默认 {@code ${user.dir}/fastdoc-export}（与 core 默认导出目录一致，
     * 默认用法〔不传 exportDir〕落点不变）。相对路径按进程工作目录解析。
     * <p>任何 {@code exportDir} 请求参数（相对子目录）解析结果越出该根即被拒绝；
     * 绝对路径、盘符、UNC、{@code ../} 穿越与符号链接逃逸一律拒绝。
     */
    private String rootDir = "";

    public String getRootDir() {
        return rootDir;
    }

    public void setRootDir(String rootDir) {
        this.rootDir = rootDir;
    }
}
