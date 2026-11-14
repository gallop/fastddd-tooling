package com.gallop.fastddd.tooling.api;

/**
 * 数据库内省器工厂契约（SPLIT-004）。
 *
 * <p>消费方（如 Studio）面向此契约按每次提交的连接信息构造 {@link DatabaseIntrospector}，而无需依赖生成器
 * 内部的 {@code DatabaseConfig} 具体类或 {@code DatabaseMetadataService} 构造器。实现由
 * {@code fastddd-code-generator} 以 Spring {@code @Component} 形式提供，经标准自动配置注入。</p>
 *
 * <p>典型用法：</p>
 * <pre>{@code
 * @RequiredArgsConstructor
 * public class StudioService {
 *     private final DatabaseIntrospectorFactory introspectorFactory;
 *
 *     public List<TableMetadata> list(String url, String user, String pwd) {
 *         DatabaseConnection conn = DatabaseConnection.builder(url)
 *                 .username(user).password(pwd).build();
 *         return introspectorFactory.create(conn).getAllTables();
 *     }
 * }
 * }</pre>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
public interface DatabaseIntrospectorFactory {

    /**
     * 按连接描述构造一个内省器。
     *
     * @param connection 数据库连接描述（不可为空）
     * @return 绑定该连接的内省器
     */
    DatabaseIntrospector create(DatabaseConnection connection);
}
