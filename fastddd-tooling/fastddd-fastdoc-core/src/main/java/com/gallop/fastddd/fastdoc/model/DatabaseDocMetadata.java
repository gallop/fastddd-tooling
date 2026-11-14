package com.gallop.fastddd.fastdoc.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据库文档元数据模型
 * 包含数据库的基本信息和所有表的元数据
 *
 * @author gallop
 * @date 2024-12-19
 */
@Data
public class DatabaseDocMetadata {

    /**
     * 数据库名称
     */
    private String databaseName;

    /**
     * 数据库类型（如MySQL、PostgreSQL等）
     */
    private String databaseType;

    /**
     * 数据库版本
     */
    private String databaseVersion;

    /**
     * 驱动名称
     */
    private String driverName;

    /**
     * 驱动版本
     */
    private String driverVersion;

    /**
     * 数据库连接URL
     */
    private String url;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 文档生成时间
     */
    private Date generateTime;

    /**
     * 表元数据列表
     */
    private List<TableDocMetadata> tables = new ArrayList<>();

    /**
     * 获取表的总数
     *
     * @return 表的总数
     */
    public int getTableCount() {
        return tables != null ? tables.size() : 0;
    }

    /**
     * 获取所有列的总数
     *
     * @return 所有列的总数
     */
    public int getTotalColumnCount() {
        if (tables == null) {
            return 0;
        }

        return tables.stream()
                .mapToInt(table -> table.getColumns() != null ? table.getColumns().size() : 0)
                .sum();
    }

    /**
     * 根据表名查找表元数据
     *
     * @param tableName 表名
     * @return 表元数据，如果未找到返回null
     */
    public TableDocMetadata findTableByName(String tableName) {
        if (tables == null || tableName == null) {
            return null;
        }

        return tables.stream()
                .filter(table -> tableName.equals(table.getTableName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取数据库简要信息
     *
     * @return 数据库简要信息
     */
    public String getDatabaseInfo() {
        return String.format("%s %s (%s)", databaseType, databaseVersion, databaseName);
    }
}