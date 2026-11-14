package com.gallop.fastddd.consumer.fastdoc;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * FastDoc HTTP 导出<b>最终落盘边界</b>沙箱测试（ECO-RELEASE-05，真实 HTTP 入口复现与验收）。
 *
 * <p>与 S3 的 {@link FastdocHttpExportSandboxTest}（输入层：exportDir/tableName 校验）互补，本类
 * 覆盖校验点与实际写入点之间的三段服务内部路径，全部为静态预置（链接在请求前创建，不制造 TOCTOU）：
 * <ol>
 *   <li>批量导出服务内部 {@code tables} 子目录段：合法 exportDir 下预置 tables junction
 *       指向可信根外时，写入/覆盖不得越界；</li>
 *   <li>批量导出的数据库表名进入文件名：H2 引号表名（含 {@code ../../../} 片段）经
 *       {@code tablesDir.resolve(tableName + ".md")} 后最终路径仍必须受可信根约束；</li>
 *   <li>覆盖语义：可信根内的既有导出文件被重导出覆盖属既有语义（合法请求，登记不改），
 *       可信根外的既有文件任何情况下不得被改动。</li>
 * </ol>
 * 全部路径限定在场景工程 target/ 沙箱内，不触碰真实目录；各用例自清理上次运行残留（invoker
 * 默认不清 clone 目录），保证修复前/后两次运行同一断言可对照。
 */
