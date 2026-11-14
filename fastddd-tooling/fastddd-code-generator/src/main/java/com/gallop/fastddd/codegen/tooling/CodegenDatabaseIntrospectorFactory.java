package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.config.DatabaseConfig;
import com.gallop.fastddd.codegen.service.DatabaseMetadataService;
import com.gallop.fastddd.tooling.api.DatabaseConnection;
import com.gallop.fastddd.tooling.api.DatabaseIntrospector;
import com.gallop.fastddd.tooling.api.DatabaseIntrospectorFactory;
import org.springframework.stereotype.Component;

/**
 * Tooling 稳定契约 {@link DatabaseIntrospectorFactory} 的官方实现（SPLIT-004 Phase A）。
 *
 * <p>将契约侧 {@link DatabaseConnection} 映射为生成器内部 {@link DatabaseConfig}，构造
 * {@link DatabaseMetadataService}（已实现 {@link DatabaseIntrospector}）并以契约接口返回。
 * 从而让消费方（如 Studio）彻底脱离 {@code codegen.config.DatabaseConfig} 与
 * {@code DatabaseMetadataService} 具体类。</p>
 *
 * <p>每次调用返回一个绑定新连接的实例（与历史上 Studio 的 {@code new DatabaseMetadataService(dbConfig)}
 * 逐连接构造语义一致）。{@code DatabaseConfig} 不出现在返回类型中，仅为本实现内部桥接。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
@Component
public class CodegenDatabaseIntrospectorFactory implements DatabaseIntrospectorFactory {

    @Override
    public DatabaseIntrospector create(DatabaseConnection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("DatabaseConnection 不能为空");
        }
        DatabaseConfig config = new DatabaseConfig();
        config.setUrl(connection.getUrl());
        config.setUsername(connection.getUsername());
        config.setPassword(connection.getPassword());
        config.setDriverClassName(connection.getDriverClassName());
        config.setCatalog(connection.getCatalog());
        config.setSchema(connection.getSchema());
        config.setTablePrefix(connection.getTablePrefix());
        return new DatabaseMetadataService(config);
    }
}
