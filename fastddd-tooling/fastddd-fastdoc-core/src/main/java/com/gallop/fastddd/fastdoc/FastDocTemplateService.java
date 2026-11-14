package com.gallop.fastddd.fastdoc;

import com.gallop.fastddd.fastdoc.model.DatabaseDocMetadata;
import com.gallop.fastddd.fastdoc.model.TableDocMetadata;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FastDoc模板生成服务
 * 提供Markdown和HTML格式的数据库文档模板生成功能
 *
 * @author gallop
 * @date 2024-12-19
 */
@Slf4j
public class FastDocTemplateService {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 生成完整的数据库Markdown文档
     *
     * @param metadata 数据库元数据
     * @return Markdown格式的文档
     */
    public String generateMarkdownDoc(DatabaseDocMetadata metadata) {
        log.info("生成数据库Markdown文档");

        StringBuilder md = new StringBuilder();

        // 文档标题和基本信息
        md.append("# 数据库文档\n\n");
        md.append("## 基本信息\n\n");
        md.append("| 项目 | 值 |\n");
        md.append("|------|----|\n");
        md.append(String.format("| 数据库名称 | %s |\n", metadata.getDatabaseName()));
        md.append(String.format("| 数据库类型 | %s |\n", metadata.getDatabaseType()));
        md.append(String.format("| 数据库版本 | %s |\n", metadata.getDatabaseVersion()));
        md.append(String.format("| 驱动名称 | %s |\n", metadata.getDriverName()));
        md.append(String.format("| 驱动版本 | %s |\n", metadata.getDriverVersion()));
        md.append(String.format("| 连接URL | %s |\n", metadata.getUrl()));
        md.append(String.format("| 用户名 | %s |\n", metadata.getUserName()));
        md.append(String.format("| 生成时间 | %s |\n", formatDate(metadata.getGenerateTime())));
        md.append(String.format("| 表数量 | %d |\n", metadata.getTableCount()));
        md.append(String.format("| 总列数 | %d |\n\n", metadata.getTotalColumnCount()));

        // 表目录
        md.append("## 表目录\n\n");
        md.append("| 序号 | 表名 | 说明 | 列数 |\n");
        md.append("|------|------|------|------|\n");

        int index = 1;
        for (TableDocMetadata table : metadata.getTables()) {
            md.append(String.format("| %d | [%s](#%s) | %s | %d |\n",
                    index++,
                    table.getTableName(),
                    table.getTableName().toLowerCase(),
                    table.getTableComment() != null ? table.getTableComment() : "",
                    table.getColumnCount()));
        }
        md.append("\n");

        // 表详细信息
        md.append("## 表详细信息\n\n");
        for (TableDocMetadata table : metadata.getTables()) {
            md.append(generateTableMarkdownSection(table));
        }

        return md.toString();
    }

    /**
     * 生成单个表的Markdown文档
     *
     * @param table 表元数据
     * @return Markdown格式的表文档
     */
    public String generateTableMarkdownDoc(TableDocMetadata table) {
        log.info("生成表[{}]的Markdown文档", table.getTableName());

        StringBuilder md = new StringBuilder();

        md.append(String.format("# 表文档 - %s\n\n", table.getTableName()));
        md.append(generateTableMarkdownSection(table));

        return md.toString();
    }

