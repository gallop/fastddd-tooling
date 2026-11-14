package com.gallop.fastddd.fastdoc;

import com.gallop.fastddd.fastdoc.model.DatabaseDocMetadata;
import com.gallop.fastddd.fastdoc.model.TableDocMetadata;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

/**
 * FastDoc数据库文档生成服务
 * 提供数据库元数据提取和文档生成功能
 *
 * @author gallop
 * @date 2024-12-19
 */
@Slf4j
public class FastDocService {

    private final DataSource dataSource;

    public FastDocService(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    private final Map<Integer, String> jdbcTypeToJavaType = new HashMap<>();
    private final Map<Integer, String> jdbcTypeToSqlType = new HashMap<>();

    /**
     * 初始化JDBC类型映射
     */
    {
        // JDBC类型到Java类型映射
        jdbcTypeToJavaType.put(Types.CHAR, "String");
        jdbcTypeToJavaType.put(Types.VARCHAR, "String");
        jdbcTypeToJavaType.put(Types.LONGVARCHAR, "String");
        jdbcTypeToJavaType.put(Types.NCHAR, "String");
        jdbcTypeToJavaType.put(Types.NVARCHAR, "String");
        jdbcTypeToJavaType.put(Types.LONGNVARCHAR, "String");
        jdbcTypeToJavaType.put(Types.NUMERIC, "BigDecimal");
        jdbcTypeToJavaType.put(Types.DECIMAL, "BigDecimal");
        jdbcTypeToJavaType.put(Types.BIT, "Boolean");
        jdbcTypeToJavaType.put(Types.BOOLEAN, "Boolean");
        jdbcTypeToJavaType.put(Types.TINYINT, "Integer");
        jdbcTypeToJavaType.put(Types.SMALLINT, "Integer");
        jdbcTypeToJavaType.put(Types.INTEGER, "Integer");
        jdbcTypeToJavaType.put(Types.BIGINT, "Long");
        jdbcTypeToJavaType.put(Types.REAL, "Float");
        jdbcTypeToJavaType.put(Types.FLOAT, "Double");
        jdbcTypeToJavaType.put(Types.DOUBLE, "Double");
        jdbcTypeToJavaType.put(Types.BINARY, "byte[]");
        jdbcTypeToJavaType.put(Types.VARBINARY, "byte[]");
        jdbcTypeToJavaType.put(Types.LONGVARBINARY, "byte[]");
        jdbcTypeToJavaType.put(Types.DATE, "LocalDate");
        jdbcTypeToJavaType.put(Types.TIME, "LocalTime");
        jdbcTypeToJavaType.put(Types.TIMESTAMP, "LocalDateTime");
        jdbcTypeToJavaType.put(Types.BLOB, "byte[]");
        jdbcTypeToJavaType.put(Types.CLOB, "String");
        jdbcTypeToJavaType.put(Types.NCLOB, "String");

        // JDBC类型到SQL类型映射
        jdbcTypeToSqlType.put(Types.CHAR, "CHAR");
        jdbcTypeToSqlType.put(Types.VARCHAR, "VARCHAR");
        jdbcTypeToSqlType.put(Types.LONGVARCHAR, "TEXT");
        jdbcTypeToSqlType.put(Types.NCHAR, "NCHAR");
        jdbcTypeToSqlType.put(Types.NVARCHAR, "NVARCHAR");
        jdbcTypeToSqlType.put(Types.LONGNVARCHAR, "NTEXT");
        jdbcTypeToSqlType.put(Types.NUMERIC, "NUMERIC");
        jdbcTypeToSqlType.put(Types.DECIMAL, "DECIMAL");
        jdbcTypeToSqlType.put(Types.BIT, "BIT");
        jdbcTypeToSqlType.put(Types.BOOLEAN, "BOOLEAN");
        jdbcTypeToSqlType.put(Types.TINYINT, "TINYINT");
        jdbcTypeToSqlType.put(Types.SMALLINT, "SMALLINT");
        jdbcTypeToSqlType.put(Types.INTEGER, "INT");
        jdbcTypeToSqlType.put(Types.BIGINT, "BIGINT");
        jdbcTypeToSqlType.put(Types.REAL, "REAL");
        jdbcTypeToSqlType.put(Types.FLOAT, "FLOAT");
        jdbcTypeToSqlType.put(Types.DOUBLE, "DOUBLE");
        jdbcTypeToSqlType.put(Types.BINARY, "BINARY");
        jdbcTypeToSqlType.put(Types.VARBINARY, "VARBINARY");
        jdbcTypeToSqlType.put(Types.LONGVARBINARY, "BLOB");
        jdbcTypeToSqlType.put(Types.DATE, "DATE");
        jdbcTypeToSqlType.put(Types.TIME, "TIME");
        jdbcTypeToSqlType.put(Types.TIMESTAMP, "TIMESTAMP");
        jdbcTypeToSqlType.put(Types.BLOB, "BLOB");
        jdbcTypeToSqlType.put(Types.CLOB, "CLOB");
        jdbcTypeToSqlType.put(Types.NCLOB, "NCLOB");
    }

    /**
     * 提取数据库文档元数据
     *
     * @return 数据库文档元数据
     */
    public DatabaseDocMetadata extractDatabaseMetadata() {
        log.info("开始提取数据库文档元数据");

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            DatabaseDocMetadata databaseDoc = new DatabaseDocMetadata();

            // 设置数据库基本信息
            databaseDoc.setDatabaseName(connection.getCatalog());
            databaseDoc.setDatabaseType(metaData.getDatabaseProductName());
            databaseDoc.setDatabaseVersion(metaData.getDatabaseProductVersion());
            databaseDoc.setDriverName(metaData.getDriverName());
            databaseDoc.setDriverVersion(metaData.getDriverVersion());
            databaseDoc.setUrl(metaData.getURL());
            databaseDoc.setUserName(metaData.getUserName());
            databaseDoc.setGenerateTime(new java.util.Date());

            // 获取所有表的元数据
            List<TableDocMetadata> tables = extractTablesMetadata(metaData);
            databaseDoc.setTables(tables);

            log.info("数据库文档元数据提取完成，共提取{}张表", tables.size());
            return databaseDoc;

        } catch (SQLException e) {
            log.error("提取数据库文档元数据失败", e);
            throw new IllegalStateException("提取数据库文档元数据失败", e);
        }
    }

