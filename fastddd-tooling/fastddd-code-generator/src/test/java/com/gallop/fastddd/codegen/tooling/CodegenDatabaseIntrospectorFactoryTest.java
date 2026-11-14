package com.gallop.fastddd.codegen.tooling;

import com.gallop.fastddd.codegen.config.DatabaseConfig;
import com.gallop.fastddd.codegen.service.DatabaseMetadataService;
import com.gallop.fastddd.tooling.api.DatabaseConnection;
import com.gallop.fastddd.tooling.api.DatabaseIntrospector;
import com.gallop.fastddd.tooling.api.DatabaseIntrospectorFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SPLIT-004 Phase A：{@link CodegenDatabaseIntrospectorFactory} 装配与字段映射测试。
 *
 * <p>锁定：工厂实现契约接口、返回 {@link DatabaseIntrospector}（非具体类）、
 * {@link DatabaseConnection} 全字段正确映射到内部 {@link DatabaseConfig}、默认驱动回落。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class CodegenDatabaseIntrospectorFactoryTest {

    @Test
    @DisplayName("类型装配：工厂可赋值契约接口")
    void factoryImplementsContract() {
        assertThat(DatabaseIntrospectorFactory.class)
                .isAssignableFrom(CodegenDatabaseIntrospectorFactory.class);
    }

    @Test
    @DisplayName("create：返回 DatabaseIntrospector（非具体类泄漏）+ 全字段映射")
    void create_returnsIntrospectorAndMapsAllFields() throws Exception {
        CodegenDatabaseIntrospectorFactory factory = new CodegenDatabaseIntrospectorFactory();

        DatabaseConnection conn = DatabaseConnection.builder("jdbc:mysql://localhost:3306/db")
                .username("alice").password("secret")
                .driverClassName("org.postgresql.Driver")
                .catalog("db").schema("public").tablePrefix("t_")
                .build();

        DatabaseIntrospector introspector = factory.create(conn);

        // 返回契约接口类型，不泄漏具体类
        assertThat(introspector).isInstanceOf(DatabaseIntrospector.class);
        assertThat(introspector).isInstanceOf(DatabaseMetadataService.class); // 实际实现

        // 内部 DatabaseConfig 全字段映射（反射读取，不污染 API）
        Field configField = DatabaseMetadataService.class.getDeclaredField("databaseConfig");
        configField.setAccessible(true);
        DatabaseConfig mapped = (DatabaseConfig) configField.get(introspector);

        assertThat(mapped.getUrl()).isEqualTo("jdbc:mysql://localhost:3306/db");
        assertThat(mapped.getUsername()).isEqualTo("alice");
        assertThat(mapped.getPassword()).isEqualTo("secret");
        assertThat(mapped.getDriverClassName()).isEqualTo("org.postgresql.Driver");
        assertThat(mapped.getCatalog()).isEqualTo("db");
        assertThat(mapped.getSchema()).isEqualTo("public");
        assertThat(mapped.getTablePrefix()).isEqualTo("t_");
    }

    @Test
    @DisplayName("create：未指定驱动时回落默认 MySQL 驱动")
    void create_appliesDefaultDriverWhenAbsent() throws Exception {
        CodegenDatabaseIntrospectorFactory factory = new CodegenDatabaseIntrospectorFactory();
        DatabaseIntrospector introspector = factory.create(
                DatabaseConnection.builder("jdbc:mysql://localhost/db").username("u").build());

        Field configField = DatabaseMetadataService.class.getDeclaredField("databaseConfig");
        configField.setAccessible(true);
        DatabaseConfig mapped = (DatabaseConfig) configField.get(introspector);
        assertThat(mapped.getDriverClassName()).isEqualTo(DatabaseConnection.DEFAULT_DRIVER);
    }

    @Test
    @DisplayName("create：null 连接抛 IllegalArgumentException")
    void create_rejectsNullConnection() {
        CodegenDatabaseIntrospectorFactory factory = new CodegenDatabaseIntrospectorFactory();
        assertThatThrownBy(() -> factory.create(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
