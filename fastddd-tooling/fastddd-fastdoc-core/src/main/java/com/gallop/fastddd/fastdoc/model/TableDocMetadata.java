package com.gallop.fastddd.fastdoc.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 表文档元数据模型
 * 包含表的基本信息、列信息、索引信息、外键信息等
 *
 * @author gallop
 * @date 2024-12-19
 */
@Data
public class TableDocMetadata {

    /**
     * 表名
     */
    private String tableName;

    /**
     * 表注释
     */
    private String tableComment;

    /**
     * 表类型（如TABLE、VIEW等）
     */
    private String tableType;

    /**
     * 列元数据列表
     */
    private List<ColumnDocMetadata> columns = new ArrayList<>();

    /**
     * 主键列
     */
    private List<ColumnDocMetadata> primaryKeys = new ArrayList<>();

    /**
     * 索引列表
     */
    private List<IndexDocMetadata> indexes = new ArrayList<>();

    /**
     * 外键列表
     */
    private List<ForeignKeyDocMetadata> foreignKeys = new ArrayList<>();

    /**
     * 获取列的总数
     *
     * @return 列的总数
     */
    public int getColumnCount() {
        return columns != null ? columns.size() : 0;
    }

    /**
     * 根据列名查找列元数据
     *
     * @param columnName 列名
     * @return 列元数据，如果未找到返回null
     */
    public ColumnDocMetadata findColumnByName(String columnName) {
        if (columns == null || columnName == null) {
            return null;
        }

        return columns.stream()
                .filter(column -> columnName.equals(column.getColumnName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 获取主键列名列表
     *
     * @return 主键列名列表
     */
    public List<String> getPrimaryKeyNames() {
        List<String> pkNames = new ArrayList<>();
        if (primaryKeys != null) {
            for (ColumnDocMetadata pk : primaryKeys) {
                pkNames.add(pk.getColumnName());
            }
        }
        return pkNames;
    }

    /**
     * 列文档元数据
     */
    @Data
    public static class ColumnDocMetadata {

        /**
         * 列名
         */
        private String columnName;

        /**
         * 列注释
         */
        private String columnComment;

        /**
         * 列在表中的位置
         */
        private int ordinalPosition;

        /**
         * 数据类型（JDBC类型）
         */
        private int dataType;

        /**
         * 数据类型名称
         */
        private String dataTypeName;

        /**
         * SQL类型
         */
        private String sqlType;

        /**
         * Java类型
         */
        private String javaType;

        /**
         * 列大小
         */
        private int columnSize;

        /**
         * 小数位数
         */
        private int decimalDigits;

        /**
         * 是否为主键
         */
        private boolean primaryKey;

        /**
         * 主键名称
         */
        private String primaryKeyName;

        /**
         * 主键序号
         */
        private int primaryKeySequence;

        /**
         * 是否允许为空
         */
        private boolean nullable;

        /**
         * 是否自增
         */
        private boolean autoIncrement;

        /**
         * 默认值
         */
        private String defaultValue;

        /**
         * 获取完整的列类型描述
         *
         * @return 列类型描述
         */
        public String getFullColumnType() {
            StringBuilder sb = new StringBuilder();
            sb.append(dataTypeName);

            if (columnSize > 0) {
                sb.append("(").append(columnSize);
                if (decimalDigits > 0) {
                    sb.append(",").append(decimalDigits);
                }
                sb.append(")");
            }

            return sb.toString();
        }

        /**
         * 是否为必填字段
         *
         * @return 是否为必填字段
         */
        public boolean isRequired() {
            return !nullable && !autoIncrement;
        }
    }

    /**
     * 索引文档元数据
     */
    @Data
    public static class IndexDocMetadata {

        /**
         * 索引名称
         */
        private String indexName;

        /**
         * 是否唯一索引
         */
        private boolean unique;

        /**
         * 索引类型
         */
        private String indexType;

        /**
         * 索引列名列表
         */
        private List<String> columns = new ArrayList<>();

        /**
         * 获取索引列名字符串
         *
         * @return 索引列名字符串，用逗号分隔
         */
        public String getColumnsString() {
            return String.join(", ", columns);
        }
    }

    /**
     * 外键文档元数据
     */
    @Data
    public static class ForeignKeyDocMetadata {

        /**
         * 外键名称
         */
        private String foreignKeyName;

        /**
         * 外键列名
         */
        private String columnName;

        /**
         * 引用的表名
         */
        private String referencedTableName;

        /**
         * 引用的列名
         */
        private String referencedColumnName;

        /**
         * 更新规则
         */
        private int updateRule;

        /**
         * 删除规则
         */
        private int deleteRule;

        /**
         * 获取外键关系描述
         *
         * @return 外键关系描述
         */
        public String getRelationshipDescription() {
            return String.format("%s -> %s.%s", columnName, referencedTableName, referencedColumnName);
        }

        /**
         * 获取更新规则描述
         *
         * @return 更新规则描述
         */
        public String getUpdateRuleDescription() {
            return getRuleDescription(updateRule);
        }

        /**
         * 获取删除规则描述
         *
         * @return 删除规则描述
         */
        public String getDeleteRuleDescription() {
            return getRuleDescription(deleteRule);
        }

        /**
         * 获取规则描述
         *
         * @param rule 规则代码
         * @return 规则描述
         */
        private String getRuleDescription(int rule) {
            switch (rule) {
                case 0: // CASCADE
                    return "CASCADE";
                case 1: // RESTRICT
                    return "RESTRICT";
                case 2: // SET_NULL
                    return "SET NULL";
                case 3: // NO_ACTION
                    return "NO ACTION";
                case 4: // SET_DEFAULT
                    return "SET DEFAULT";
                default:
                    return "UNKNOWN";
            }
        }
    }
}