@SpringBootTest(classes = FastdocMinimalApp.class,
        properties = "fastddd.fastdoc.export.root-dir=target/fastdoc-export-sandbox")
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class FastdocHttpFinalPathSandboxTest {

    private static final Path ROOT = Path.of("target/fastdoc-export-sandbox");

    /** 向量 3 表名：进入文件名后经 tablesDir 三级 .. 落到 target/（可信根外一层） */
    private static final String HOSTILE_TABLE = "../../../z-fastdoc-escape-vector3";
    private static final Path HOSTILE_ESCAPE_FILE = Path.of("target/z-fastdoc-escape-vector3.md");

    @Autowired
    MockMvc mockMvc;

    @Autowired
    DataSource dataSource;

    @BeforeAll
    void createHostileQuotedTableName() throws Exception {
        try (Connection c = dataSource.getConnection(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS \"" + HOSTILE_TABLE + "\" (ID BIGINT PRIMARY KEY)");
        } catch (SQLException e) {
            Assumptions.assumeTrue(false, "H2 不接受含路径片段的引号表名，向量 3 以模块级 mock 用例为准: " + e.getMessage());
        }
    }

    @Test
    void batchThroughPresetTablesJunctionRejectedWithoutOutsideChange() throws Exception {
        Assumptions.assumeTrue(isWindows(), "junction 用例仅 Windows 执行");
        Path outside = Path.of("target/ecorelease05-jx-outside").toAbsolutePath();
        // 顺序要求：先清 exportDir（上次残留的 junction 会被下钻删除其目标内容——均为沙箱内目录），
        // 再布置根外哨兵，避免哨兵被残留链接的清理误删
        Path exportDir = ROOT.resolve("jx");
        resetDir(exportDir);
        resetDir(outside);
        Files.writeString(outside.resolve("marker.txt"), "untouched");
        createJunction(exportDir.resolve("tables"), outside);

        mockMvc.perform(post("/api/fastdoc/tables/export?exportDir=jx"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false));

        List<Path> outsideContent;
        try (Stream<Path> s = Files.list(outside)) {
            outsideContent = s.toList();
        }
        assertEquals(List.of(outside.resolve("marker.txt")), outsideContent,
                "tables 段指向根外时不得有任何根外写入: " + outsideContent);
        assertEquals("untouched", Files.readString(outside.resolve("marker.txt")));
    }

    @Test
    void hostileDbTableNameCannotEscapeRootInBatchFilename() throws Exception {
        Files.deleteIfExists(HOSTILE_ESCAPE_FILE);
        Path tables = ROOT.resolve("tn3").resolve("tables");
        resetDir(ROOT.resolve("tn3"));

        mockMvc.perform(post("/api/fastdoc/tables/export?exportDir=tn3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertFalse(Files.exists(HOSTILE_ESCAPE_FILE), "含路径片段的表名不得在可信根外产生文件");
        assertTrue(Files.exists(tables.resolve("T_FASTDOC_DEMO.md")), "正常表导出不受影响");
    }

    @Test
    void existingOutsideFileSurvivesWhenTablesJunctionPointsOutside() throws Exception {
        Assumptions.assumeTrue(isWindows(), "junction 用例仅 Windows 执行");
        Path outside = Path.of("target/ecorelease05-ow-outside").toAbsolutePath();
        Path exportDir = ROOT.resolve("ow3");
        resetDir(exportDir);
        resetDir(outside);
        Files.writeString(outside.resolve("T_FASTDOC_DEMO.md"), "SENTINEL-KEEP-ME");
        Files.writeString(outside.resolve("other.txt"), "also-kept");
        createJunction(exportDir.resolve("tables"), outside);

        mockMvc.perform(post("/api/fastdoc/tables/export?exportDir=ow3"))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.success").value(false));

        assertEquals("SENTINEL-KEEP-ME", Files.readString(outside.resolve("T_FASTDOC_DEMO.md")),
                "根外既有普通文件不得被覆盖");
        assertEquals("also-kept", Files.readString(outside.resolve("other.txt")));
        List<Path> outsideContent;
        try (Stream<Path> s = Files.list(outside)) {
            outsideContent = s.sorted().toList();
        }
        assertEquals(List.of(outside.resolve("T_FASTDOC_DEMO.md"), outside.resolve("other.txt")).stream().sorted()
                .toList(), outsideContent, "根外目录内容零新增: " + outsideContent);
    }

    @Test
    void withinRootReExportOverwriteKeepsWorkingAsDocumented() throws Exception {
        Path tables = ROOT.resolve("owin").resolve("tables");
        resetDir(ROOT.resolve("owin"));
        Files.createDirectories(tables);
        Path existing = tables.resolve("T_FASTDOC_DEMO.md");
        Files.writeString(existing, "OLD-EXPORT");

        mockMvc.perform(post("/api/fastdoc/tables/export?exportDir=owin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertNotEquals("OLD-EXPORT", Files.readString(existing),
                "根内既有导出文件的覆盖=重导出语义（合法请求），修复不得矫枉过正");
        List<Path> produced;
        try (Stream<Path> s = Files.walk(ROOT.resolve("owin"))) {
            produced = s.filter(p -> p.toString().endsWith(".md")).toList();
        }
        // 注：H2 元数据批量导出会带上 INFORMATION_SCHEMA 系统表（存量行为，非本批范围），
        // 这里只约束目标表恰好一份且无路径形态异常的产物
        assertEquals(1, produced.stream().filter(p -> p.getFileName().toString().equals("T_FASTDOC_DEMO.md")).count(),
                "根内目标表恰好一份导出文件");
        assertTrue(produced.stream().noneMatch(p -> p.getFileName().toString().contains("..")),
                "根内不得出现含路径片段的产物名");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }

    /** 递归清掉上次运行残留后确保目录存在（早退分支同样要建目录，供哨兵文件写入） */
    private static void resetDir(Path dir) throws Exception {
        if (Files.exists(dir)) {
            try (Stream<Path> s = Files.walk(dir)) {
                s.sorted(Comparator.reverseOrder()).forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception e) {
                        throw new IllegalStateException("清理失败: " + p, e);
                    }
                });
            }
            assertFalse(Files.exists(dir));
        }
        Files.createDirectories(dir);
    }

    private static void createJunction(Path link, Path target) throws Exception {
        Files.createDirectories(target);
        Files.createDirectories(link.getParent());
        if (!Files.exists(link)) {
            Process mklink = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J",
                    link.toAbsolutePath().toString(), target.toAbsolutePath().toString())
                    .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
            Assumptions.assumeTrue(mklink.waitFor() == 0, "mklink /J 创建失败，跳过 junction 用例");
        }
    }
}