    /**
     * 生成表的Markdown章节
     *
     * @param table 表元数据
     * @return 表的Markdown章节
     */
    private String generateTableMarkdownSection(TableDocMetadata table) {
        StringBuilder md = new StringBuilder();

        // 表基本信息
        md.append(String.format("### %s\n\n", table.getTableName()));
        if (table.getTableComment() != null && !table.getTableComment().isEmpty()) {
            md.append(String.format("**说明：** %s\n\n", table.getTableComment()));
        }

        // 列信息
        md.append("#### 列信息\n\n");
        md.append("| 序号 | 列名 | 类型 | 长度 | 精度 | 主键 | 非空 | 自增 | 默认值 | 说明 |\n");
        md.append("|------|------|------|------|------|------|------|------|--------|------|\n");

        for (TableDocMetadata.ColumnDocMetadata column : table.getColumns()) {
            md.append(String.format("| %d | %s | %s | %d | %d | %s | %s | %s | %s | %s |\n",
                    column.getOrdinalPosition(),
                    column.getColumnName(),
                    column.getFullColumnType(),
                    column.getColumnSize(),
                    column.getDecimalDigits(),
                    column.isPrimaryKey() ? "✓" : "",
                    column.isRequired() ? "✓" : "",
                    column.isAutoIncrement() ? "✓" : "",
                    column.getDefaultValue() != null ? column.getDefaultValue() : "",
                    column.getColumnComment() != null ? column.getColumnComment() : ""));
        }
        md.append("\n");

        // 主键信息
        if (!table.getPrimaryKeys().isEmpty()) {
            md.append("#### 主键信息\n\n");
            md.append("| 列名 | 主键名称 | 序号 |\n");
            md.append("|------|----------|------|\n");

            for (TableDocMetadata.ColumnDocMetadata pk : table.getPrimaryKeys()) {
                md.append(String.format("| %s | %s | %d |\n",
                        pk.getColumnName(),
                        pk.getPrimaryKeyName() != null ? pk.getPrimaryKeyName() : "",
                        pk.getPrimaryKeySequence()));
            }
            md.append("\n");
        }

        // 索引信息
        if (!table.getIndexes().isEmpty()) {
            md.append("#### 索引信息\n\n");
            md.append("| 索引名称 | 类型 | 唯一 | 列名 |\n");
            md.append("|----------|------|------|------|\n");

            for (TableDocMetadata.IndexDocMetadata index : table.getIndexes()) {
                md.append(String.format("| %s | %s | %s | %s |\n",
                        index.getIndexName(),
                        index.getIndexType() != null ? index.getIndexType() : "",
                        index.isUnique() ? "✓" : "",
                        index.getColumnsString()));
            }
            md.append("\n");
        }

        // 外键信息
        if (!table.getForeignKeys().isEmpty()) {
            md.append("#### 外键信息\n\n");
            md.append("| 外键名称 | 列名 | 引用表 | 引用列 | 更新规则 | 删除规则 |\n");
            md.append("|----------|------|--------|--------|----------|----------|\n");

            for (TableDocMetadata.ForeignKeyDocMetadata fk : table.getForeignKeys()) {
                md.append(String.format("| %s | %s | %s | %s | %s | %s |\n",
                        fk.getForeignKeyName() != null ? fk.getForeignKeyName() : "",
                        fk.getColumnName(),
                        fk.getReferencedTableName(),
                        fk.getReferencedColumnName(),
                        fk.getUpdateRuleDescription(),
                        fk.getDeleteRuleDescription()));
            }
            md.append("\n");
        }

        md.append("---\n\n");

        return md.toString();
    }

