package com.gallop.fastddd.fastdoc.web;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * FastDocExportPathGuard 单元测试（ECO-RELEASE-04 / S3，A4 阻断项 A3）。
 *
 * <p>覆盖：默认根、合法相对子目录、目录穿越、绝对路径/盘符/UNC、盘相对路径、
 * 符号链接逃逸、深层不存在路径、表名校验。全部断言在 guard 层完成，
 * 不依赖真实 HTTP 层（HTTP 行为见 FastDocControllerExportTest 与 fastdoc-minimal 场景）。
 */
class FastDocExportPathGuardTest {

    @TempDir
    Path tempDir;

    private FastDocExportPathGuard guardWithTempRoot() {
        return new FastDocExportPathGuard(tempDir.resolve("fastdoc-root").toString());
    }

    @Test
    void nullOrBlankExportDirResolvesToTrustedRoot() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        Path resolved = guard.resolveExportDir(null);
        assertEquals(guard.getRootDir(), resolved);
        assertEquals(guard.getRootDir(), guard.resolveExportDir("   "));
        // 根目录被确保存在（可信配置路径）
        assertTrue(Files.isDirectory(resolved));
    }

    @Test
    void relativeSubdirInsideRootIsAccepted() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        Path resolved = guard.resolveExportDir("sub/deep");
        assertEquals(guard.getRootDir().resolve("sub").resolve("deep"), resolved);
        assertTrue(resolved.startsWith(guard.getRootDir()));
    }

    @Test
    void normalizeThatStaysInsideRootIsAccepted() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        // sub/../sub2 规范化后仍在根内 → 允许
        Path resolved = guard.resolveExportDir("sub/../sub2");
        assertEquals(guard.getRootDir().resolve("sub2"), resolved);
    }

    @Test
    void parentTraversalIsRejected() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("../escape"));
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("sub/../../escape"));
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir(".."));
    }

    @Test
    void absolutePathFormsAreRejected() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        // 真实绝对路径（平台无关构造：tempDir 一定是绝对路径）
        String absoluteOutside = tempDir.resolve("outside").toString();
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir(absoluteOutside));
        // POSIX 绝对 / Windows 根相对 / 盘符 / 盘相对 / UNC：显式前缀规则平台无关地拒绝
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("/etc/passwd"));
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("\\\\server\\share"));
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("C:" + (System.getProperty("os.name", "").toLowerCase().contains("win") ? "\\Windows" : "x")));
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("C:relative"));
    }

    @Test
    void nonexistentDeepPathIsAllowedWhenAncestorChainIsRoot() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        // 根存在、其余段不存在：最近已存在祖先=根，实路径在根内 → 允许（创建由后续服务完成）
        Path resolved = guard.resolveExportDir("a/b/c");
        assertEquals(guard.getRootDir().resolve("a").resolve("b").resolve("c"), resolved);
    }

    @Test
    void symlinkEscapeIsRejected() throws IOException {
        Path outside = tempDir.resolve("outside-real");
        Files.createDirectories(outside);
        Path root = tempDir.resolve("fastdoc-root");
        Files.createDirectories(root);
        Path link = root.resolve("link-to-outside");
        try {
            Files.createSymbolicLink(link, outside);
        } catch (UnsupportedOperationException | IOException e) {
            Assumptions.assumeTrue(false, "当前环境不支持创建符号链接，跳过实路径校验用例: " + e.getMessage());
        }
        FastDocExportPathGuard guard = new FastDocExportPathGuard(root.toString());
        // 链接本身即目标（词法在根内、实路径在根外）→ 拒绝
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("link-to-outside"));
        // 链接下再接子目录（最近已存在祖先是链接）→ 同样拒绝
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("link-to-outside/sub"));
    }

    @Test
    void symlinkToInsideRootIsAccepted() throws IOException {
        Path root = tempDir.resolve("fastddd-root"); // 注意：与 guardWithTempRoot 同名，确保链接语义清晰
        Files.createDirectories(root.resolve("real-sub"));
        Path link = root.resolve("link-to-inside");
        try {
            Files.createSymbolicLink(link, root.resolve("real-sub"));
        } catch (UnsupportedOperationException | IOException e) {
            Assumptions.assumeTrue(false, "当前环境不支持创建符号链接，跳过: " + e.getMessage());
        }
        FastDocExportPathGuard guard = new FastDocExportPathGuard(root.toString());
        Path resolved = guard.resolveExportDir("link-to-inside");
        assertTrue(resolved.startsWith(guard.getRootDir()));
    }

    @Test
    void junctionEscapeIsRejectedOnWindows() throws Exception {
        Assumptions.assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("win"),
                "junction 用例仅 Windows 执行");
        Path outside = tempDir.resolve("outside-real");
        Files.createDirectories(outside);
        Path root = tempDir.resolve("fastdoc-root");
        Files.createDirectories(root);
        Path link = root.resolve("link-out");
        // 目录 junction 无需管理员权限；Windows 上 Java toRealPath 会解析 reparse point（含 junction）
        Process mklink = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J",
                link.toString(), outside.toString())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        Assumptions.assumeTrue(mklink.waitFor() == 0, "mklink /J 创建失败，跳过 junction 用例");

        FastDocExportPathGuard guard = new FastDocExportPathGuard(root.toString());
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("link-out"));
        assertThrows(IllegalArgumentException.class, () -> guard.resolveExportDir("link-out/newsub"));
        // 逃逸目标目录本身零变化
        try (var stream = Files.list(outside)) {
            assertEquals(0, stream.count());
        }
    }

    @Test
    void tableNameValidationAcceptsCommonNames() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        assertDoesNotThrow(() -> guard.validateTableName("t_user"));
        assertDoesNotThrow(() -> guard.validateTableName("T_FASTDOC_DEMO"));
        assertDoesNotThrow(() -> guard.validateTableName("schema.table"));
        assertDoesNotThrow(() -> guard.validateTableName("order-items"));
    }

    @Test
    void tableNameValidationRejectsPathDangerousForms() {
        FastDocExportPathGuard guard = guardWithTempRoot();
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName(null));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("  "));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("a/b"));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("a\\b"));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("a:b"));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName(".."));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("a..b"));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName(".hidden"));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("name."));
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("a" + (char) 0 + "b")); // 控制字符
        assertThrows(IllegalArgumentException.class, () -> guard.validateTableName("a".repeat(129)));
    }

    // ========== ECO-RELEASE-05：写入层最终路径校验（checkWriteTarget） ==========

    @Test
    void writeTargetInsideRootIsAccepted() throws IOException {
        FastDocExportPathGuard guard = guardWithTempRoot();
        Path root = guard.getRootDir();
        Files.createDirectories(root.resolve("sub").resolve("tables"));
        // 不存在的新文件（父目录在根内）
        assertDoesNotThrow(() -> guard.checkWriteTarget(root.resolve("sub").resolve("tables").resolve("T_USER.md")));
        // 已存在的根内文件（重导出覆盖语义：写入层不禁止根内覆盖）
        Path existing = root.resolve("sub").resolve("tables").resolve("T_USER.md");
        Files.writeString(existing, "old");
        assertDoesNotThrow(() -> guard.checkWriteTarget(existing));
    }

    @Test
    void writeTargetOutsideRootIsRejected() throws IOException {
        FastDocExportPathGuard guard = guardWithTempRoot();
        Files.createDirectories(guard.getRootDir());
        Path outside = tempDir.resolve("outside-file.md");
        assertThrows(IllegalArgumentException.class, () -> guard.checkWriteTarget(outside));
    }

    @Test
    void writeTargetWithTraversalSegmentsInFileNameIsRejected() throws IOException {
        FastDocExportPathGuard guard = guardWithTempRoot();
        Files.createDirectories(guard.getRootDir());
        Path tablesDir = guard.getRootDir().resolve("sub").resolve("tables");
        Files.createDirectories(tablesDir);
        // 数据库表名携带路径片段进入文件名后（resolve 不规范化），最终路径词法越根 → 拒绝
        // （正斜杠在 Windows/Linux 均为分隔符，本用例跨平台可移植）
        assertThrows(IllegalArgumentException.class,
                () -> guard.checkWriteTarget(tablesDir.resolve("../../../z-evil.md")));
    }

    @Test
    void writeTargetWithBackslashTraversalInFileNameIsRejectedOnWindows() throws IOException {
        // 反斜杠穿越仅 Windows 有穿越语义（\ 为分隔符）；Linux 下 \ 非分隔符，整串为合法
        // 单文件名词法落在根内（写入被钉死在根内，无越界事实），且输入层 validateTableName
        // 已拒绝任何含 \ 的表名——写入层对这类串无拦截义务，故用例仅 Windows 执行。
        Assumptions.assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("win"),
                "反斜杠穿越用例仅 Windows 执行");
        FastDocExportPathGuard guard = guardWithTempRoot();
        Files.createDirectories(guard.getRootDir());
        Path tablesDir = guard.getRootDir().resolve("sub").resolve("tables");
        Files.createDirectories(tablesDir);
        assertThrows(IllegalArgumentException.class,
                () -> guard.checkWriteTarget(tablesDir.resolve("..\\..\\..\\z-evil.md")));
    }

    @Test
    void writeTargetThroughTablesJunctionIsRejectedOnWindows() throws Exception {
        Assumptions.assumeTrue(System.getProperty("os.name", "").toLowerCase().contains("win"),
                "junction 用例仅 Windows 执行");
        Path outside = tempDir.resolve("outside-tables");
        Files.createDirectories(outside);
        Files.writeString(outside.resolve("keep.txt"), "keep");
        Path root = tempDir.resolve("fastdoc-root");
        Path tablesLink = root.resolve("sub").resolve("tables");
        Files.createDirectories(tablesLink.getParent());
        Process mklink = new ProcessBuilder("cmd.exe", "/c", "mklink", "/J",
                tablesLink.toString(), outside.toString())
                .redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start();
        Assumptions.assumeTrue(mklink.waitFor() == 0, "mklink /J 创建失败，跳过 junction 用例");

        FastDocExportPathGuard guard = new FastDocExportPathGuard(root.toString());
        // tables 段本身（建目录前校验）经链接实路径越根 → 拒绝
        assertThrows(IllegalArgumentException.class, () -> guard.checkWriteTarget(tablesLink));
        // 经链接后的最终文件 → 同样拒绝
        assertThrows(IllegalArgumentException.class, () -> guard.checkWriteTarget(tablesLink.resolve("T_USER.md")));
        // 链接目标零变化
        try (var stream = Files.list(outside)) {
            assertEquals(1, stream.count());
        }
        assertEquals("keep", Files.readString(outside.resolve("keep.txt")));
    }
}
