package com.gallop.fastddd.fastdoc;

import java.io.IOException;
import java.nio.file.Path;

/**
 * FastDoc 导出写前校验钩子（ECO-RELEASE-05：最终落盘边界补齐）。
 *
 * <p>在每次实际写入发生之前，以<b>即将传给文件系统操作的最终 Path</b>（写文件路径或批量导出
 * tables 子目录）回调，把校验点与实际写入点绑定：实现方在此做词法包含与实路径包含复核，
 * 越界即抛异常终止该次写入。S3 的输入层校验（exportDir/tableName）继续保留，本钩子覆盖
 * 校验与写入之间的服务内部路径段（tables 段、由数据库表名生成的文件名）。
 *
 * <p>传 {@code null} 表示不做写前校验——本地/CLI 可信路径行为保持不变，不构成权限体系。
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
@FunctionalInterface
public interface FastDocExportFileGuard {

    /**
     * 校验即将写入/创建的最终路径。
     *
     * @param target 即将执行文件系统操作的路径
     * @throws IOException               路径无法解析（如可信根不可用）
     * @throws IllegalArgumentException  路径越出允许边界
     */
    void checkWriteTarget(Path target) throws IOException;
}
