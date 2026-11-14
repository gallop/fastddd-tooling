package com.gallop.fastddd.fastdoc.web;

import com.gallop.fastddd.fastdoc.FastDocExportFileGuard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * FastDoc HTTP 导出路径防护（ECO-RELEASE-04 / S3，A4 阻断项 A3 整改）
 *
 * <p>在<b>任何目录创建/文件写入发生之前</b>，对不可信的 HTTP 输入（{@code exportDir} 请求参数、
 * {@code tableName} 路径变量）做白名单式校验，防止：
 * <ul>
 *   <li>目录穿越（{@code ../} 序列越出可信根）；</li>
 *   <li>绝对路径绕过（POSIX {@code /x}、Windows 盘符 {@code C:\x} / 盘相对 {@code C:x}、UNC {@code \\x}）；</li>
 *   <li>符号链接逃逸（已存在路径段的 toRealPath 必须仍落在可信根的真实路径内）；</li>
 *   <li>非预期覆盖 / 文件名注入（tableName 禁止路径分隔符、控制字符等，文件名由框架生成）。</li>
 * </ul>
 *
 * <p>安全边界不依赖 {@code @PreAuthorize}：即使端点被放宽为 permitAll，写入也被钉死在可信根内。
 * 仅约束 HTTP 边界；core 的本地/CLI 可信路径不经此类，行为不变。
 *
 * <p>校验顺序（先词法后实路径，全部在任何 create/write 之前完成）：
 * 根目录存在化（可信配置路径）→ 词法包含（resolve+normalize 后必须 startsWith 根）→
 * 最近已存在祖先的 toRealPath 包含（防符号链接逃逸）。
 *
 * <p><b>写入层（ECO-RELEASE-05 补齐）</b>：本类同时实现 {@link FastDocExportFileGuard}，
 * 在每次实际写文件与 tables 子目录创建之前以最终 Path 回调 {@link #checkWriteTarget(Path)}，
 * 覆盖输入层校验与实际写入之间的服务内部路径段（tables 段经符号链接指向根外、数据库表名
 * 携带路径片段进入文件名后越界、以及由此产生的根外覆盖）。写入层校验与实际写入之间仍存在
 * 本地文件系统竞态（TOCTOU），残留风险独立登记于整改记录。
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public class FastDocExportPathGuard implements FastDocExportFileGuard {

    private static final int MAX_TABLE_NAME_LENGTH = 128;

    /** 可信根（绝对规范化路径） */
    private final Path rootDir;

    public FastDocExportPathGuard(String configuredRoot) {
        Path root;
        if (configuredRoot == null || configuredRoot.trim().isEmpty()) {
            // 与 core 默认导出目录（相对 fastdoc-export）保持一致落点，默认用法不变
            root = Paths.get(System.getProperty("user.dir")).resolve("fastdoc-export");
        } else {
            root = Paths.get(configuredRoot.trim());
        }
        this.rootDir = root.toAbsolutePath().normalize();
    }

    /** 可信根（绝对规范化），测试与日志用 */
    public Path getRootDir() {
        return rootDir;
    }

    /**
     * 校验并解析 HTTP exportDir 请求参数 → 可信根内的绝对目标目录。
     *
     * @param exportDir 不可信 HTTP 输入；null/空白表示直接使用可信根
     * @return 可信根内的绝对路径（尚未创建，由调用方在通过校验后使用）
     * @throws IllegalArgumentException 任何形式的越界/非法输入（含根目录不可用）
     */
    public Path resolveExportDir(String exportDir) {
        // 可信配置路径：确保根存在（首次调用创建），失败则拒绝全部 HTTP 导出（fail-closed）
        Path rootReal;
        try {
            Files.createDirectories(rootDir);
            rootReal = rootDir.toRealPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("导出根目录不可用: " + rootDir, e);
        }

        if (exportDir == null || exportDir.trim().isEmpty()) {
            return rootDir;
        }
        String trimmed = exportDir.trim();

        // 绝对路径 / 盘符（含盘相对 C:x）/ UNC 前缀：HTTP 输入一律不允许指定根外起点
        if (trimmed.startsWith("/") || trimmed.startsWith("\\")
                || trimmed.matches("^[A-Za-z]:.*")) {
            throw new IllegalArgumentException("exportDir 不允许绝对路径/盘符/UNC: " + trimmed);
        }

        final Path userPath;
        try {
            userPath = Paths.get(trimmed);
        } catch (java.nio.file.InvalidPathException e) {
            throw new IllegalArgumentException("exportDir 含非法路径字符", e);
        }
        if (userPath.isAbsolute() || userPath.getRoot() != null) {
            throw new IllegalArgumentException("exportDir 不允许绝对路径: " + trimmed);
        }

        // 词法包含：normalize 抵消 ../ 与 ./，越出可信根即拒绝
        Path target = rootDir.resolve(trimmed).normalize();
        if (!target.startsWith(rootDir)) {
            throw new IllegalArgumentException("exportDir 越出可信导出根: " + trimmed);
        }

        // 实路径包含：目标路径中已存在的部分（含符号链接）解析后必须仍在可信根真实路径内
        Path existing = nearestExistingAncestor(target);
        Path existingReal;
        try {
            existingReal = existing.toRealPath();
        } catch (IOException e) {
            throw new IllegalArgumentException("exportDir 无法解析真实路径: " + trimmed, e);
        }
        if (!existingReal.startsWith(rootReal)) {
            throw new IllegalArgumentException("exportDir 经符号链接越出可信导出根: " + trimmed);
        }

        return target;
    }

    /**
     * 校验 HTTP tableName 路径变量（会进入导出文件名）。
     *
     * @throws IllegalArgumentException 含路径分隔符/控制字符/保留片段或超长
     */
    public void validateTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new IllegalArgumentException("表名不能为空");
        }
        String t = tableName.trim();
        if (t.length() > MAX_TABLE_NAME_LENGTH) {
            throw new IllegalArgumentException("表名超长（>" + MAX_TABLE_NAME_LENGTH + "）: " + t);
        }
        if (t.startsWith(".") || t.endsWith(".") || t.endsWith(" ")) {
            throw new IllegalArgumentException("表名不允许以点/空格开头或结尾: " + t);
        }
        if (t.contains("..")) {
            throw new IllegalArgumentException("表名不允许包含路径穿越片段: " + t);
        }
        for (int i = 0; i < t.length(); i++) {
            char c = t.charAt(i);
            // 路径分隔符、Windows 盘符/ADS 冒号、控制字符全部拒绝
            if (c == '/' || c == '\\' || c == ':' || c < 0x20 || c == 0x7F) {
                throw new IllegalArgumentException("表名含非法字符: " + t);
            }
        }
    }

    /**
     * 写入层最终路径校验（ECO-RELEASE-05）：与实际写入绑定。
     *
     * <p>以即将传给文件系统操作的最终 Path（写文件或 tables 子目录）做双重包含复核：
     * 词法包含（normalize 抵消 {@code ../} 与分隔符注入——数据库表名可携带路径片段）→
     * 最近已存在祖先 toRealPath 包含（tables 段等中间段经符号链接/junction 指向根外时拒绝，
     * 覆盖目标经链接落在根外时同样拒绝）。根不可用时 toRealPath 抛 IOException（fail-closed）。
     */
    @Override
    public void checkWriteTarget(Path target) throws IOException {
        Path absolute = target.toAbsolutePath().normalize();
        if (!absolute.startsWith(rootDir)) {
            throw new IllegalArgumentException("导出目标越出可信导出根: " + target);
        }
        Path rootReal = rootDir.toRealPath();
        Path existing = nearestExistingAncestor(absolute);
        if (!existing.toRealPath().startsWith(rootReal)) {
            throw new IllegalArgumentException("导出目标经符号链接越出可信导出根: " + target);
        }
    }

    /** 自 target 向上找最近已存在的祖先；全不存在则退回文件系统根（交由 toRealPath 失败） */
    private static Path nearestExistingAncestor(Path target) {
        Path current = target;
        while (current != null && !Files.exists(current)) {
            current = current.getParent();
        }
        return current != null ? current : target.getRoot();
    }
}
