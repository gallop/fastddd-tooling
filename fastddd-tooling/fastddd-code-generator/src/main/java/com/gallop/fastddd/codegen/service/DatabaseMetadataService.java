package com.gallop.fastddd.codegen.service;

import com.gallop.fastddd.codegen.config.DatabaseConfig;
import com.gallop.fastddd.common.model.ColumnMetadata;
import com.gallop.fastddd.common.model.TableMetadata;
import com.gallop.fastddd.codegen.util.NamingUtils;
import com.gallop.fastddd.tooling.api.DatabaseIntrospector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

/**
 * 数据库元数据提取服务
 *
 * <p>实现 Tooling 稳定契约 {@link DatabaseIntrospector}（SPLIT-003），使消费方可面向契约编程，
 * 不再绑定本具体类。{@link #getAllTables()} / {@link #getTable(String)} 签名与契约一致。</p>
 *
 * @author gallop
 * @date 2023-10-10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseMetadataService implements DatabaseIntrospector {

    private final DatabaseConfig databaseConfig;
    private final Map<Integer, String> jdbcTypeToJavaType = new HashMap<>();

    /**
     * 初始化JDBC类型到Java类型的映射
     */
    {
        jdbcTypeToJavaType.put(Types.CHAR, "String");
        jdbcTypeToJavaType.put(Types.VARCHAR, "String");
        jdbcTypeToJavaType.put(Types.LONGVARCHAR, "String");
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
    }

    /**
     * 获取数据库连接
     *
     * @return 数据库连接
     * @throws SQLException SQL异常
     */
    private Connection getConnection() throws SQLException {
        try {
            Class.forName(databaseConfig.getDriverClassName());
            return DriverManager.getConnection(
                    databaseConfig.getUrl(),
                    databaseConfig.getUsername(),
                    databaseConfig.getPassword()
            );
        } catch (ClassNotFoundException e) {
            throw new SQLException("数据库驱动类未找到: " + databaseConfig.getDriverClassName(), e);
        }
    }

    /**
     * 获取所有表的元数据
     *
     * @return 表元数据列表
     */
    public List<TableMetadata> getAllTables() {
        List<TableMetadata> tables = new ArrayList<>();

        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = databaseConfig.getCatalog();
            String schema = databaseConfig.getSchema();

            // 获取所有表
            // 对于 MySQL: catalog 是数据库名, schema 通常为 null
            // 对于 PostgreSQL: schema 是模式名
            try (ResultSet rs = metaData.getTables(catalog, schema, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    String tableComment = rs.getString("REMARKS");
                    
                    // 创建表元数据对象
                    TableMetadata table = new TableMetadata();
                    table.setTableName(tableName);
                    table.setTableComment(tableComment);
                    
                    // 设置实体名称（去除表前缀，转为驼峰命名）
                    String entityName = tableName;
                    if (databaseConfig.getTablePrefix() != null && !databaseConfig.getTablePrefix().isEmpty()) {
                        if (entityName.startsWith(databaseConfig.getTablePrefix())) {
                            entityName = entityName.substring(databaseConfig.getTablePrefix().length());
                        }
                    }
                    table.setEntityName(NamingUtils.underscoreToCamel(entityName, true));
                    
                    // 获取表的列信息
                    getTableColumns(metaData, table);
                    
                    // 获取表的主键信息
                    getTablePrimaryKeys(metaData, table);
                    
                    tables.add(table);
                }
            }
        } catch (SQLException e) {
            log.error("获取数据库表元数据失败", e);
            throw new RuntimeException("获取数据库表元数据失败", e);
        }
        
        return tables;
    }

    /**
     * 获取指定表的元数据
     *
     * @param tableName 表名
     * @return 表元数据
     */
    public TableMetadata getTable(String tableName) {
        try (Connection conn = getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = databaseConfig.getCatalog();
            String schema = databaseConfig.getSchema();

            // 获取表信息
            try (ResultSet rs = metaData.getTables(catalog, schema, tableName, new String[]{"TABLE"})) {
                if (rs.next()) {
                    String tableComment = rs.getString("REMARKS");
                    
                    // 创建表元数据对象
                    TableMetadata table = new TableMetadata();
                    table.setTableName(tableName);
                    table.setTableComment(tableComment);
                    
                    // 设置实体名称（去除表前缀，转为驼峰命名）
                    String entityName = tableName;
                    if (databaseConfig.getTablePrefix() != null && !databaseConfig.getTablePrefix().isEmpty()) {
                        if (entityName.startsWith(databaseConfig.getTablePrefix())) {
                            entityName = entityName.substring(databaseConfig.getTablePrefix().length());
                        }
                    }
                    table.setEntityName(NamingUtils.underscoreToCamel(entityName, true));
                    
                    // 获取表的列信息
                    getTableColumns(metaData, table);
                    
                    // 获取表的主键信息
                    getTablePrimaryKeys(metaData, table);
                    
                    return table;
                }
            }
        } catch (SQLException e) {
            log.error("获取表元数据失败: {}", tableName, e);
            throw new RuntimeException("获取表元数据失败: " + tableName, e);
        }
        
        return null;
    }

    /**
     * 获取表的列信息
     *
     * @param metaData 数据库元数据
     * @param table    表元数据
     * @throws SQLException SQL异常
     */
    private void getTableColumns(DatabaseMetaData metaData, TableMetadata table) throws SQLException {
        try (ResultSet rs = metaData.getColumns(databaseConfig.getCatalog(), databaseConfig.getSchema(), table.getTableName(), "%")) {
            while (rs.next()) {
                ColumnMetadata column = new ColumnMetadata();
                
                String columnName = rs.getString("COLUMN_NAME");
                column.setColumnName(columnName);
                column.setColumnComment(rs.getString("REMARKS"));
                column.setFieldName(NamingUtils.underscoreToCamel(columnName, false));
                
                int dataType = rs.getInt("DATA_TYPE");
                column.setDataType(dataType);
                column.setDataTypeName(rs.getString("TYPE_NAME"));
                column.setColumnSize(rs.getInt("COLUMN_SIZE"));
                column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
                column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                
                // 设置Java类型
                String javaType = jdbcTypeToJavaType.getOrDefault(dataType, "Object");
                column.setJavaType(javaType);
                
                // 检查是否自增列（MySQL特有）
                String isAutoIncrement = rs.getString("IS_AUTOINCREMENT");
                column.setAutoIncrement("YES".equals(isAutoIncrement));
                
                table.getColumns().add(column);
            }
        }
    }

    /**
     * 获取表的主键信息
     *
     * @param metaData 数据库元数据
     * @param table    表元数据
     * @throws SQLException SQL异常
     */
    private void getTablePrimaryKeys(DatabaseMetaData metaData, TableMetadata table) throws SQLException {
        try (ResultSet rs = metaData.getPrimaryKeys(databaseConfig.getCatalog(), databaseConfig.getSchema(), table.getTableName())) {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                
                // 在列列表中找到对应的列，并标记为主键
                for (ColumnMetadata column : table.getColumns()) {
                    if (column.getColumnName().equals(columnName)) {
                        column.setPrimaryKey(true);
                        table.getPrimaryKeys().add(column);
                        break;
                    }
                }
            }
        }
    }
}