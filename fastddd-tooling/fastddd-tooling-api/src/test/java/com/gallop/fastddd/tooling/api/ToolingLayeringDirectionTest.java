package com.gallop.fastddd.tooling.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Tooling 可以依赖 Framework，但不得反向依赖 Studio Pro 或 FastAgent。 */
class ToolingLayeringDirectionTest {

    private static final List<String> FORBIDDEN_IMPORT_PREFIXES = List.of(
            "com.gallop.fastddd.studio.",
            "com.gallop.fastagent.");

    private static final Path TOOLING_DIR = Paths.get("..").toAbsolutePath().normalize();

    @Test
    void toolingSourceShallNotImportStudioOrFastAgentPackages() throws IOException {
        assertThat(Files.isDirectory(TOOLING_DIR))
                .as("Tooling 聚合器目录存在: %s", TOOLING_DIR)
                .isTrue();

        List<String> violations = new ArrayList<>();
        try (DirectoryStream<Path> moduleDirs = Files.newDirectoryStream(TOOLING_DIR, "fastddd-*")) {
            for (Path moduleDir : moduleDirs) {
                Path mainJava = moduleDir.resolve("src/main/java");
                if (Files.isDirectory(mainJava)) {
                    scanMainJava(mainJava, violations);
                }
            }
        }

        assertThat(violations)
                .as("Tooling Community 源码不得 import Studio Pro/FastAgent 包；扫描根: %s", TOOLING_DIR)
                .isEmpty();
    }

    private void scanMainJava(Path mainJava, List<String> violations) throws IOException {
        Files.walkFileTree(mainJava, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!file.toString().endsWith(".java")) {
                    return FileVisitResult.CONTINUE;
                }
                try {
                    for (String line : Files.readAllLines(file)) {
                        String trimmed = line.trim();
                        if (!trimmed.startsWith("import ")) {
                            continue;
                        }
                        for (String prefix : FORBIDDEN_IMPORT_PREFIXES) {
                            if (trimmed.contains(prefix)) {
                                violations.add(file + ": " + trimmed);
                            }
                        }
                    }
                } catch (IOException e) {
                    throw new AssertionError("读取失败: " + file, e);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