    /**
     * 生成完整的数据库HTML文档
     *
     * @param metadata 数据库元数据
     * @return HTML格式的文档
     */
    public String generateHtmlDoc(DatabaseDocMetadata metadata) {
        log.info("生成数据库HTML文档");

        StringBuilder html = new StringBuilder();

        // HTML头部
        html.append(getHtmlHeader("数据库文档"));

        // 文档内容
        html.append("<div class=\"container\">\n");
        html.append("<h1>数据库文档</h1>\n");

        // 基本信息
        html.append("<h2>基本信息</h2>\n");
        html.append("<table class=\"info-table\">\n");
        html.append("<tr><td>数据库名称</td><td>").append(metadata.getDatabaseName()).append("</td></tr>\n");
        html.append("<tr><td>数据库类型</td><td>").append(metadata.getDatabaseType()).append("</td></tr>\n");
        html.append("<tr><td>数据库版本</td><td>").append(metadata.getDatabaseVersion()).append("</td></tr>\n");
        html.append("<tr><td>驱动名称</td><td>").append(metadata.getDriverName()).append("</td></tr>\n");
        html.append("<tr><td>驱动版本</td><td>").append(metadata.getDriverVersion()).append("</td></tr>\n");
        html.append("<tr><td>连接URL</td><td>").append(metadata.getUrl()).append("</td></tr>\n");
        html.append("<tr><td>用户名</td><td>").append(metadata.getUserName()).append("</td></tr>\n");
        html.append("<tr><td>生成时间</td><td>").append(formatDate(metadata.getGenerateTime())).append("</td></tr>\n");
        html.append("<tr><td>表数量</td><td>").append(metadata.getTableCount()).append("</td></tr>\n");
        html.append("<tr><td>总列数</td><td>").append(metadata.getTotalColumnCount()).append("</td></tr>\n");
        html.append("</table>\n");

        // 表目录
        html.append("<h2>表目录</h2>\n");
        html.append("<table class=\"table-list\">\n");
        html.append("<thead>\n");
        html.append("<tr><th>序号</th><th>表名</th><th>说明</th><th>列数</th></tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        int index = 1;
        for (TableDocMetadata table : metadata.getTables()) {
            html.append("<tr>");
            html.append("<td>").append(index++).append("</td>");
            html.append("<td><a href=\"#").append(table.getTableName().toLowerCase()).append("\">")
                .append(table.getTableName()).append("</a></td>");
            html.append("<td>").append(table.getTableComment() != null ? table.getTableComment() : "").append("</td>");
            html.append("<td>").append(table.getColumnCount()).append("</td>");
            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");

        // 表详细信息
        html.append("<h2>表详细信息</h2>\n");
        for (TableDocMetadata table : metadata.getTables()) {
            html.append(generateTableHtmlSection(table));
        }

        html.append("</div>\n");
        html.append(getHtmlFooter());

        return html.toString();
    }

    /**
     * 生成单个表的HTML文档
     *
     * @param table 表元数据
     * @return HTML格式的表文档
     */
    public String generateTableHtmlDoc(TableDocMetadata table) {
        log.info("生成表[{}]的HTML文档", table.getTableName());

        StringBuilder html = new StringBuilder();

        html.append(getHtmlHeader("表文档 - " + table.getTableName()));
        html.append("<div class=\"container\">\n");
        html.append("<h1>表文档 - ").append(table.getTableName()).append("</h1>\n");
        html.append(generateTableHtmlSection(table));
        html.append("</div>\n");
        html.append(getHtmlFooter());

        return html.toString();
    }

    /**
     * 生成表的HTML章节
     *
     * @param table 表元数据
     * @return 表的HTML章节
     */
    private String generateTableHtmlSection(TableDocMetadata table) {
        StringBuilder html = new StringBuilder();

        // 表基本信息
        html.append("<div class=\"table-section\" id=\"").append(table.getTableName().toLowerCase()).append("\">\n");
        html.append("<h3>").append(table.getTableName()).append("</h3>\n");

        if (table.getTableComment() != null && !table.getTableComment().isEmpty()) {
            html.append("<p class=\"table-comment\"><strong>说明：</strong>").append(table.getTableComment()).append("</p>\n");
        }

        // 列信息
        html.append("<h4>列信息</h4>\n");
        html.append("<table class=\"column-table\">\n");
        html.append("<thead>\n");
        html.append("<tr><th>序号</th><th>列名</th><th>类型</th><th>长度</th><th>精度</th><th>主键</th><th>非空</th><th>自增</th><th>默认值</th><th>说明</th></tr>\n");
        html.append("</thead>\n");
        html.append("<tbody>\n");

        for (TableDocMetadata.ColumnDocMetadata column : table.getColumns()) {
            html.append("<tr>");
            html.append("<td>").append(column.getOrdinalPosition()).append("</td>");
            html.append("<td class=\"column-name\">").append(column.getColumnName()).append("</td>");
            html.append("<td>").append(column.getFullColumnType()).append("</td>");
            html.append("<td>").append(column.getColumnSize()).append("</td>");
            html.append("<td>").append(column.getDecimalDigits()).append("</td>");
            html.append("<td>").append(column.isPrimaryKey() ? "✓" : "").append("</td>");
            html.append("<td>").append(column.isRequired() ? "✓" : "").append("</td>");
            html.append("<td>").append(column.isAutoIncrement() ? "✓" : "").append("</td>");
            html.append("<td>").append(column.getDefaultValue() != null ? column.getDefaultValue() : "").append("</td>");
            html.append("<td>").append(column.getColumnComment() != null ? column.getColumnComment() : "").append("</td>");
            html.append("</tr>\n");
        }

        html.append("</tbody>\n");
        html.append("</table>\n");

        // 主键信息
        if (!table.getPrimaryKeys().isEmpty()) {
            html.append("<h4>主键信息</h4>\n");
            html.append("<table class=\"pk-table\">\n");
            html.append("<thead>\n");
            html.append("<tr><th>列名</th><th>主键名称</th><th>序号</th></tr>\n");
            html.append("</thead>\n");
            html.append("<tbody>\n");

            for (TableDocMetadata.ColumnDocMetadata pk : table.getPrimaryKeys()) {
                html.append("<tr>");
                html.append("<td>").append(pk.getColumnName()).append("</td>");
                html.append("<td>").append(pk.getPrimaryKeyName() != null ? pk.getPrimaryKeyName() : "").append("</td>");
                html.append("<td>").append(pk.getPrimaryKeySequence()).append("</td>");
                html.append("</tr>\n");
            }

            html.append("</tbody>\n");
            html.append("</table>\n");
        }

        // 索引信息
        if (!table.getIndexes().isEmpty()) {
            html.append("<h4>索引信息</h4>\n");
            html.append("<table class=\"index-table\">\n");
            html.append("<thead>\n");
            html.append("<tr><th>索引名称</th><th>类型</th><th>唯一</th><th>列名</th></tr>\n");
            html.append("</thead>\n");
            html.append("<tbody>\n");

            for (TableDocMetadata.IndexDocMetadata index : table.getIndexes()) {
                html.append("<tr>");
                html.append("<td>").append(index.getIndexName()).append("</td>");
                html.append("<td>").append(index.getIndexType() != null ? index.getIndexType() : "").append("</td>");
                html.append("<td>").append(index.isUnique() ? "✓" : "").append("</td>");
                html.append("<td>").append(index.getColumnsString()).append("</td>");
                html.append("</tr>\n");
            }

            html.append("</tbody>\n");
            html.append("</table>\n");
        }

        // 外键信息
        if (!table.getForeignKeys().isEmpty()) {
            html.append("<h4>外键信息</h4>\n");
            html.append("<table class=\"fk-table\">\n");
            html.append("<thead>\n");
            html.append("<tr><th>外键名称</th><th>列名</th><th>引用表</th><th>引用列</th><th>更新规则</th><th>删除规则</th></tr>\n");
            html.append("</thead>\n");
            html.append("<tbody>\n");

            for (TableDocMetadata.ForeignKeyDocMetadata fk : table.getForeignKeys()) {
                html.append("<tr>");
                html.append("<td>").append(fk.getForeignKeyName() != null ? fk.getForeignKeyName() : "").append("</td>");
                html.append("<td>").append(fk.getColumnName()).append("</td>");
                html.append("<td>").append(fk.getReferencedTableName()).append("</td>");
                html.append("<td>").append(fk.getReferencedColumnName()).append("</td>");
                html.append("<td>").append(fk.getUpdateRuleDescription()).append("</td>");
                html.append("<td>").append(fk.getDeleteRuleDescription()).append("</td>");
                html.append("</tr>\n");
            }

            html.append("</tbody>\n");
            html.append("</table>\n");
        }

        html.append("</div>\n");

        return html.toString();
    }

    /**
     * 获取HTML文档头部
     *
     * @param title 文档标题
     * @return HTML头部
     */
    private String getHtmlHeader(String title) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"zh-CN\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>" + title + "</title>\n" +
                "    <style>\n" +
                "        body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; margin: 0; padding: 20px; background-color: #f5f5f5; }\n" +
                "        .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }\n" +
                "        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }\n" +
                "        h2 { color: #34495e; margin-top: 30px; }\n" +
                "        h3 { color: #2980b9; margin-top: 25px; }\n" +
                "        h4 { color: #27ae60; margin-top: 20px; }\n" +
                "        table { width: 100%; border-collapse: collapse; margin: 15px 0; }\n" +
                "        th, td { padding: 12px; text-align: left; border: 1px solid #ddd; }\n" +
                "        th { background-color: #f8f9fa; font-weight: bold; color: #495057; }\n" +
                "        tr:nth-child(even) { background-color: #f8f9fa; }\n" +
                "        tr:hover { background-color: #e9ecef; }\n" +
                "        .info-table td:first-child { font-weight: bold; background-color: #e9ecef; width: 150px; }\n" +
                "        .column-name { font-weight: bold; color: #2980b9; }\n" +
                "        .table-comment { color: #666; font-style: italic; margin: 10px 0; }\n" +
                "        .table-section { margin-bottom: 40px; padding: 20px; border: 1px solid #e0e0e0; border-radius: 5px; }\n" +
                "        a { color: #3498db; text-decoration: none; }\n" +
                "        a:hover { text-decoration: underline; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n";
    }

    /**
     * 获取HTML文档尾部
     *
     * @return HTML尾部
     */
    private String getHtmlFooter() {
        return "</body>\n</html>";
    }

    /**
     * 格式化日期
     *
     * @param date 日期
     * @return 格式化后的日期字符串
     */
    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }
}