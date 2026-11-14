package com.gallop.fastddd.tooling.api;

import java.util.Objects;

/**
 * 数据库连接描述符（SPLIT-004）。
 *
 * <p>携带建立 {@link DatabaseIntrospector} 所需的全部原始连接参数（URL、凭据、驱动、catalog/schema、表前缀），
 * 使消费方（如 Studio）能在<strong>不依赖生成器内部 {@code DatabaseConfig}</strong> 的前提下，按用户每次提交的
 * 连接信息构造内省器。属通用连接描述，非任何消费方专用类型（不违反 SPLIT-004 禁止项）。</p>
 *
 * <p>不可变值对象，经 {@link Builder} 构造。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public final class DatabaseConnection {

    /** 默认驱动类名（MySQL Connector/J 8），与生成器内部 {@code DatabaseConfig} 默认一致。 */
    public static final String DEFAULT_DRIVER = "com.mysql.cj.jdbc.Driver";

    private final String url;
    private final String username;
    private final String password;
    private final String driverClassName;
    private final String catalog;
    private final String schema;
    private final String tablePrefix;

    private DatabaseConnection(Builder b) {
        this.url = Objects.requireNonNull(b.url, "url 不能为空");
        this.username = b.username;
        this.password = b.password;
        this.driverClassName = b.driverClassName != null ? b.driverClassName : DEFAULT_DRIVER;
        this.catalog = b.catalog;
        this.schema = b.schema;
        this.tablePrefix = b.tablePrefix;
    }

    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getDriverClassName() { return driverClassName; }
    public String getCatalog() { return catalog; }
    public String getSchema() { return schema; }
    public String getTablePrefix() { return tablePrefix; }

    public static Builder builder(String url) {
        return new Builder(url);
    }

    /** 可变构建器，{@link #build()} 产出不可变 {@link DatabaseConnection}。 */
    public static final class Builder {
        private final String url;
        private String username;
        private String password;
        private String driverClassName;
        private String catalog;
        private String schema;
        private String tablePrefix;

        private Builder(String url) {
            this.url = url;
        }

        public Builder username(String username) { this.username = username; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder driverClassName(String driverClassName) { this.driverClassName = driverClassName; return this; }
        public Builder catalog(String catalog) { this.catalog = catalog; return this; }
        public Builder schema(String schema) { this.schema = schema; return this; }
        public Builder tablePrefix(String tablePrefix) { this.tablePrefix = tablePrefix; return this; }

        public DatabaseConnection build() { return new DatabaseConnection(this); }
    }
}
