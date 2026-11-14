package ${packageName}.service;

import ${packageName}.entity.${table.entityName};
import ${packageName}.mapper.${table.entityName}Mapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ${table.entityName}Service ${msg.proj.serviceTest.unitTestLabel}
 *
 * @author ${author}
 * @date ${date}
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("${table.entityName}Service ${msg.proj.serviceTest.testLabel}")
class ${table.entityName}ServiceTest {

    @Mock
    private ${table.entityName}Mapper ${table.entityName?uncap_first}Mapper;

    @InjectMocks
    private ${table.entityName}ServiceImpl ${table.entityName?uncap_first}Service;

    private ${table.entityName} testEntity;

    @BeforeEach
    void setUp() {
        testEntity = new ${table.entityName}();
        testEntity.setId(1L);
        // TODO: ${msg.proj.serviceTest.todoOtherFields}
    }

    @Test
    @DisplayName("${msg.proj.serviceTest.displayGetById}")
    void shouldReturnEntity_whenGetById() {
        // Given
        when(${table.entityName?uncap_first}Mapper.selectById(1L)).thenReturn(testEntity);

        // When
        ${table.entityName} result = ${table.entityName?uncap_first}Service.getById(1L);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(${table.entityName?uncap_first}Mapper, times(1)).selectById(1L);
    }

    @Test
    @DisplayName("${msg.proj.serviceTest.displaySave}")
    void shouldSaveEntity_whenSave() {
        // Given
        when(${table.entityName?uncap_first}Mapper.insert(any(${table.entityName}.class))).thenReturn(1);

        // When
        boolean result = ${table.entityName?uncap_first}Service.save(testEntity);

        // Then
        assertTrue(result);
        verify(${table.entityName?uncap_first}Mapper, times(1)).insert(any(${table.entityName}.class));
    }

    @Test
    @DisplayName("${msg.proj.serviceTest.displayUpdate}")
    void shouldUpdateEntity_whenUpdateById() {
        // Given
        when(${table.entityName?uncap_first}Mapper.updateById(any(${table.entityName}.class))).thenReturn(1);

        // When
        boolean result = ${table.entityName?uncap_first}Service.updateById(testEntity);

        // Then
        assertTrue(result);
        verify(${table.entityName?uncap_first}Mapper, times(1)).updateById(any(${table.entityName}.class));
    }

    @Test
    @DisplayName("${msg.proj.serviceTest.displayRemove}")
    void shouldRemoveEntity_whenRemoveById() {
        // Given
        when(${table.entityName?uncap_first}Mapper.deleteById(1L)).thenReturn(1);

        // When
        boolean result = ${table.entityName?uncap_first}Service.removeById(1L);

        // Then
        assertTrue(result);
        verify(${table.entityName?uncap_first}Mapper, times(1)).deleteById(1L);
    }
}
