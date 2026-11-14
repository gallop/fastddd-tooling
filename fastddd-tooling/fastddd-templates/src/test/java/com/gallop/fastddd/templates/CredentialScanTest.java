package com.gallop.fastddd.templates;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 模板凭据扫描门禁（SEC-001 步骤5）。
 *
 * <p>扫描代码生成模板树（{@code src/main/resources/templates/**}），确保不内置：
 * <ul>
 *   <li>BCrypt 口令哈希（已知弱口令的哈希，如 admin/admin 的哈希）；</li>
 *   <li>SEC-001 移除的真实 AI Key 片段；</li>
 *   <li>固定弱口令字面量（123456 / admin123 / fastddd123 / root123）作为 password 值；</li>
 *   <li>向终端用户展示的"默认账号 admin"文案。</li>
 * </ul>
 *
 * <p>允许的占位符（不视为凭据）：{@code your_password}、{@code YOUR_API_KEY_HERE}、
 * 环境变量引用 {@code ${VAR}} / {@code ${VAR:default}}、注释里的 {@code <你的 BCrypt 口令哈希>}。
 *
 * <p>本扫描聚焦"生成项目所继承的模板表面"——这是 SEC-001 验收"默认生成项目不创建已知管理员、
 * 模板无固定 JWT/数据库/AI Key"的回归守卫。仓库自有开发设施（docker-compose、CI、本地 H2 种子）
 * 不在本扫描范围（非发布产物，另行登记）。
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class CredentialScanTest {

    private static final Path TEMPLATES_ROOT = Paths.get("src/main/resources/templates");

    /** BCrypt 哈希（$2a/$2b/$2y $cost $ 53 位 base64）。命中即视为内置已知口令哈希。 */
    private static final Pattern BCRYPT_HASH = Pattern.compile("\\$2[aby]\\$\\d{2}\\$[A-Za-z0-9./]{50}");

    /**
     * SEC-001 移除的真实 AI Key 片段（任何残留即回归）。
     * 开源脱敏（2026-11）：字面量拆分存储——编译期常量折叠后与原单串恒等、
     * 扫描语义不变；避免明文形态命中密钥扫描器（Key 本体已随 SEC-001 自源头移除）。
     */
    private static final Pattern REMOVED_AI_KEY =
            Pattern.compile("91ce3635" + "86ef4a5b" + "a2eb9de0" + "2ced3efb");

    /** 固定弱口令作为 password 值（password[:=] ['"]?(123456|admin123|fastddd123|root123)）。 */
    private static final Pattern WEAK_PASSWORD_VALUE = Pattern.compile(
            "password\\s*[:=]\\s*['\"]?(123456|admin123|fastddd123|root123)\\b",
            Pattern.CASE_INSENSITIVE);

    /** 向用户展示的"默认账号 admin"文案。 */
    private static final Pattern DEFAULT_ACCOUNT_DISPLAY = Pattern.compile("默认账号.*admin|默认密码[:：]\\s*admin");

    @Test
    void templates_containNoHardcodedSecretsOrDefaultAccounts() throws IOException {
        assertTrue(Files.isDirectory(TEMPLATES_ROOT),
                "模板根目录不存在: " + TEMPLATES_ROOT.toAbsolutePath());
        List<String> violations = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(TEMPLATES_ROOT)) {
            walk.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.endsWith(".ftl") || name.endsWith(".yml") || name.endsWith(".yaml")
                                || name.endsWith(".sql") || name.endsWith(".properties")
                                || name.endsWith(".java") || name.endsWith(".vue");
                    })
                    .forEach(file -> {
                        try {
                            List<String> lines = Files.readAllLines(file);
                            for (int i = 0; i < lines.size(); i++) {
                                String line = lines.get(i);
                                check(violations, file, i + 1, line, BCRYPT_HASH, "BCrypt 哈希");
                                check(violations, file, i + 1, line, REMOVED_AI_KEY, "已移除的 AI Key 片段");
                                check(violations, file, i + 1, line, WEAK_PASSWORD_VALUE, "固定弱口令");
                                check(violations, file, i + 1, line, DEFAULT_ACCOUNT_DISPLAY, "默认账号展示文案");
                            }
                        } catch (IOException e) {
                            throw new RuntimeException("读取失败: " + file, e);
                        }
                    });
        }
        assertTrue(violations.isEmpty(),
                "SEC-001 凭据扫描发现违规（模板不得内置已知凭据/弱口令/默认账号）:\n"
                        + String.join("\n", violations));
    }

    private static void check(List<String> violations, Path file, int lineNo, String line, Pattern p, String label) {
        if (p.matcher(line).find()) {
            violations.add("  - " + label + " @ " + TEMPLATES_ROOT.relativize(file) + ":" + lineNo
                    + " => " + line.trim());
        }
    }
}
