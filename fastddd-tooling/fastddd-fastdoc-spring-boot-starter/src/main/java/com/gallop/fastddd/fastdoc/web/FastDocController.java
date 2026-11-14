package com.gallop.fastddd.fastdoc.web;

import com.gallop.fastddd.common.i18n.Messages;
import com.gallop.fastddd.fastdoc.FastDocExportService;
import com.gallop.fastddd.fastdoc.FastDocService;
import com.gallop.fastddd.fastdoc.FastDocTemplateService;
import com.gallop.fastddd.fastdoc.model.DatabaseDocMetadata;
import com.gallop.fastddd.fastdoc.model.TableDocMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * FastDoc数据库文档生成控制器
 * 提供数据库文档生成的REST API接口
 *
 * <p>SPLIT-002：自 fast-ddd-web（包 com.gallop.fastddd.web.fastdoc）上移至 Tooling 层
 * fastddd-fastdoc-spring-boot-starter（包 com.gallop.fastddd.fastdoc.web），消除
 * Framework → Tooling 反向依赖。类逻辑保持不变。
 *
 * @author gallop
 * @date 2024-12-19
 */
@Slf4j
@RestController
@RequestMapping("/api/fastdoc")
@RequiredArgsConstructor
@Tag(name = "FastDoc", description = "数据库文档生成API")
@ConditionalOnBean(FastDocService.class)
public class FastDocController {

    private final FastDocService fastDocService;
    private final FastDocTemplateService templateService;
    private final FastDocExportService exportService;
    private final FastDocExportPathGuard exportPathGuard;

    /**
     * 获取数据库元数据
     *
     * @return 数据库元数据
     */
    @GetMapping("/metadata")
    @Operation(summary = "获取数据库元数据", description = "获取当前数据库的完整元数据信息")
    public ResponseEntity<DatabaseDocMetadata> getDatabaseMetadata() {
        log.info("获取数据库元数据");

        DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();
        return ResponseEntity.ok(metadata);
    }

    /**
     * 获取指定表的元数据
     *
     * @param tableName 表名
     * @return 表元数据
     */
    @GetMapping("/metadata/table/{tableName}")
    @Operation(summary = "获取表元数据", description = "获取指定表的详细元数据信息")
    public ResponseEntity<TableDocMetadata> getTableMetadata(
            @Parameter(description = "表名", required = true)
            @PathVariable String tableName) {
        log.info("获取表[{}]的元数据", tableName);

        TableDocMetadata metadata = fastDocService.extractTableMetadata(tableName);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(metadata);
    }

    /**
     * 生成数据库文档（Markdown格式）
     *
     * @return Markdown格式的数据库文档
     */
    @GetMapping(value = "/generate/markdown", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "生成Markdown文档", description = "生成完整的数据库Markdown文档")
    public ResponseEntity<String> generateMarkdownDoc() {
        log.info("生成数据库Markdown文档");

        DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();
        String markdownDoc = templateService.generateMarkdownDoc(metadata);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=database-doc.md")
                .body(markdownDoc);
    }

    /**
     * 生成指定表的文档（Markdown格式）
     *
     * @param tableName 表名
     * @return Markdown格式的表文档
     */
    @GetMapping(value = "/generate/markdown/table/{tableName}", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "生成表Markdown文档", description = "生成指定表的Markdown文档")
    public ResponseEntity<String> generateTableMarkdownDoc(
            @Parameter(description = "表名", required = true)
            @PathVariable String tableName) {
        log.info("生成表[{}]的Markdown文档", tableName);

        TableDocMetadata metadata = fastDocService.extractTableMetadata(tableName);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }

        String markdownDoc = templateService.generateTableMarkdownDoc(metadata);

