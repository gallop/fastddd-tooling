package com.gallop.fastddd.codegen.service;

import com.gallop.fastddd.codegen.config.DatabaseConfig;
import com.gallop.fastddd.common.model.ColumnMetadata;
import com.gallop.fastddd.common.model.TableMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * 代码生成服务测试类
 *
 * @author gallop
 * @date 2023-10-10
 */
@ExtendWith(MockitoExtension.class)
public class CodeGeneratorServiceTest {

    @Mock
    private DatabaseMetadataService databaseMetadataService;

    @Mock
    private TemplateProcessorService templateProcessorService;

    @Mock
    private DatabaseConfig databaseConfig;

    @InjectMocks
    private CodeGeneratorService codeGeneratorService;

    private TableMetadata mockTableMetadata;

    @BeforeEach
    public void setUp() {
        // 创建模拟的表元数据
        mockTableMetadata = new TableMetadata();
        mockTableMetadata.setTableName("test_user");
        mockTableMetadata.setTableComment("用户表");
        mockTableMetadata.setEntityName("User");

        // 创建模拟的列元数据
        List<ColumnMetadata> columns = new ArrayList<>();

        ColumnMetadata idColumn = new ColumnMetadata();
        idColumn.setColumnName("id");
        idColumn.setColumnComment("主键ID");
        idColumn.setFieldName("id");
        idColumn.setJavaType("Long");
        idColumn.setPrimaryKey(true);
        columns.add(idColumn);

        ColumnMetadata usernameColumn = new ColumnMetadata();
        usernameColumn.setColumnName("username");
        usernameColumn.setColumnComment("用户名");
        usernameColumn.setFieldName("username");
        usernameColumn.setJavaType("String");
        columns.add(usernameColumn);

        ColumnMetadata emailColumn = new ColumnMetadata();
        emailColumn.setColumnName("email");
        emailColumn.setColumnComment("邮箱");
        emailColumn.setFieldName("email");
        emailColumn.setJavaType("String");
        columns.add(emailColumn);

        mockTableMetadata.setColumns(columns);

        // 设置模拟行为
        when(databaseMetadataService.getTable(anyString())).thenReturn(mockTableMetadata);
        when(templateProcessorService.processTemplate(anyString(), any())).thenReturn("mock template content");
    }

    @Test
    public void testGenerateCode() {
        // 创建配置对象
        com.gallop.fastddd.codegen.config.GeneratorConfig config = new com.gallop.fastddd.codegen.config.GeneratorConfig();
        config.setModelType(com.gallop.fastddd.codegen.config.GeneratorConfig.ModelType.ANEMIC);
        config.setGenerateEntity(true);
        config.setGenerateMapper(true);
        config.setGenerateService(true);
        config.setGenerateController(true);

        // 执行代码生成
        Map<String, String> generatedCode = codeGeneratorService.generateCode(
                "test_user",
                "com.gallop.test",
                "user",
                config);

        // 验证生成的代码 (检查键名是否包含文件名，因为现在返回的是完整路径)
        assertNotNull(generatedCode);
        assertTrue(generatedCode.keySet().stream().anyMatch(k -> k.endsWith("User.java")));
        assertTrue(generatedCode.keySet().stream().anyMatch(k -> k.endsWith("UserMapper.java")));
        assertTrue(generatedCode.keySet().stream().anyMatch(k -> k.endsWith("UserService.java")));
        assertTrue(generatedCode.keySet().stream().anyMatch(k -> k.endsWith("UserServiceImpl.java")));
        assertTrue(generatedCode.keySet().stream().anyMatch(k -> k.endsWith("UserController.java")));
    }
}