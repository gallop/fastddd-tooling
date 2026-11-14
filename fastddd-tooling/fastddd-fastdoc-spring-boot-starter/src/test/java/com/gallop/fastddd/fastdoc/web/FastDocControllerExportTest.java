package com.gallop.fastddd.fastdoc.web;

import com.gallop.fastddd.fastdoc.FastDocExportService;
import com.gallop.fastddd.fastdoc.FastDocService;
import com.gallop.fastddd.fastdoc.FastDocTemplateService;
import com.gallop.fastddd.fastdoc.model.DatabaseDocMetadata;
import com.gallop.fastddd.fastdoc.model.TableDocMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FastDoc 导出端点 HTTP 行为测试（ECO-RELEASE-04 / S3，A4 阻断项 A3）。
 *
 * <p>真实链路：Controller（含防护）→ FastDocExportPathGuard（真实）→
 * FastDocExportService（真实）→ FastDocTemplateService（真实），仅 FastDocService（JDBC 元数据）
 * 以 mock 替代。断言：合法导出落在可信根内；非法请求 400 且可信根外零文件变化。
 *
 * <p>全栈（真实自动装配 + 真实 DataSource）版本见 tooling-consumer-tests 的 fastdoc-minimal 场景。
 */
class FastDocControllerExportTest {

    @TempDir
    Path tempDir;

    private FastDocService fastDocService;
    private FastDocController controller;
    private MockMvc mockMvc;
    private Path root;

    @BeforeEach
    void setUp() {
        fastDocService = mock(FastDocService.class);
        FastDocTemplateService templateService = new FastDocTemplateService();
        FastDocExportService exportService = new FastDocExportService(fastDocService, templateService);
        root = tempDir.resolve("fastdoc-root");
        FastDocExportPathGuard guard = new FastDocExportPathGuard(root.toString());
        controller = new FastDocController(fastDocService, templateService, exportService, guard);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        TableDocMetadata table = new TableDocMetadata();
        table.setTableName("T_USER");
        table.setTableType("TABLE");
        DatabaseDocMetadata database = new DatabaseDocMetadata();
        database.setDatabaseName("sandbox");
        database.setTables(Collections.singletonList(table));
        when(fastDocService.extractDatabaseMetadata()).thenReturn(database);
        when(fastDocService.extractTableMetadata("T_USER")).thenReturn(table);
    }

    private long countMdFiles(Path dir) throws Exception {
        try (var walk = Files.walk(dir)) {
            return walk.filter(p -> p.toString().endsWith(".md")).count();
        }
    }

