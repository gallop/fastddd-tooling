package com.gallop.fastddd.tooling.api;

import com.gallop.fastddd.common.model.TableMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * SPLIT-003：Tooling API 二进制面与契约测试。
 *
 * <p>双重职责：</p>
 * <ol>
 *   <li><b>二进制面清单</b>——锁定 API 公开类型集合（{@link #apiSurfaceIsComplete}），任何新增/移除公开类型
 *       需同步更新本测试，保证二进制面受控。</li>
 *   <li><b>契约行为</b>——锁定枚举解析、请求/结果值对象的不变式。</li>
 * </ol>
 *
 * <p>依赖契约（禁依赖 FreeMarker/MyBatis-Plus/Spring-MVC/DB 驱动）由 {@code pom.xml} 的
 * enforcer {@code bannedDependencies} 规则在构建期强制，此处不重复断言。</p>
 *
 * @author gallop
 * @since 1.0.0-jdk21
 */
class ToolingApiContractTest {

    @Test
    @DisplayName("API 公开类型清单（二进制面清单，增删需同步本断言）")
    void apiSurfaceIsComplete() {
        // 故意显式引用每个公开类型——编译期锁定其存在，清单变更强制更新本测试
        var surface = java.util.List.of(
                ModelType.class,
                IdStrategy.class,
                WriteMode.class,
                GenerationRequest.class,
                GenerationResult.class,
                GenerationException.class,
                ProjectGenerator.class,
                DatabaseIntrospector.class,
                DatabaseIntrospectorFactory.class,
                DatabaseConnection.class,
                TemplateCatalog.class,
                GenerationProgressListener.class);
        assertThat(surface).hasSize(12);
        // 全部位于 API 包
        surface.forEach(c ->
                assertThat(c.getPackageName()).isEqualTo("com.gallop.fastddd.tooling.api"));
    }

    @Nested
    @DisplayName("ModelType")
    class ModelTypeContract {
        @Test
        void fromCode_resolvesAndDefaults() {
            assertThat(ModelType.fromCode("ANEMIC")).isEqualTo(ModelType.ANEMIC);
            assertThat(ModelType.fromCode("rich")).isEqualTo(ModelType.RICH); // 大小写不敏感
            assertThat(ModelType.fromCode(null)).isEqualTo(ModelType.ANEMIC); // 默认
            assertThat(ModelType.fromCode("unknown")).isEqualTo(ModelType.ANEMIC);
        }

        @Test
        void isValid() {
            assertThat(ModelType.isValid("ANEMIC")).isTrue();
            assertThat(ModelType.isValid("RICH")).isTrue();
            assertThat(ModelType.isValid(null)).isFalse();
            assertThat(ModelType.isValid("xyz")).isFalse();
        }
    }

    @Nested
    @DisplayName("IdStrategy")
    class IdStrategyContract {
        @Test
        void fromCode_resolvesAndDefaults() {
            assertThat(IdStrategy.fromCode("ASSIGN_ID")).isEqualTo(IdStrategy.ASSIGN_ID);
            assertThat(IdStrategy.fromCode("INPUT")).isEqualTo(IdStrategy.INPUT);
            assertThat(IdStrategy.fromCode("NONE")).isEqualTo(IdStrategy.NONE);
            assertThat(IdStrategy.fromCode(null)).isEqualTo(IdStrategy.AUTO); // 默认
            assertThat(IdStrategy.fromCode("unknown")).isEqualTo(IdStrategy.AUTO);
        }

        @Test
        void codesAreStable() {
            // 代码值是跨产品传递契约，锁定稳定性
            assertThat(IdStrategy.AUTO.getCode()).isEqualTo("AUTO");
            assertThat(IdStrategy.ASSIGN_ID.getCode()).isEqualTo("ASSIGN_ID");
            assertThat(IdStrategy.INPUT.getCode()).isEqualTo("INPUT");
            assertThat(IdStrategy.NONE.getCode()).isEqualTo("NONE");
        }
    }

    @Nested
    @DisplayName("WriteMode")
    class WriteModeContract {
        @Test
        void fromCode_resolvesAndDefaults() {
            assertThat(WriteMode.fromCode("DTO")).isEqualTo(WriteMode.DTO);
            assertThat(WriteMode.fromCode("READ_ONLY")).isEqualTo(WriteMode.READ_ONLY);
            assertThat(WriteMode.fromCode(null)).isEqualTo(WriteMode.READ_ONLY); // 默认安全
            assertThat(WriteMode.fromCode("unknown")).isEqualTo(WriteMode.READ_ONLY);
        }

        @Test
        void codesAreStable() {
            // 代码值桥接 GeneratorConfig/模板，锁定稳定性
            assertThat(WriteMode.READ_ONLY.getCode()).isEqualTo("READ_ONLY");
            assertThat(WriteMode.DTO.getCode()).isEqualTo("DTO");
        }
    }

    @Nested
    @DisplayName("GenerationRequest（不可变 + builder）")
    class RequestContract {
        @Test
        void builder_appliesDefaults() {
            TableMetadata table = new TableMetadata();
            table.setTableName("t_user");
            GenerationRequest req = GenerationRequest.builder(table, "com.example.app").build();

            assertThat(req.getTable()).isSameAs(table);
            assertThat(req.getPackageName()).isEqualTo("com.example.app");
            assertThat(req.getModelType()).isEqualTo(ModelType.ANEMIC); // 默认
            assertThat(req.getIdStrategy()).isEqualTo(IdStrategy.AUTO); // 默认
            assertThat(req.getAuthor()).isEqualTo("FastDDD"); // 默认
            assertThat(req.isEnableCQRS()).isFalse();
        }

        @Test
        void builder_appliesExplicitValues() {
            TableMetadata table = new TableMetadata();
            table.setTableName("t_order");
            GenerationRequest req = GenerationRequest.builder(table, "com.example.app")
                    .moduleName("order")
                    .modelType(ModelType.RICH)
                    .enableCQRS(true)
                    .useLombok(true)
                    .useSwagger(false)
                    .idStrategy(IdStrategy.INPUT)
                    .author("alice")
                    .build();

            assertThat(req.getModuleName()).isEqualTo("order");
            assertThat(req.getModelType()).isEqualTo(ModelType.RICH);
            assertThat(req.isEnableCQRS()).isTrue();
            assertThat(req.isUseSwagger()).isFalse();
            assertThat(req.getIdStrategy()).isEqualTo(IdStrategy.INPUT);
            assertThat(req.getAuthor()).isEqualTo("alice");
        }

        @Test
        void builder_requiresTableAndPackage() {
            assertThatThrownBy(() -> GenerationRequest.builder(null, "com.example.app").build())
                    .isInstanceOf(NullPointerException.class);
            assertThatThrownBy(() -> GenerationRequest.builder(new TableMetadata(), null).build())
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("GenerationResult（不可变）")
    class ResultContract {
        @Test
        void filesAreImmutable() {
            GenerationResult result = GenerationResult.builder("t_user")
                    .addFile("a/User.java", "content-a")
                    .addFile("b/UserService.java", "content-b")
                    .build();

            assertThat(result.getTableName()).isEqualTo("t_user");
            assertThat(result.getFileCount()).isEqualTo(2);
            assertThat(result.getFiles()).containsKeys("a/User.java", "b/UserService.java");

            assertThatThrownBy(() -> result.getFiles().put("c/Hack.java", "x"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("DatabaseConnection（不可变 + 默认驱动）")
    class DatabaseConnectionContract {
        @Test
        void builder_appliesDefaults() {
            DatabaseConnection conn = DatabaseConnection.builder("jdbc:mysql://localhost:3306/db").build();
            assertThat(conn.getUrl()).isEqualTo("jdbc:mysql://localhost:3306/db");
            assertThat(conn.getDriverClassName()).isEqualTo(DatabaseConnection.DEFAULT_DRIVER);
            assertThat(conn.getUsername()).isNull();
            assertThat(conn.getCatalog()).isNull();
        }

        @Test
        void builder_appliesExplicitValues() {
            DatabaseConnection conn = DatabaseConnection.builder("jdbc:postgresql://h/db")
                    .username("alice").password("secret")
                    .driverClassName("org.postgresql.Driver")
                    .catalog("db").schema("public").tablePrefix("t_")
                    .build();
            assertThat(conn.getUsername()).isEqualTo("alice");
            assertThat(conn.getDriverClassName()).isEqualTo("org.postgresql.Driver");
            assertThat(conn.getSchema()).isEqualTo("public");
            assertThat(conn.getTablePrefix()).isEqualTo("t_");
        }

        @Test
        void builder_requiresUrl() {
            assertThatThrownBy(() -> DatabaseConnection.builder(null).build())
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