    /**
     * 提取指定表的文档元数据
     *
     * @param tableName 表名
     * @return 表文档元数据
     */
    public TableDocMetadata extractTableMetadata(String tableName) {
        log.info("开始提取表[{}]的文档元数据", tableName);

        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            TableDocMetadata table = extractSingleTableMetadata(metaData, tableName);
            if (table != null) {
                log.info("表[{}]文档元数据提取完成", tableName);
            } else {
                log.warn("未找到表[{}]", tableName);
            }

            return table;

        } catch (SQLException e) {
            log.error("提取表[{}]文档元数据失败", tableName, e);
            throw new IllegalStateException("提取表文档元数据失败: " + tableName, e);
        }
    }

    /**
     * 提取所有表的元数据
     *
     * @param metaData 数据库元数据
     * @return 表元数据列表
     * @throws SQLException SQL异常
     */
    private List<TableDocMetadata> extractTablesMetadata(DatabaseMetaData metaData) throws SQLException {
        List<TableDocMetadata> tables = new ArrayList<>();

        // 获取所有表
        try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                String tableName = rs.getString("TABLE_NAME");

                // 跳过系统表
                if (isSystemTable(tableName)) {
                    continue;
                }

                TableDocMetadata table = extractSingleTableMetadata(metaData, tableName);
                if (table != null) {
                    tables.add(table);
                }
            }
        }

        return tables;
    }

    /**
     * 提取单个表的元数据
     *
     * @param metaData  数据库元数据
     * @param tableName 表名
     * @return 表元数据
     * @throws SQLException SQL异常
     */
    private TableDocMetadata extractSingleTableMetadata(DatabaseMetaData metaData, String tableName) throws SQLException {
        // 获取表基本信息
        try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
            if (rs.next()) {
                TableDocMetadata table = new TableDocMetadata();
                table.setTableName(rs.getString("TABLE_NAME"));
                table.setTableComment(rs.getString("REMARKS"));
                table.setTableType(rs.getString("TABLE_TYPE"));

                // 获取列信息
                extractColumnsMetadata(metaData, table);

                // 获取主键信息
                extractPrimaryKeysMetadata(metaData, table);

                // 获取索引信息
                extractIndexesMetadata(metaData, table);

                // 获取外键信息
                extractForeignKeysMetadata(metaData, table);

                return table;
            }
        }

        return null;
    }

    /**
     * 提取列元数据
     *
     * @param metaData 数据库元数据
     * @param table    表元数据
     * @throws SQLException SQL异常
     */
    private void extractColumnsMetadata(DatabaseMetaData metaData, TableDocMetadata table) throws SQLException {
        try (ResultSet rs = metaData.getColumns(null, null, table.getTableName(), "%")) {
            while (rs.next()) {
                TableDocMetadata.ColumnDocMetadata column = new TableDocMetadata.ColumnDocMetadata();

                column.setColumnName(rs.getString("COLUMN_NAME"));
                column.setColumnComment(rs.getString("REMARKS"));
                column.setOrdinalPosition(rs.getInt("ORDINAL_POSITION"));

                int dataType = rs.getInt("DATA_TYPE");
                column.setDataType(dataType);
                column.setDataTypeName(rs.getString("TYPE_NAME"));
                column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);

                String defaultValue = rs.getString("COLUMN_DEF");
                column.setDefaultValue(defaultValue);

                // 设置Java类型和SQL类型
                column.setJavaType(jdbcTypeToJavaType.getOrDefault(dataType, "Object"));
                column.setSqlType(jdbcTypeToSqlType.getOrDefault(dataType, "UNKNOWN"));

                // 检查是否自增列
                String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
                column.setAutoIncrement("YES".equals(isAutoIncrement));

                table.getColumns().add(column);
            }
        }
    }

    /**
     * 提取主键元数据
     *
     * @param metaData 数据库元数据
     * @param table    表元数据
     * @throws SQLException SQL异常
     */
    private void extractPrimaryKeysMetadata(DatabaseMetaData metaData, TableDocMetadata table) throws SQLException {
        try (ResultSet rs = metaData.getPrimaryKeys(null, null, table.getTableName())) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                String pkName = rs.getString("PK_NAME");
                int keySeq = rs.getInt("KEY_SEQ");

                // 在列列表中找到对应的列，并标记为主键
                for (TableDocMetadata.ColumnDocMetadata column : table.getColumns()) {
                    if (column.getColumnName().equals(columnName)) {
                        column.setPrimaryKey(true);
                        column.setPrimaryKeyName(pkName);
                        column.setPrimaryKeySequence(keySeq);
                        table.getPrimaryKeys().add(column);
                        break;
                    }
                }
            }
        }
    }

    /**
     * 提取索引元数据
     *
     * @param metaData 数据库元数据
     * @param table    表元数据
     * @throws SQLException SQL异常
     */
    private void extractIndexesMetadata(DatabaseMetaData metaData, TableDocMetadata table) throws SQLException {
        try (ResultSet rs = metaData.getIndexInfo(null, null, table.getTableName(), false, false)) {
            Map<String, TableDocMetadata.IndexDocMetadata> indexMap = new HashMap<>();

            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");

                // 跳过主键索引
                if (indexName == null || "PRIMARY".equals(indexName)) {
                    continue;
                }

                TableDocMetadata.IndexDocMetadata index = indexMap.computeIfAbsent(indexName, k -> {
                    TableDocMetadata.IndexDocMetadata idx = new TableDocMetadata.IndexDocMetadata();
                    idx.setIndexName(indexName);
                    try {
                        idx.setUnique(!rs.getBoolean("NON_UNIQUE"));
                        idx.setIndexType(rs.getString("TYPE"));
                    } catch (SQLException e) {
                        log.warn("读取索引[{}]元数据失败: {}", indexName, e.getMessage());
                    }
                    return idx;
                });

                String columnName = rs.getString("COLUMN_NAME");
                if (columnName != null) {
                    index.getColumns().add(columnName);
                }
            }

            table.setIndexes(new ArrayList<>(indexMap.values()));
        }
    }

    /**
     * 提取外键元数据
     *
     * @param metaData 数据库元数据
     * @param table    表元数据
     * @throws SQLException SQL异常
     */
    private void extractForeignKeysMetadata(DatabaseMetaData metaData, TableDocMetadata table) throws SQLException {
        try (ResultSet rs = metaData.getImportedKeys(null, null, table.getTableName())) {
            while (rs.next()) {
                TableDocMetadata.ForeignKeyDocMetadata foreignKey = new TableDocMetadata.ForeignKeyDocMetadata();

                foreignKey.setForeignKeyName(rs.getString("FK_NAME"));
                foreignKey.setColumnName(rs.getString("FKCOLUMN_NAME"));
                foreignKey.setReferencedTableName(rs.getString("PKTABLE_NAME"));
                foreignKey.setReferencedColumnName(rs.getString("PKCOLUMN_NAME"));
                foreignKey.setUpdateRule(rs.getInt("UPDATE_RULE"));
                foreignKey.setDeleteRule(rs.getInt("DELETE_RULE"));

                table.getForeignKeys().add(foreignKey);
            }
        }
    }

    /**
     * 判断是否为系统表
     *
     * @param tableName 表名
     * @return 是否为系统表
     */
    private boolean isSystemTable(String tableName) {
        if (tableName == null) {
            return true;
        }

        String lowerTableName = tableName.toLowerCase();

        // MySQL系统表
        if (lowerTableName.startsWith("information_schema") ||
            lowerTableName.startsWith("performance_schema") ||
            lowerTableName.startsWith("mysql") ||
            lowerTableName.startsWith("sys")) {
            return true;
        }

        // PostgreSQL系统表
        if (lowerTableName.startsWith("pg_") ||
            lowerTableName.startsWith("information_schema")) {
            return true;
        }

        // Oracle系统表
        if (lowerTableName.startsWith("sys") ||
            lowerTableName.startsWith("system") ||
            lowerTableName.startsWith("dba_") ||
            lowerTableName.startsWith("all_") ||
            lowerTableName.startsWith("user_")) {
            return true;
        }

        // SQL Server系统表
        if (lowerTableName.startsWith("sys") ||
            lowerTableName.startsWith("information_schema")) {
            return true;
        }

        return false;
    }
}