    @Test
    void legalDatabaseExportWritesInsideTrustedRoot() throws Exception {
        mockMvc.perform(post("/api/fastdoc/database/export").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertTrue(countMdFiles(root) >= 1, "合法导出应在可信根内产出文档");
    }

    @Test
    void legalSubdirExportStaysInsideRoot() throws Exception {
        mockMvc.perform(post("/api/fastdoc/database/export?exportDir=sub"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertTrue(Files.isDirectory(root.resolve("sub")));
        assertTrue(countMdFiles(root.resolve("sub")) >= 1);
    }

    @Test
    void legalTableExportWritesInsideRoot() throws Exception {
        mockMvc.perform(post("/api/fastdoc/table/T_USER/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertTrue(countMdFiles(root) >= 1);
    }

    @Test
    void traversalExportDirRejectedWithNoFileChangeOutsideRoot() throws Exception {
        Path escapeTarget = tempDir.resolve("escaped");
        mockMvc.perform(post("/api/fastdoc/database/export?exportDir=../escaped"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        assertFalse(Files.exists(escapeTarget), "穿越目标目录不得被创建");
    }

    @Test
    void absoluteExportDirRejectedWithNoFileChangeOutsideRoot() throws Exception {
        Path outside = tempDir.resolve("outside-abs");
        Files.createDirectories(outside);
        Path marker = outside.resolve("marker.txt");
        Files.writeString(marker, "untouched");
        mockMvc.perform(post("/api/fastdoc/database/export?exportDir=" + outside.toAbsolutePath()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        // 根目录外无文件变化：既有 marker 未被改动、目录内无新增
        assertEquals("untouched", Files.readString(marker));
        try (var stream = Files.list(outside)) {
            assertEquals(1, stream.count());
        }
    }

    @Test
    void maliciousTableNameRejectedBeforeAnyWrite() {
        // 含路径分隔符的表名（URL 形态下被路由层吞掉，这里直接调方法验证防护本身）
        var response = controller.exportTableDoc("a/b", null, "markdown");
        assertEquals(400, response.getStatusCode().value());
        assertEquals(Boolean.FALSE, response.getBody().get("success"));
        var dotDot = controller.exportTableDoc("..", "sub", "markdown");
        assertEquals(400, dotDot.getStatusCode().value());
        assertEquals(Boolean.FALSE, dotDot.getBody().get("success"));
        var ads = controller.exportTableDoc("a:b", "sub", "markdown");
        assertEquals(400, ads.getStatusCode().value());
    }

    @Test
    void tablesBatchExportWritesOnlyUnderRootTablesDir() throws Exception {
        mockMvc.perform(post("/api/fastdoc/tables/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertTrue(Files.isDirectory(root.resolve("tables")));
        assertEquals(1, countMdFiles(root.resolve("tables")));
        // 可信根外（tempDir 直下除 root 外）零新增
        try (var stream = Files.list(tempDir)) {
            List<Path> outsideRoot = stream.filter(p -> !p.equals(root)).toList();
            assertEquals(0, outsideRoot.size(), "可信根外不得有任何新建: " + outsideRoot);
        }
    }

    // ========== ECO-RELEASE-05：最终落盘边界（写入层） ==========

    @Test
    void batchExportSkipsHostileDbTableNameWithoutEscapingRoot() throws Exception {
        // 数据库表名是不可直接信任的输入面：引号标识符可携带路径片段，进入文件名后 resolve 不规范化
        TableDocMetadata evil = new TableDocMetadata();
        evil.setTableName("../../../z-controller-evil");
        evil.setTableType("TABLE");
        TableDocMetadata normal = new TableDocMetadata();
        normal.setTableName("T_USER");
        normal.setTableType("TABLE");
        DatabaseDocMetadata withEvil = new DatabaseDocMetadata();
        withEvil.setDatabaseName("sandbox");
        withEvil.setTables(java.util.Arrays.asList(normal, evil));
        when(fastDocService.extractDatabaseMetadata()).thenReturn(withEvil);

        mockMvc.perform(post("/api/fastdoc/tables/export?exportDir=tn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // tablesDir=root/tn/tables，三级 .. 的逃逸落点是 tempDir（可信根外一层）
        assertFalse(Files.exists(tempDir.resolve("z-controller-evil.md")), "恶意表名不得在根外产生文件");
        assertTrue(Files.exists(root.resolve("tn").resolve("tables").resolve("T_USER.md")), "正常表导出不受影响");
    }

    @Test
    void batchExportThroughTablesJunctionRejectedWithoutOutsideOverwrite() throws Exception {
        org.junit.jupiter.api.Assumptions.assumeTrue(
                System.getProperty("os.name", "").toLowerCase().contains("win"), "junction 用例仅 Windows 执行");
        Path outside = tempDir.resolve("outside-jx");
        Files.createDirectories(outside);
        Files.writeString(outside.resolve("T_USER.md"), "SENTINEL-KEEP");
        Path tablesLink = root.resolve("jj").resolve("tables");
        Files.createDirectories(tablesLink.getParent());
        Process mklink = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J",
                tablesLink.toString(), outside.toString())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        org.junit.jupiter.api.Assumptions.assumeTrue(mklink.waitFor() == 0, "mklink /J 创建失败，跳过 junction 用例");

        mockMvc.perform(post("/api/fastdoc/tables/export?exportDir=jj"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false));

        assertEquals("SENTINEL-KEEP", Files.readString(outside.resolve("T_USER.md")), "根外既有文件不得被覆盖");
        try (var stream = Files.list(outside)) {
            assertEquals(1, stream.count(), "根外目录零新增: ");
        }
    }
}
