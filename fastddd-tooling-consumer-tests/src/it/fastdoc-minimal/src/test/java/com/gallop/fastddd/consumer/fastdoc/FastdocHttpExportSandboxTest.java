package com.gallop.fastddd.consumer.fastdoc;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FastDoc HTTP 导出可信根沙箱测试（ECO-RELEASE-04 / S3，A4 阻断项 A3 全栈验收）。
 *
 * <p>真实链路：自动装配的 FastDocWebAutoConfiguration + FastDocExportPathGuard（配置自
 * {@code fastddd.fastdoc.export.root-dir}）+ 真实 FastDocExportService + 真实 H2 DataSource。
 * 断言口径（任务书验收）：合法导出通过；非法请求被拒绝且可信根外无文件变化。
 * 全部路径限定在场景工程 target/ 沙箱内，不触碰真实目录。
 */
@SpringBootTest(classes = FastdocMinimalApp.class,
        properties = "fastddd.fastdoc.export.root-dir=target/fastdoc-export-sandbox")
@AutoConfigureMockMvc
class FastdocHttpExportSandboxTest {

    /** 可信根（相对场景 basedir，随构建产物落在沙箱内） */
    private static final Path ROOT = Path.of("target/fastdoc-export-sandbox");

    @Autowired
    MockMvc mockMvc;

    private long countMdFiles(Path dir) throws Exception {
        try (var walk = Files.walk(dir)) {
            return walk.filter(p -> p.toString().endsWith(".md")).count();
        }
    }

    @Test
    void legalDatabaseExportWritesInsideTrustedRoot() throws Exception {
        mockMvc.perform(post("/api/fastdoc/database/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertTrue(countMdFiles(ROOT) >= 1, "合法导出应在可信根内产出文档");
    }

    @Test
    void legalTableExportWithRealTableWritesInsideRoot() throws Exception {
        mockMvc.perform(post("/api/fastdoc/table/T_FASTDOC_DEMO/export"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertTrue(countMdFiles(ROOT) >= 1);
    }

    @Test
    void legalBatchExportWritesUnderRootTablesDir() throws Exception {
        mockMvc.perform(post("/api/fastdoc/tables/export?exportDir=batch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        assertTrue(Files.isDirectory(ROOT.resolve("batch").resolve("tables")));
        assertTrue(countMdFiles(ROOT.resolve("batch").resolve("tables")) >= 1);
    }

    @Test
    void traversalExportDirRejectedWithoutCreatingOutsideRoot() throws Exception {
        Path escapeTarget = Path.of("target/fastdoc-sandbox-escape");
        mockMvc.perform(post("/api/fastdoc/database/export?exportDir=../fastdoc-sandbox-escape"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        assertFalse(Files.exists(escapeTarget), "穿越目标目录不得被创建");
    }

    @Test
    void absoluteExportDirRejectedWithNoFileChangeOutsideRoot() throws Exception {
        Path outside = Path.of("target/fastdoc-abs-outside");
        Files.createDirectories(outside);
        Path marker = outside.resolve("marker.txt");
        Files.writeString(marker, "untouched");
        mockMvc.perform(post("/api/fastdoc/database/export?exportDir=" + outside.toAbsolutePath()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        assertEquals("untouched", Files.readString(marker));
        try (var stream = Files.list(outside)) {
            assertEquals(1, stream.count(), "根外目录内不得有新增文件");
        }
    }

    @Test
    void junctionEscapeRejectedOnWindows() throws Exception {
        Assumptions.assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("win"),
                "junction 用例仅 Windows 执行");
        Path realOutside = Path.of("target/fastdoc-junction-real").toAbsolutePath();
        Files.createDirectories(realOutside);
        Files.createDirectories(ROOT);
        Path link = ROOT.resolve("link-j");
        if (!Files.exists(link)) {
            Process mklink = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J",
                    link.toAbsolutePath().toString(), realOutside.toString())
                    .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            Assumptions.assumeTrue(mklink.waitFor() == 0, "mklink /J 创建失败，跳过 junction 用例");
        }
        mockMvc.perform(post("/api/fastdoc/database/export?exportDir=link-j"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
        try (var stream = Files.list(realOutside)) {
            assertEquals(0, stream.count(), "junction 逃逸目标目录不得出现任何文件");
        }
    }
}
