package com.gallop.fastddd.tooling.api;

import com.gallop.fastddd.common.model.TableMetadata;

import java.util.List;

/**
 * 数据库元数据内省契约（SPLIT-003）。
 *
 * <p>取代消费者直接绑定 {@code codegen.DatabaseMetadataService} 具体类。用于在不依赖具体 JDBC/MyBatis-Plus
 * 实现的前提下表达"读取库表结构"能力。实现由 {@code fastddd-code-generator} 提供。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public interface DatabaseIntrospector {

    /** 列出库内全部表元数据。 */
    List<TableMetadata> getAllTables();

    /**
     * 按表名读取单表元数据。
     *
     * @param tableName 表名
     * @return 表元数据；表不存在返回 {@code null}
     */
    TableMetadata getTable(String tableName);
}
