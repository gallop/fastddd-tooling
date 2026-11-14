package com.gallop.fastddd.tooling.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 代码生成结果（SPLIT-003 结构化契约）。
 *
 * <p>取代历史 {@code Map<String, String>}（相对路径 → 文件内容）零散返回值，提供自描述的结构化结果。
 * 不可变；经 {@link Builder} 构造。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public final class GenerationResult {

    private final String tableName;
    private final Map<String, String> files;

    private GenerationResult(Builder b) {
        this.tableName = Objects.requireNonNull(b.tableName, "tableName 不能为空");
        this.files = Collections.unmodifiableMap(new LinkedHashMap<>(b.files));
    }

    public String getTableName() { return tableName; }

    /** 生成的文件映射（相对路径 → 内容），不可变。 */
    public Map<String, String> getFiles() { return files; }

    /** 生成的文件数量。 */
    public int getFileCount() { return files.size(); }

    public static Builder builder(String tableName) {
        return new Builder(tableName);
    }

    /** 可变构建器，{@link #build()} 产出不可变 {@link GenerationResult}。 */
    public static final class Builder {
        private final String tableName;
        private final Map<String, String> files = new LinkedHashMap<>();

        private Builder(String tableName) {
            this.tableName = tableName;
        }

        public Builder addFile(String relativePath, String content) {
            this.files.put(relativePath, content);
            return this;
        }

        public Builder putAll(Map<String, String> files) {
            if (files != null) {
                this.files.putAll(files);
            }
            return this;
        }

        public GenerationResult build() { return new GenerationResult(this); }
    }
}