        return ResponseEntity.ok()
                .header("Content-Disposition", String.format("attachment; filename=%s-doc.md", tableName))
                .body(markdownDoc);
    }

    /**
     * 生成数据库文档（HTML格式）
     *
     * @return HTML格式的数据库文档
     */
    @GetMapping(value = "/generate/html", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "生成HTML文档", description = "生成完整的数据库HTML文档")
    public ResponseEntity<String> generateHtmlDoc() {
        log.info("生成数据库HTML文档");

        DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();
        String htmlDoc = templateService.generateHtmlDoc(metadata);

        return ResponseEntity.ok(htmlDoc);
    }

    /**
     * 生成指定表的文档（HTML格式）
     *
     * @param tableName 表名
     * @return HTML格式的表文档
     */
    @GetMapping(value = "/generate/html/table/{tableName}", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "生成表HTML文档", description = "生成指定表的HTML文档")
    public ResponseEntity<String> generateTableHtmlDoc(
            @Parameter(description = "表名", required = true)
            @PathVariable String tableName) {
        log.info("生成表[{}]的HTML文档", tableName);

        TableDocMetadata metadata = fastDocService.extractTableMetadata(tableName);
        if (metadata == null) {
            return ResponseEntity.notFound().build();
        }

        String htmlDoc = templateService.generateTableHtmlDoc(metadata);

        return ResponseEntity.ok(htmlDoc);
    }

    /**
     * 生成数据库Markdown文档
     *
     * @return Markdown格式的数据库文档
     */
    @GetMapping(value = "/database/markdown", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "生成数据库Markdown文档", description = "生成完整的数据库Markdown格式文档")
    public ResponseEntity<String> generateDatabaseMarkdown() {
        log.info("生成数据库Markdown文档");
        try {
            DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();
            String markdown = templateService.generateMarkdownDoc(metadata);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"database-doc.md\"")
                    .body(markdown);
        } catch (Exception e) {
            log.error("生成数据库Markdown文档失败", e);
            return ResponseEntity.internalServerError()
                    .body(Messages.get("web.doc.generate.failed", e.getMessage()));
        }
    }

    /**
     * 生成数据库HTML文档
     *
     * @return HTML格式的数据库文档
     */
    @GetMapping(value = "/database/html", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "生成数据库HTML文档", description = "生成完整的数据库HTML格式文档")
    public ResponseEntity<String> generateDatabaseHtml() {
        log.info("生成数据库HTML文档");
        try {
            DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();
            String html = templateService.generateHtmlDoc(metadata);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"database-doc.html\"")
                    .body(html);
        } catch (Exception e) {
            log.error("生成数据库HTML文档失败", e);
            return ResponseEntity.internalServerError()
                    .body(Messages.get("web.doc.generate.failed", e.getMessage()));
        }
    }

    /**
     * 生成指定表的Markdown文档
     *
     * @param tableName 表名
     * @return Markdown格式的表文档
     */
    @GetMapping(value = "/table/{tableName}/markdown", produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "生成表Markdown文档", description = "生成指定表的Markdown格式文档")
    public ResponseEntity<String> generateTableMarkdown(
            @Parameter(description = "表名", required = true)
            @PathVariable String tableName) {
        log.info("生成表[{}]的Markdown文档", tableName);
        try {
            TableDocMetadata metadata = fastDocService.extractTableMetadata(tableName);
            if (metadata == null) {
                return ResponseEntity.notFound().build();
            }
            String markdown = templateService.generateTableMarkdownDoc(metadata);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + tableName + "-doc.md\"")
                    .body(markdown);
        } catch (Exception e) {
            log.error("生成表[{}]Markdown文档失败", tableName, e);
            return ResponseEntity.internalServerError()
                    .body(Messages.get("web.doc.generate.failed", e.getMessage()));
        }
    }

    /**
     * 生成指定表的HTML文档
     *
     * @param tableName 表名
     * @return HTML格式的表文档
     */
    @GetMapping(value = "/table/{tableName}/html", produces = MediaType.TEXT_HTML_VALUE)
    @Operation(summary = "生成表HTML文档", description = "生成指定表的HTML格式文档")
    public ResponseEntity<String> generateTableHtml(
            @Parameter(description = "表名", required = true)
            @PathVariable String tableName) {
        log.info("生成表[{}]的HTML文档", tableName);
        try {
            TableDocMetadata metadata = fastDocService.extractTableMetadata(tableName);
            if (metadata == null) {
                return ResponseEntity.notFound().build();
            }
            String html = templateService.generateTableHtmlDoc(metadata);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + tableName + "-doc.html\"")
                    .body(html);
        } catch (Exception e) {
            log.error("生成表[{}]HTML文档失败", tableName, e);
            return ResponseEntity.internalServerError()
                    .body(Messages.get("web.doc.generate.failed", e.getMessage()));
        }
    }

    /**
     * 导出数据库文档到文件
     *
     * @param format 导出格式（markdown/html）
     * @param exportDir 导出目录（可选）
     * @return 导出结果
     */
    @PostMapping("/database/export")
    @Operation(summary = "导出数据库文档", description = "将数据库文档导出到文件系统")
    public ResponseEntity<Map<String, Object>> exportDatabaseDoc(
            @Parameter(description = "导出格式", example = "markdown")
            @RequestParam(defaultValue = "markdown") String format,
            @Parameter(description = "导出目录")
            @RequestParam(required = false) String exportDir) {
        log.info("导出数据库文档，格式: {}, 目录: {}", format, exportDir);

        // ECO-RELEASE-04/S3：不可信 HTTP 输入先经可信根校验（防穿越/绝对路径/符号链接逃逸），
        // 校验失败一律 400；通过后以解析出的可信根内绝对目录调用服务。边界不依赖 @PreAuthorize。
        // ECO-RELEASE-05：同时把 guard 作为写前校验钩子传入——服务内部路径段（tables 子目录、
        // 由数据库表名生成的文件名）在实际写入前以最终 Path 复核，校验点与写入点绑定。
        final String targetDir;
        try {
            targetDir = exportPathGuard.resolveExportDir(exportDir).toString();
        } catch (IllegalArgumentException e) {
            log.warn("拒绝 FastDoc HTTP 数据库导出请求: {}", e.getMessage());
            Map<String, Object> rejected = new HashMap<>();
            rejected.put("success", false);
            rejected.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(rejected);
        }

        FastDocExportService.ExportResult result = exportService.exportDatabaseDoc(targetDir, format, exportPathGuard);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        if (result.isSuccess()) {
            response.put("filePath", result.getFilePath());
            response.put("tableCount", result.getTableCount());
            response.put("fileSize", result.getFileSize());
        }

        return result.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.internalServerError().body(response);
    }

    /**
     * 导出指定表的文档到文件
     *
     * @param tableName 表名
     * @param format 导出格式（markdown/html）
     * @param exportDir 导出目录（可选）
     * @return 导出结果
     */
    @PostMapping("/table/{tableName}/export")
    @Operation(summary = "导出表文档", description = "将指定表的文档导出到文件系统")
    public ResponseEntity<Map<String, Object>> exportTableDoc(
            @Parameter(description = "表名", required = true)
            @PathVariable String tableName,
            @Parameter(description = "导出格式", example = "markdown")
            @RequestParam(defaultValue = "markdown") String format,
            @Parameter(description = "导出目录")
            @RequestParam(required = false) String exportDir) {
        log.info("导出表[{}]文档，格式: {}, 目录: {}", tableName, format, exportDir);

        // ECO-RELEASE-04/S3：目录与表名（进入文件名）均先校验，失败 400
        // ECO-RELEASE-05：guard 同时作为写前校验钩子传入（最终路径写前复核）
        final String targetDir;
        try {
            exportPathGuard.validateTableName(tableName);
            targetDir = exportPathGuard.resolveExportDir(exportDir).toString();
        } catch (IllegalArgumentException e) {
            log.warn("拒绝 FastDoc HTTP 表导出请求: {}", e.getMessage());
            Map<String, Object> rejected = new HashMap<>();
            rejected.put("success", false);
            rejected.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(rejected);
        }

        FastDocExportService.ExportResult result = exportService.exportTableDoc(tableName, targetDir, format, exportPathGuard);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        if (result.isSuccess()) {
            response.put("filePath", result.getFilePath());
            response.put("tableCount", result.getTableCount());
            response.put("fileSize", result.getFileSize());
        }

        return result.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.internalServerError().body(response);
    }

    /**
     * 批量导出所有表的文档到文件
     *
     * @param format 导出格式（markdown/html）
     * @param exportDir 导出目录（可选）
     * @return 导出结果
     */
    @PostMapping("/tables/export")
    @Operation(summary = "批量导出所有表文档", description = "将所有表的文档批量导出到文件系统")
    public ResponseEntity<Map<String, Object>> exportAllTablesDoc(
            @Parameter(description = "导出格式", example = "markdown")
            @RequestParam(defaultValue = "markdown") String format,
            @Parameter(description = "导出目录")
            @RequestParam(required = false) String exportDir) {
        log.info("批量导出所有表文档，格式: {}, 目录: {}", format, exportDir);

        // ECO-RELEASE-04/S3：批量导出目录同样先经可信根校验
        // ECO-RELEASE-05：批量导出的 tables 子目录与数据库表名文件名均经写前校验钩子复核
        final String targetDir;
        try {
            targetDir = exportPathGuard.resolveExportDir(exportDir).toString();
        } catch (IllegalArgumentException e) {
            log.warn("拒绝 FastDoc HTTP 批量导出请求: {}", e.getMessage());
            Map<String, Object> rejected = new HashMap<>();
            rejected.put("success", false);
            rejected.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(rejected);
        }

        FastDocExportService.ExportResult result = exportService.exportAllTablesDoc(targetDir, format, exportPathGuard);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("message", result.getMessage());
        if (result.isSuccess()) {
            response.put("filePath", result.getFilePath());
            response.put("tableCount", result.getTableCount());
            response.put("fileSize", result.getFileSize());
        }

        return result.isSuccess() ?
                ResponseEntity.ok(response) :
                ResponseEntity.internalServerError().body(response);
 }

    /**
     * 获取数据库统计信息
     *
     * @return 数据库统计信息
     */
    @GetMapping("/statistics")
    @Operation(summary = "获取数据库统计信息", description = "获取数据库的统计信息，如表数量、列数量等")
    public ResponseEntity<Map<String, Object>> getDatabaseStatistics() {
        log.info("获取数据库统计信息");

        DatabaseDocMetadata metadata = fastDocService.extractDatabaseMetadata();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("databaseName", metadata.getDatabaseName());
        statistics.put("databaseType", metadata.getDatabaseType());
        statistics.put("databaseVersion", metadata.getDatabaseVersion());
        statistics.put("tableCount", metadata.getTableCount());
        statistics.put("totalColumnCount", metadata.getTotalColumnCount());
        statistics.put("generateTime", metadata.getGenerateTime());

        // 统计各种类型的表数量
        Map<String, Integer> tableTypeCount = new HashMap<>();
        for (TableDocMetadata table : metadata.getTables()) {
            String tableType = table.getTableType();
            tableTypeCount.put(tableType, tableTypeCount.getOrDefault(tableType, 0) + 1);
        }
        statistics.put("tableTypeCount", tableTypeCount);

        // 统计主键数量
        int primaryKeyCount = 0;
        int foreignKeyCount = 0;
        int indexCount = 0;

        for (TableDocMetadata table : metadata.getTables()) {
            primaryKeyCount += table.getPrimaryKeys().size();
            foreignKeyCount += table.getForeignKeys().size();
            indexCount += table.getIndexes().size();
        }

        statistics.put("primaryKeyCount", primaryKeyCount);
        statistics.put("foreignKeyCount", foreignKeyCount);
        statistics.put("indexCount", indexCount);

        return ResponseEntity.ok(statistics);
    }

    /**
     * 健康检查接口
     *
     * @return 健康状态
     */
    @GetMapping("/health")
    @Operation(summary = "健康检查", description = "检查FastDoc服务的健康状态")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "FastDoc");
        health.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return ResponseEntity.ok(health);
    }
}
