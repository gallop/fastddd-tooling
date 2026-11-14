package com.gallop.fastddd.fastdoc;

import com.gallop.fastddd.fastdoc.model.DatabaseDocMetadata;
import com.gallop.fastddd.fastdoc.model.TableDocMetadata;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * FastDoc文档导出服务
 * 提供文档导出到文件系统的功能
 *
 * @author gallop
 * @date 2024-12-19
 */
@Slf4j
public class FastDocExportService {

    private final FastDocService fastDocService;
    private final FastDocTemplateService templateService;

    public FastDocExportService(FastDocService fastDocService, FastDocTemplateService templateService) {
        this.fastDocService = fastDocService;
        this.templateService = templateService;
    }

    private static final String DEFAULT_EXPORT_DIR = "fastdoc-export";
    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss";

    /**
     * 导出完整的数据库文档到指定目录
     *
     * @param exportDir 导出目录，如果为null则使用默认目录
     * @param format 导出格式：markdown 或 html
     * @return 导出结果信息
     */
    public ExportResult exportDatabaseDoc(String exportDir, String format) {
        return exportDatabaseDoc(exportDir, format, null);
    }

    /**
     * 导出完整的数据库文档到指定目录（带写前校验）。
     *
     * @param exportDir 导出目录，如果为null则使用默认目录
     * @param format 导出格式：markdown 或 html
     * @param fileGuard 写前校验钩子（ECO-RELEASE-05）：对即将写入的最终路径回调，null 表示不校验
     * @return 导出结果信息
     */
    public ExportResult exportDatabaseDoc(String exportDir, String format, FastDocExportFileGuard fileGuard) {
        log.info("开始导出数据库文档，格式: {}, 目录: {}", format, exportDir);

        try {
            // 准备导出目录
            Path exportPath = prepareExportDirectory(exportDir);

            // 提取数据库元数据
            DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();

            // 生成文档内容
            String content;
            String fileName;
            if ("html".equalsIgnoreCase(format)) {
                content = templateService.generateHtmlDoc(metadata);
                fileName = String.format("database-doc_%s.html", getCurrentTimestamp());
            } else {
                content = templateService.generateMarkdownDoc(metadata);
                fileName = String.format("database-doc_%s.md", getCurrentTimestamp());
            }

            // 写入文件（ECO-RELEASE-05：写前对最终路径校验，校验点与写入点绑定）
            Path filePath = exportPath.resolve(fileName);
            checkWriteTarget(fileGuard, filePath);
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("数据库文档导出成功: {}", filePath.toAbsolutePath());

            return ExportResult.success(filePath.toAbsolutePath().toString(),
                    metadata.getTableCount(), content.length());

        } catch (Exception e) {
            log.error("导出数据库文档失败", e);
            return ExportResult.failure("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出指定表的文档
     *
     * @param tableName 表名
     * @param exportDir 导出目录，如果为null则使用默认目录
     * @param format 导出格式：markdown 或 html
     * @return 导出结果信息
     */
    public ExportResult exportTableDoc(String tableName, String exportDir, String format) {
        return exportTableDoc(tableName, exportDir, format, null);
    }

    /**
     * 导出指定表的文档（带写前校验）。
     *
     * @param tableName 表名
     * @param exportDir 导出目录，如果为null则使用默认目录
     * @param format 导出格式：markdown 或 html
     * @param fileGuard 写前校验钩子（ECO-RELEASE-05）：对即将写入的最终路径回调，null 表示不校验
     * @return 导出结果信息
     */
    public ExportResult exportTableDoc(String tableName, String exportDir, String format,
                                       FastDocExportFileGuard fileGuard) {
        log.info("开始导出表[{}]文档，格式: {}, 目录: {}", tableName, format, exportDir);

        try {
            // 准备导出目录
            Path exportPath = prepareExportDirectory(exportDir);

            // 提取表元数据
            TableDocMetadata metadata = fastDocService.extractTableMetadata(tableName);
            if (metadata == null) {
                return ExportResult.failure("表不存在: " + tableName);
            }

            // 生成文档内容
            String content;
            String fileName;
            if ("html".equalsIgnoreCase(format)) {
                content = templateService.generateTableHtmlDoc(metadata);
                fileName = String.format("%s-doc_%s.html", tableName, getCurrentTimestamp());
            } else {
                content = templateService.generateTableMarkdownDoc(metadata);
                fileName = String.format("%s-doc_%s.md", tableName, getCurrentTimestamp());
            }

            // 写入文件（ECO-RELEASE-05：写前对最终路径校验，校验点与写入点绑定）
            Path filePath = exportPath.resolve(fileName);
            checkWriteTarget(fileGuard, filePath);
            Files.write(filePath, content.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

            log.info("表[{}]文档导出成功: {}", tableName, filePath.toAbsolutePath());

            return ExportResult.success(filePath.toAbsolutePath().toString(),
                    1, content.length());

        } catch (Exception e) {
            log.error("导出表[{}]文档失败", tableName, e);
            return ExportResult.failure("导出失败: " + e.getMessage());
        }
    }

    /**
     * 批量导出所有表的文档
     *
     * @param exportDir 导出目录，如果为null则使用默认目录
     * @param format 导出格式：markdown 或 html
     * @return 导出结果信息
     */
    public ExportResult exportAllTablesDoc(String exportDir, String format) {
        return exportAllTablesDoc(exportDir, format, null);
    }

    /**
     * 批量导出所有表的文档（带写前校验）。
     *
     * <p>ECO-RELEASE-05：服务内部追加的 {@code tables} 子目录段与由数据库表名生成的文件名，
     * 均在创建/写入前经 fileGuard 对最终路径校验（数据库表名是不可直接信任的输入面——
     * 引号标识符可携带路径片段）。
     *
     * @param exportDir 导出目录，如果为null则使用默认目录
     * @param format 导出格式：markdown 或 html
     * @param fileGuard 写前校验钩子，null 表示不校验（本地/CLI 可信路径）
     * @return 导出结果信息
     */
    public ExportResult exportAllTablesDoc(String exportDir, String format, FastDocExportFileGuard fileGuard) {
        log.info("开始批量导出所有表文档，格式: {}, 目录: {}", format, exportDir);

        try {
            // 准备导出目录
            Path exportPath = prepareExportDirectory(exportDir);
            Path tablesDir = exportPath.resolve("tables");
            // tables 段（服务内部路径，S3 输入层校验未覆盖）在建目录前先经最终路径校验
            checkWriteTarget(fileGuard, tablesDir);
            Files.createDirectories(tablesDir);

            // 提取数据库元数据
            DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();

            int successCount = 0;
            long totalSize = 0;

            // 导出每个表的文档
            for (TableDocMetadata table : metadata.getTables()) {
                try {
                    String content;
                    String fileName;
                    if ("html".equalsIgnoreCase(format)) {
                        content = templateService.generateTableHtmlDoc(table);
                        fileName = String.format("%s.html", table.getTableName());
                    } else {
                        content = templateService.generateTableMarkdownDoc(table);
                        fileName = String.format("%s.md", table.getTableName());
                    }

                    Path filePath = tablesDir.resolve(fileName);
                    // 每个最终文件写前校验（表名可携带路径片段，最终路径必须仍受约束）
                    checkWriteTarget(fileGuard, filePath);
                    Files.write(filePath, content.getBytes(StandardCharsets.UTF_8),
                            StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);

                    successCount++;
                    totalSize += content.length();

                } catch (Exception e) {
                    log.warn("导出表[{}]文档失败: {}", table.getTableName(), e.getMessage());
                }
            }

            log.info("批量导出完成，成功: {}/{}, 总大小: {} bytes",
                    successCount, metadata.getTableCount(), totalSize);

            return ExportResult.success(tablesDir.toAbsolutePath().toString(),
                    successCount, totalSize);

        } catch (Exception e) {
            log.error("批量导出表文档失败", e);
            return ExportResult.failure("批量导出失败: " + e.getMessage());
        }
    }

    /**
     * 准备导出目录
     *
     * @param exportDir 指定的导出目录
     * @return 准备好的导出目录路径
     * @throws IOException 创建目录失败时抛出
     */
    private Path prepareExportDirectory(String exportDir) throws IOException {
        Path exportPath;
        if (exportDir != null && !exportDir.trim().isEmpty()) {
            exportPath = Paths.get(exportDir.trim());
        } else {
            exportPath = Paths.get(DEFAULT_EXPORT_DIR);
        }

        // 创建目录（如果不存在）
        Files.createDirectories(exportPath);

        return exportPath;
    }

    /**
     * 写前校验钩子调用（ECO-RELEASE-05）：fileGuard 为 null 时保持既有行为不变。
     */
    private static void checkWriteTarget(FastDocExportFileGuard fileGuard, Path target) throws IOException {
        if (fileGuard != null) {
            fileGuard.checkWriteTarget(target);
        }
    }

    /**
     * 获取当前时间戳字符串
     *
     * @return 时间戳字符串
     */
    private String getCurrentTimestamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }

    /**
     * 保存内容到文件
     *
     * @param content  文件内容
     * @param outputDir 输出目录
     * @param fileName 文件名
     * @throws IOException 如果保存失败
     */
    public void saveToFile(String content, String outputDir, String fileName) throws IOException {
        Path outputPath = Paths.get(outputDir);
        if (!Files.exists(outputPath)) {
            Files.createDirectories(outputPath);
        }

        Path filePath = outputPath.resolve(fileName);
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }
    public static class ExportResult {
        private final boolean success;
        private final String message;
        private final String filePath;
        private final int tableCount;
        private final long fileSize;

        private ExportResult(boolean success, String message, String filePath, int tableCount, long fileSize) {
            this.success = success;
            this.message = message;
            this.filePath = filePath;
            this.tableCount = tableCount;
            this.fileSize = fileSize;
        }

        public static ExportResult success(String filePath, int tableCount, long fileSize) {
            return new ExportResult(true, "导出成功", filePath, tableCount, fileSize);
        }

        public static ExportResult failure(String message) {
            return new ExportResult(false, message, null, 0, 0);
        }

        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getFilePath() { return filePath; }
        public int getTableCount() { return tableCount; }
        public long getFileSize() { return fileSize; }
    }
}